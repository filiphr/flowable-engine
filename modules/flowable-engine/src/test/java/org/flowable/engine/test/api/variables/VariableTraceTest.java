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
import java.util.Map;

import org.flowable.common.engine.api.variable.VariableTrace;
import org.flowable.common.engine.api.variable.VariableTraceEntry;
import org.flowable.common.engine.api.variable.VariableTraceOperationType;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.ObjectNode;

/**
 * @author Filip Hrisafov
 */
public class VariableTraceTest extends PluggableFlowableTestCase {

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithProcessInstanceBuilder.bpmn20.xml")
    public void testVariableTraceWithProcessInstanceBuilder() {
        VariableTrace trace = new VariableTrace();
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceProcess")
                .variable("inputVar", "hello")
                .variableTrace(trace)
                .start();

        // The process starts, sets inputVar, serviceTask1 reads inputVar and creates outputVar,
        // then stops at userTask1.
        assertThat(trace.isEmpty()).isFalse();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: Process Instance Builder ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-10s | source=%s | target=%s | transient=%s | %s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(),
                    e.scopeId(), e.variableScopeId(), e.transientVariable(), e.timestamp());
        }
        System.out.println("=== By Element ===");
        trace.byElement().forEach((element, list) -> {
            System.out.println("  [" + element + "]");
            for (VariableTraceEntry e : list) {
                System.out.printf("    %-6s  %-15s = %s%n", e.operationType(), e.variableName(), e.value());
            }
        });
        System.out.println();

        // Verify the initial CREATE entry (variable set at process start) has the start event as source element
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE && "theStart".equals(e.elementId()) && "inputVar".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.elementId()).as("elementId for initial CREATE").isEqualTo("theStart");
                    assertThat(e.scopeId()).as("scopeId for initial CREATE").isEqualTo(processInstance.getId());
                    assertThat(e.scopeType()).as("scopeType for initial CREATE").isEqualTo("bpmn");
                    assertThat(e.definitionId()).as("definitionId for initial CREATE").isEqualTo(processInstance.getProcessDefinitionId());
                    assertThat(e.variableScopeId()).as("variableScopeId for initial CREATE").isEqualTo(processInstance.getId());
                    assertThat(e.variableScopeType()).as("variableScopeType for initial CREATE").isEqualTo("bpmn");
                });

        // serviceTask1 should have read inputVar and created outputVar
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.READ)
                .extracting(VariableTraceEntry::elementId, VariableTraceEntry::variableName, VariableTraceEntry::variableType, VariableTraceEntry::value)
                .containsExactly(tuple("serviceTask1", "inputVar", "string", "hello"));

        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE)
                .extracting(VariableTraceEntry::elementId, VariableTraceEntry::variableName, VariableTraceEntry::variableType, VariableTraceEntry::value)
                .contains(tuple("serviceTask1", "outputVar", "string", "HELLO"));

        // Source scope should be the process instance
        assertThat(entries)
                .filteredOn(e -> "serviceTask1".equals(e.elementId()))
                .allSatisfy(e -> {
                    assertThat(e.scopeId()).isEqualTo(processInstance.getId());
                    assertThat(e.variableScopeId()).isEqualTo(processInstance.getId());
                    assertThat(e.definitionId()).isEqualTo(processInstance.getProcessDefinitionId());
                });

        // Verify byElement() grouping
        Map<String, List<VariableTraceEntry>> byElement = trace.byElement();
        assertThat(byElement).containsKey("serviceTask1");
        assertThat(byElement.get("serviceTask1")).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithTaskCompletion.bpmn20.xml")
    public void testVariableTraceWithTaskCompletion() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceTaskProcess")
                .variable("inputVar", "world")
                .start();

        org.flowable.task.api.Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        assertThat(task).isNotNull();

        // Complete the task with variable tracing
        VariableTrace trace = new VariableTrace();
        taskService.createTaskCompletionBuilder()
                .taskId(task.getId())
                .variable("taskVar", "fromTask")
                .variableTrace(trace)
                .complete();

        assertThat(trace.isEmpty()).isFalse();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: Task Completion ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-10s | source=%s | target=%s | transient=%s | %s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(),
                    e.scopeId(), e.variableScopeId(), e.transientVariable(), e.timestamp());
        }
        System.out.println();

        // serviceTask1 runs after task completion: reads inputVar, creates outputVar
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.READ && "inputVar".equals(e.variableName()))
                .extracting(VariableTraceEntry::elementId, VariableTraceEntry::value)
                .containsExactly(tuple("serviceTask1", "world"));

        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE && "outputVar".equals(e.variableName()))
                .extracting(VariableTraceEntry::elementId, VariableTraceEntry::value)
                .containsExactly(tuple("serviceTask1", "WORLD"));
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithProcessInstanceBuilder.bpmn20.xml")
    public void testVariableTraceWithPredicateFiltering() {
        // Deploy another process for filtering
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceProcess")
                .variable("inputVar", "test")
                .start();

        // Create a trace with a predicate that filters out everything
        VariableTrace filteredTrace = new VariableTrace((definitionId, scopeId, scopeType, elementId) -> false);

        org.flowable.task.api.Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        taskService.createTaskCompletionBuilder()
                .taskId(task.getId())
                .variableTrace(filteredTrace)
                .complete();

        // With predicate filtering everything out, trace should be empty
        assertThat(filteredTrace.isEmpty()).isTrue();

        // Now with a predicate that only captures serviceTask2
        ProcessInstance processInstance2 = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceProcess")
                .variable("inputVar", "test2")
                .start();

        VariableTrace selectiveTrace = new VariableTrace(
                (definitionId, scopeId, scopeType, elementId) -> "serviceTask2".equals(elementId));

        org.flowable.task.api.Task task2 = taskService.createTaskQuery()
                .processInstanceId(processInstance2.getId())
                .singleResult();

        taskService.createTaskCompletionBuilder()
                .taskId(task2.getId())
                .variableTrace(selectiveTrace)
                .complete();

        // Only serviceTask2 entries should be present
        assertThat(selectiveTrace.getEntries())
                .allSatisfy(e -> assertThat(e.elementId()).isEqualTo("serviceTask2"));
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testEngineAutoTrace.bpmn20.xml")
    public void testDirectScopedValueBinding() throws Exception {
        // This test verifies the same behavior the VariableTraceHelper provides:
        // binding a VariableTrace via ScopedValue and collecting entries.
        VariableTrace trace = new VariableTrace();
        ScopedValue.where(VariableTrace.CURRENT, trace)
                .call(() -> {
                    runtimeService.createProcessInstanceBuilder()
                            .processDefinitionKey("autoTraceProcess")
                            .variable("inputVar", "auto")
                            .start();
                    return null;
                });

        assertThat(trace.isEmpty()).isFalse();

        assertThat(trace.getEntries())
                .filteredOn(e -> "serviceTask1".equals(e.elementId()))
                .extracting(VariableTraceEntry::variableName, VariableTraceEntry::operationType)
                .contains(
                        tuple("inputVar", VariableTraceOperationType.READ),
                        tuple("outputVar", VariableTraceOperationType.CREATE));
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithProcessInstanceBuilder.bpmn20.xml")
    public void testTransientVariableTracing() {
        VariableTrace trace = new VariableTrace();
        runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceProcess")
                .variable("inputVar", "hello")
                .transientVariable("transientInput", "tempValue")
                .variableTrace(trace)
                .start();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: Transient Variables ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-10s | transient=%s | %s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(), e.transientVariable(), e.timestamp());
        }
        System.out.println();

        // Check that transient variable operations are recorded with transientVariable=true and null value
        List<VariableTraceEntry> transientEntries = entries.stream()
                .filter(VariableTraceEntry::transientVariable)
                .toList();

        // Transient variables should have null values and transientVariable flag set
        assertThat(transientEntries)
                .allSatisfy(e -> {
                    assertThat(e.value()).isNull();
                    assertThat(e.transientVariable()).isTrue();
                });
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithExpressions.bpmn20.xml")
    public void testVariableTraceWithExpressions() {
        VariableTrace trace = new VariableTrace();
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceExpressionProcess")
                .variable("inputVar", "hello")
                .variableTrace(trace)
                .start();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: Expressions ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-15s | source=%s | target=%s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(),
                    e.scopeId(), e.variableScopeId());
        }
        System.out.println();

        // serviceTask1 expression "${inputVar.toUpperCase()}" should READ inputVar
        assertThat(entries)
                .filteredOn(e -> "serviceTask1".equals(e.elementId()) && e.operationType() == VariableTraceOperationType.READ)
                .extracting(VariableTraceEntry::variableName, VariableTraceEntry::value)
                .contains(tuple("inputVar", "hello"));

        // serviceTask1 result variable should CREATE outputVar
        assertThat(entries)
                .filteredOn(e -> "serviceTask1".equals(e.elementId()) && e.operationType() == VariableTraceOperationType.CREATE)
                .extracting(VariableTraceEntry::variableName, VariableTraceEntry::value)
                .contains(tuple("outputVar", "HELLO"));

        // The exclusive gateway condition "${outputVar == 'HELLO'}" should READ outputVar
        assertThat(entries)
                .filteredOn(e -> "exclusiveGw".equals(e.elementId()) && e.operationType() == VariableTraceOperationType.READ)
                .extracting(VariableTraceEntry::variableName, VariableTraceEntry::value)
                .contains(tuple("outputVar", "HELLO"));

        // serviceTask2 expression "${outputVar.concat('_world')}" should READ outputVar and CREATE finalVar
        assertThat(entries)
                .filteredOn(e -> "serviceTask2".equals(e.elementId()) && e.operationType() == VariableTraceOperationType.READ)
                .extracting(VariableTraceEntry::variableName, VariableTraceEntry::value)
                .contains(tuple("outputVar", "HELLO"));

        assertThat(entries)
                .filteredOn(e -> "serviceTask2".equals(e.elementId()) && e.operationType() == VariableTraceOperationType.CREATE)
                .extracting(VariableTraceEntry::variableName, VariableTraceEntry::value)
                .contains(tuple("finalVar", "HELLO_world"));

        // Verify ordering via sequence
        assertThat(entries)
                .extracting(VariableTraceEntry::sequence)
                .isSorted();
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithDelete.bpmn20.xml")
    public void testVariableTraceWithDelete() {
        VariableTrace trace = new VariableTrace();
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceDeleteProcess")
                .variable("inputVar", "hello")
                .variableTrace(trace)
                .start();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: Delete Variable ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-10s | source=%s | target=%s | transient=%s | %s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(),
                    e.scopeId(), e.variableScopeId(), e.transientVariable(), e.timestamp());
        }
        System.out.println();

        // inputVar should be created at start (start event), read by serviceTask1, then deleted by serviceTask1
        assertThat(entries)
                .extracting(VariableTraceEntry::elementId, VariableTraceEntry::variableName,
                        VariableTraceEntry::operationType, VariableTraceEntry::value)
                .containsExactly(
                        tuple("theStart", "inputVar", VariableTraceOperationType.CREATE, "hello"),
                        tuple("serviceTask1", "inputVar", VariableTraceOperationType.READ, "hello"),
                        tuple("serviceTask1", "outputVar", VariableTraceOperationType.CREATE, "HELLO"),
                        tuple("serviceTask1", "inputVar", VariableTraceOperationType.DELETE, null));

        // DELETE entry should have null value
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.DELETE)
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.variableName()).isEqualTo("inputVar");
                    assertThat(e.value()).isNull();
                    assertThat(e.elementId()).isEqualTo("serviceTask1");
                    assertThat(e.scopeId()).isEqualTo(processInstance.getId());
                });
    }

    /**
     * Service task delegate that reads inputVar and creates outputVar with its uppercase value.
     */
    public static class ReadAndWriteDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String input = (String) execution.getVariable("inputVar");
            execution.setVariable("outputVar", input.toUpperCase());
        }
    }

    /**
     * Service task delegate that updates an existing variable.
     */
    public static class UpdateVariableDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String output = (String) execution.getVariable("outputVar");
            execution.setVariable("outputVar", output + "_updated");
        }
    }

    /**
     * Service task delegate that reads inputVar, creates outputVar, and deletes inputVar.
     */
    public static class ReadCreateAndDeleteDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String input = (String) execution.getVariable("inputVar");
            execution.setVariable("outputVar", input.toUpperCase());
            execution.removeVariable("inputVar");
        }
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testJsonInPlaceMutationTracing.bpmn20.xml")
    public void testJsonInPlaceMutationTracing() {
        ObjectNode jsonVar = processEngineConfiguration.getObjectMapper()
                .createObjectNode()
                .put("name", "Kermit")
                .put("age", 30);

        VariableTrace trace = new VariableTrace();
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("jsonInPlaceMutationProcess")
                .variable("jsonVar", jsonVar)
                .variableTrace(trace)
                .start();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: JSON In-Place Mutation ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-40s | transient=%s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(), e.transientVariable());
        }
        System.out.println();

        // The initial CREATE of jsonVar should be recorded with type "json"
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE && "jsonVar".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> assertThat(e.variableType()).isEqualTo("json"));

        // Service tasks mutate the JSON in-place. The TraceableObject detects the cumulative change
        // at command close and records an UPDATE with the diff.
        List<VariableTraceEntry> jsonUpdates = entries.stream()
                .filter(e -> e.operationType() == VariableTraceOperationType.UPDATE && "jsonVar".equals(e.variableName()))
                .toList();

        assertThat(jsonUpdates).hasSize(1);
        VariableTraceEntry updateEntry = jsonUpdates.get(0);

        // Source element is null for in-place mutations (detected at command close, not attributable to a single element)
        assertThat(updateEntry.elementId()).isNull();
        assertThat(updateEntry.variableType()).isEqualTo("json");
        assertThat(updateEntry.variableScopeId()).isEqualTo(processInstance.getId());

        // The value should be the diff, not the full JSON
        @SuppressWarnings("unchecked")
        Map<String, Object> diff = (Map<String, Object>) updateEntry.value();
        assertThat(diff).isNotNull();

        // serviceTask1 added "city" field, serviceTask2 changed "name" from "Kermit" to "Fozzie"
        @SuppressWarnings("unchecked")
        Map<String, Object> added = (Map<String, Object>) diff.get("added");
        assertThat(added).containsEntry("city", "Amsterdam");

        @SuppressWarnings("unchecked")
        Map<String, Object> changed = (Map<String, Object>) diff.get("changed");
        assertThat(changed).containsKey("name");

        @SuppressWarnings("unchecked")
        Map<String, Object> nameDiff = (Map<String, Object>) changed.get("name");
        assertThat(nameDiff).containsEntry("old", "Kermit").containsEntry("new", "Fozzie");

        // "age" was not changed, so it should not appear in the diff
        assertThat(diff).doesNotContainKey("removed");
        assertThat(changed).doesNotContainKey("age");
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithInitiator.bpmn20.xml")
    public void testVariableTraceWithInitiator() {
        try {
            identityService.setAuthenticatedUserId("kermit");

            VariableTrace trace = new VariableTrace();
            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey("variableTraceWithInitiatorProcess")
                    .variable("inputVar", "hello")
                    .variableTrace(trace)
                    .start();

            List<VariableTraceEntry> entries = trace.getEntries();

            System.out.println("=== Variable Trace: With Initiator ===");
            for (VariableTraceEntry e : entries) {
                System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-10s | source=%s/%s | target=%s/%s%n",
                        e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(),
                        e.scopeId(), e.scopeType(), e.variableScopeId(), e.variableScopeType());
            }
            System.out.println();

            // The initiator variable should have the start event as source element
            assertThat(entries)
                    .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE
                            && "initiator".equals(e.variableName()))
                    .hasSize(1)
                    .first()
                    .satisfies(e -> {
                        assertThat(e.elementId()).as("elementId for initiator").isEqualTo("theStart");
                        assertThat(e.scopeId()).as("scopeId for initiator").isEqualTo(processInstance.getId());
                        assertThat(e.value()).isEqualTo("kermit");
                    });

            // inputVar should also have the start event as source element
            assertThat(entries)
                    .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE
                            && "inputVar".equals(e.variableName()))
                    .hasSize(1)
                    .first()
                    .satisfies(e -> {
                        assertThat(e.elementId()).as("elementId for inputVar").isEqualTo("theStart");
                        assertThat(e.scopeId()).as("scopeId for inputVar").isEqualTo(processInstance.getId());
                    });
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceViaProcessInstance.bpmn20.xml")
    public void testVariableTraceViaProcessInstance() {
        // Tests that execution.getProcessInstance().setVariable(...) gets the correct source element
        VariableTrace trace = new VariableTrace();
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceViaProcessInstanceProcess")
                .variable("inputVar", "hello")
                .variableTrace(trace)
                .start();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: Via ProcessInstance ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-15s | var=%-15s | type=%-10s | value=%-10s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value());
        }
        System.out.println();

        // The outputVar created via execution.getProcessInstance().setVariable() should have serviceTask1 as source element
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE && "outputVar".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.elementId()).as("elementId for processInstance.setVariable()").isEqualTo("serviceTask1");
                    assertThat(e.scopeId()).isEqualTo(processInstance.getId());
                    assertThat(e.variableScopeId()).isEqualTo(processInstance.getId());
                });
    }

    /**
     * Service task delegate that reads inputVar and creates outputVar via the process instance scope.
     * This tests that the source element is correctly set when setVariable() is called on the process instance
     * rather than on the current execution.
     */
    public static class WriteViaProcessInstanceDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String input = (String) execution.getVariable("inputVar");
            ExecutionEntity executionEntity = (ExecutionEntity) execution;
            executionEntity.getProcessInstance().setVariable("outputVar", input.toUpperCase());
        }
    }

    @Test
    @Deployment(resources = {
            "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithCallActivity.parent.bpmn20.xml",
            "org/flowable/engine/test/api/variables/VariableTraceTest.testVariableTraceWithCallActivity.child.bpmn20.xml"
    })
    public void testVariableTraceWithCallActivity() {
        VariableTrace trace = new VariableTrace();
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("variableTraceCallActivityParent")
                .variable("inputVar", "hello")
                .variableTrace(trace)
                .start();

        List<VariableTraceEntry> entries = trace.getEntries();

        System.out.println("=== Variable Trace: Call Activity ===");
        for (VariableTraceEntry e : entries) {
            System.out.printf("  #%-3d %-6s | element=%-20s | var=%-15s | type=%-10s | value=%-10s | source=%s/%s | target=%s/%s%n",
                    e.sequence(), e.operationType(), e.elementId(), e.variableName(), e.variableType(), e.value(),
                    e.scopeId(), e.scopeType(), e.variableScopeId(), e.variableScopeType());
        }
        System.out.println();

        // inputVar created at parent start event
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE
                        && "inputVar".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.elementId()).as("elementId").isEqualTo("parentStart");
                    assertThat(e.scopeId()).as("scopeId").isEqualTo(processInstance.getId());
                });

        // childInput created in child process via in-mapping (source element is the call activity)
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE
                        && "childInput".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.elementId()).as("elementId for childInput").isEqualTo("callActivity1");
                    assertThat(e.scopeId()).as("scopeId for childInput").isEqualTo(processInstance.getId());
                });

        // childServiceTask reads childInput and creates childOutput in the child process
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.READ
                        && "childInput".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.elementId()).as("elementId for READ").isEqualTo("childServiceTask");
                });

        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE
                        && "childOutput".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.elementId()).as("elementId for childOutput CREATE").isEqualTo("childServiceTask");
                });

        // outputVar created in parent process via out-mapping (source element is the call activity)
        assertThat(entries)
                .filteredOn(e -> e.operationType() == VariableTraceOperationType.CREATE
                        && "outputVar".equals(e.variableName()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.elementId()).as("elementId for outputVar").isEqualTo("callActivity1");
                    assertThat(e.scopeId()).as("scopeId for outputVar").isEqualTo(processInstance.getId());
                });

        // Mapping ID verification:
        // In-mapping: READ of inputVar gets a mappingId (happens inside IOParameterUtil per-parameter scope).
        // The CREATE of childInput does NOT get a mappingId because it happens via bulk setVariables
        // during child process initialization, outside the per-parameter ScopedValue binding.
        VariableTraceEntry inMappingRead = entries.stream()
                .filter(e -> e.operationType() == VariableTraceOperationType.READ && "inputVar".equals(e.variableName())
                        && "callActivity1".equals(e.elementId()))
                .findFirst().orElseThrow();
        assertThat(inMappingRead.mappingId()).as("in-mapping READ should have mappingId").isNotNull();

        VariableTraceEntry inMappingCreate = entries.stream()
                .filter(e -> e.operationType() == VariableTraceOperationType.CREATE && "childInput".equals(e.variableName()))
                .findFirst().orElseThrow();
        assertThat(inMappingCreate.mappingId()).as("in-mapping CREATE has no mappingId (bulk setVariables)").isNull();

        // Out-mapping: both READ and CREATE share the same mappingId because they both happen
        // inside the same IOParameterUtil processParameter call (direct variable consumer).
        VariableTraceEntry outMappingRead = entries.stream()
                .filter(e -> e.operationType() == VariableTraceOperationType.READ && "childOutput".equals(e.variableName())
                        && "callActivity1".equals(e.elementId()))
                .findFirst().orElseThrow();
        VariableTraceEntry outMappingCreate = entries.stream()
                .filter(e -> e.operationType() == VariableTraceOperationType.CREATE && "outputVar".equals(e.variableName()))
                .findFirst().orElseThrow();
        assertThat(outMappingRead.mappingId()).as("out-mapping READ should have mappingId").isNotNull();
        assertThat(outMappingCreate.mappingId()).as("out-mapping CREATE should have same mappingId").isEqualTo(outMappingRead.mappingId());

        // In-mapping and out-mapping should have different mappingIds
        assertThat(inMappingRead.mappingId()).as("in and out mapping should have different IDs")
                .isNotEqualTo(outMappingRead.mappingId());

        // Non-mapping entries should have null mappingId
        VariableTraceEntry initialCreate = entries.stream()
                .filter(e -> e.operationType() == VariableTraceOperationType.CREATE && "inputVar".equals(e.variableName())
                        && "parentStart".equals(e.elementId()))
                .findFirst().orElseThrow();
        assertThat(initialCreate.mappingId()).as("non-mapping CREATE should have null mappingId").isNull();

        // Delegate reads/writes should have null mappingId
        VariableTraceEntry delegateRead = entries.stream()
                .filter(e -> e.operationType() == VariableTraceOperationType.READ && "childInput".equals(e.variableName())
                        && "childServiceTask".equals(e.elementId()))
                .findFirst().orElseThrow();
        assertThat(delegateRead.mappingId()).as("delegate READ should have null mappingId").isNull();
    }

    /**
     * Service task delegate for child process: reads childInput and creates childOutput.
     */
    public static class ReadAndWriteChildDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            String input = (String) execution.getVariable("childInput");
            execution.setVariable("childOutput", input.toUpperCase());
        }
    }

    /**
     * Service task delegate that gets a JSON variable and adds a "city" field in-place.
     */
    public static class AddFieldToJsonDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            ObjectNode json = (ObjectNode) execution.getVariable("jsonVar");
            json.put("city", "Amsterdam");
            // No setVariable() call — this is an in-place mutation
        }
    }

    /**
     * Service task delegate that gets a JSON variable and modifies the "name" field in-place.
     */
    public static class ModifyJsonFieldDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            ObjectNode json = (ObjectNode) execution.getVariable("jsonVar");
            json.put("name", "Fozzie");
            // No setVariable() call — this is an in-place mutation
        }
    }
}
