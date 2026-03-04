/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.flowable.common.engine.impl.db.DbSqlSession;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.ConfigurationResource;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.flowable.variable.service.impl.persistence.entity.VariableTraceEntity;
import org.flowable.variable.service.impl.persistence.entity.VariableTraceEntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that variable trace entries are automatically persisted to the ACT_HI_VAR_TRACE table
 * when {@code variableTracePersistenceEnabled} is set to {@code true}.
 *
 * @author Filip Hrisafov
 */
@FlowableTest
@ConfigurationResource("org/flowable/engine/test/api/variables/flowable.variable-trace-persistence.cfg.xml")
class VariableTracePersistenceTest {

    private ProcessEngineConfigurationImpl processEngineConfiguration;
    private RuntimeService runtimeService;
    private TaskService taskService;

    @BeforeEach
    void setUp(ProcessEngine processEngine) {
        processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        runtimeService = processEngine.getRuntimeService();
        taskService = processEngine.getTaskService();
    }

    @AfterEach
    void cleanUp() {
        // Clean up persisted trace entries
        processEngineConfiguration.getCommandExecutor().execute(commandContext -> {
            DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
            dbSqlSession.getSqlSession()
                    .delete("org.flowable.variable.service.impl.persistence.entity.VariableTraceEntityImpl.bulkDeleteVariableTraceEntries");
            return null;
        });
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithProcessInstanceBuilder.bpmn20.xml")
    void testVariableTracePersistence() {
        // Verify helper and handler are wired
        assertThat(processEngineConfiguration.isVariableTracePersistenceEnabled()).isTrue();
        assertThat(processEngineConfiguration.isVariableTraceEnabled()).isTrue();
        assertThat(processEngineConfiguration.getVariableTraceHelper()).as("VariableTraceHelper").isNotNull();
        assertThat(processEngineConfiguration.getVariableTraceHelper().getHandler()).as("VariableTraceHelper handler").isNotNull();

        // Start a process that creates and reads variables
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceProcess")
                .variable("inputVar", "hello")
                .start();

        // Query the persisted trace entries
        List<VariableTraceEntity> traceEntries = processEngineConfiguration.getCommandExecutor().execute(commandContext -> {
            VariableTraceEntityManager entityManager = processEngineConfiguration
                    .getVariableServiceConfiguration()
                    .getVariableTraceEntityManager();
            return entityManager.findByTargetScopeId(processInstance.getId(), "bpmn");
        });

        assertThat(traceEntries).isNotEmpty();

        // Verify entries are ordered by sequence
        assertThat(traceEntries)
                .extracting(VariableTraceEntity::getSequence)
                .isSorted();

        // All entries should share the same traceId (same command)
        String traceId = traceEntries.get(0).getTraceId();
        assertThat(traceEntries)
                .extracting(VariableTraceEntity::getTraceId)
                .containsOnly(traceId);

        // Verify CREATE of inputVar at process start (source element is the start event)
        assertThat(traceEntries)
                .filteredOn(e -> "CREATE".equals(e.getOperationType()) && "inputVar".equals(e.getVariableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.getSourceElementId()).isEqualTo("theStart");
                    assertThat(e.getValueText()).isEqualTo("hello");
                    assertThat(e.getVariableType()).isEqualTo("string");
                    assertThat(e.getTargetScopeId()).isEqualTo(processInstance.getId());
                    assertThat(e.getTargetScopeType()).isEqualTo("bpmn");
                    assertThat(e.isTransientVariable()).isFalse();
                    assertThat(e.getTimestamp()).isNotNull();
                });

        // serviceTask1 reads inputVar
        assertThat(traceEntries)
                .filteredOn(e -> "READ".equals(e.getOperationType()) && "inputVar".equals(e.getVariableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.getSourceElementId()).isEqualTo("serviceTask1");
                    assertThat(e.getValueText()).isEqualTo("hello");
                });

        // serviceTask1 creates outputVar
        assertThat(traceEntries)
                .filteredOn(e -> "CREATE".equals(e.getOperationType()) && "outputVar".equals(e.getVariableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.getSourceElementId()).isEqualTo("serviceTask1");
                    assertThat(e.getValueText()).isEqualTo("HELLO");
                    assertThat(e.getVariableType()).isEqualTo("string");
                });
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithTaskCompletion.bpmn20.xml")
    void testVariableTracePersistenceAcrossCommands() {
        // First command: start process
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceTaskProcess")
                .variable("inputVar", "world")
                .start();

        // Second command: complete the task
        org.flowable.task.api.Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        assertThat(task).isNotNull();

        taskService.createTaskCompletionBuilder()
                .taskId(task.getId())
                .variable("taskVar", "fromTask")
                .complete();

        // Query all persisted trace entries for this process instance
        List<VariableTraceEntity> traceEntries = processEngineConfiguration.getCommandExecutor().execute(commandContext -> {
            VariableTraceEntityManager entityManager = processEngineConfiguration
                    .getVariableServiceConfiguration()
                    .getVariableTraceEntityManager();
            return entityManager.findByTargetScopeId(processInstance.getId(), "bpmn");
        });

        assertThat(traceEntries).isNotEmpty();

        // There should be entries from at least two different commands (two different traceIds)
        long distinctTraceIds = traceEntries.stream()
                .map(VariableTraceEntity::getTraceId)
                .distinct()
                .count();
        assertThat(distinctTraceIds).isGreaterThanOrEqualTo(2);

        // Verify entries from both commands are present
        assertThat(traceEntries)
                .extracting(VariableTraceEntity::getVariableName, VariableTraceEntity::getOperationType)
                .contains(
                        tuple("inputVar", "CREATE"),
                        tuple("taskVar", "CREATE"));
    }
}
