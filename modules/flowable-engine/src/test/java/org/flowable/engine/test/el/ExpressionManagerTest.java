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

package org.flowable.engine.test.el;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.impl.el.VariableContainerWrapper;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.variable.service.impl.el.NoExecutionVariableScope;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Frederik Heremans
 */
public class ExpressionManagerTest extends PluggableFlowableTestCase {

    @Test
    public void testExpressionEvaluationWithoutProcessContext() {
        Expression expression = this.processEngineConfiguration.getExpressionManager().createExpression("#{1 == 1}");
        Object value = expression.getValue(new NoExecutionVariableScope());
        assertThat(value).isEqualTo(true);
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
    public void testIntJsonVariableSerialization() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("mapVariable", processEngineConfiguration.getObjectMapper().createObjectNode().put("minIntVar", Integer.MIN_VALUE));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

        Expression expression = this.processEngineConfiguration.getExpressionManager().createExpression("#{mapVariable.minIntVar}");
        Object value = managementService.executeCommand(commandContext ->
            expression.getValue((ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult()));

        assertThat(value).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
    public void testShortJsonVariableSerialization() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("mapVariable", processEngineConfiguration.getObjectMapper().createObjectNode().put("minShortVar", Short.MIN_VALUE));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

        Expression expression = this.processEngineConfiguration.getExpressionManager().createExpression("#{mapVariable.minShortVar}");
        Object value = managementService.executeCommand(commandContext ->
            expression.getValue((ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult()));

        assertThat(value).isEqualTo((int) Short.MIN_VALUE);
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
    public void testOverloadedMethodUsage() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("nodeVariable", processEngineConfiguration.getObjectMapper().createObjectNode());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

        Expression expression = this.processEngineConfiguration.getExpressionManager().createExpression("#{nodeVariable.put('stringVar', 'String value').put('intVar', 10)}");
        Object value = managementService.executeCommand(commandContext ->
            expression.getValue((ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult()));

        Assertions.assertThat(value)
            .isInstanceOfSatisfying(ObjectNode.class, node -> {
                Assertions.assertThat(node.path("stringVar").textValue()).isEqualTo("String value");
                Assertions.assertThat(node.path("intVar").intValue()).isEqualTo(10);
            });
    }

    @Test
    void testBeanInvocationPerformance() {
        // With Odyseus
        // 5.5s
        // 4.8s
        // 5s
        Map<String, Object> variables = new HashMap<>();
        variables.put("bean", new MyBean());
        VariableContainerWrapper variableContainer = new VariableContainerWrapper(variables);

        int iterations = 10_000;
        long start = System.nanoTime();
        Object value = null;
        for (int i = 0; i < iterations; i++) {
            variables.put("param", "hello" + i);
            Expression expression = processEngineConfiguration.getExpressionManager().createExpression("${bean.invoke(param)}");
            value = expression.getValue(variableContainer);
        }

        long end = System.nanoTime();
        System.out.println(value);

        System.out.println("Run in " + ((end - start) / 1_000_000) + "ms");
    }

    @Test
    void testStringConcatenationPerformance() {
        // With Odyseus
        // 278ms
        // 200ms
        // 268ms

        Map<String, Object> variables = new HashMap<>();
        VariableContainerWrapper variableContainer = new VariableContainerWrapper(variables);

        int iterations = 100_000;
        long start = System.nanoTime();
        Object value = null;
        for (int i = 0; i < iterations; i++) {
            variables.put("param", "hello" + i);
            Expression expression = processEngineConfiguration.getExpressionManager().createExpression("Task name ${param}");
            value = expression.getValue(variableContainer);
        }

        long end = System.nanoTime();
        System.out.println(value);

        System.out.println("Run in " + ((end - start) / 1_000_000) + "ms");
    }

    public static class MyBean {

        public String invoke(String param) {
            return param;
        }
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
    public void testFloatJsonVariableSerialization() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("mapVariable", processEngineConfiguration.getObjectMapper().createObjectNode().put("minFloatVar", new Float(-1.5)));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

        Expression expression = this.processEngineConfiguration.getExpressionManager().createExpression("#{mapVariable.minFloatVar}");
        Object value = managementService.executeCommand(commandContext ->
            expression.getValue((ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult()));

        assertThat(value).isEqualTo(-1.5d);
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
    public void testNullJsonVariableSerialization() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("mapVariable", processEngineConfiguration.getObjectMapper().createObjectNode().putNull("nullVar"));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

        Expression expression = this.processEngineConfiguration.getExpressionManager().createExpression("#{mapVariable.nullVar}");
        Object value = managementService.executeCommand(commandContext ->
            expression.getValue((ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult()));

        assertThat(value).isNull();
    }

    @Test
    @Deployment
    public void testMethodExpressions() {
        // Process contains 2 service tasks. One containing a method with no params, the other
        // contains a method with 2 params. When the process completes without exception, test passed.
        Map<String, Object> vars = new HashMap<>();
        vars.put("aString", "abcdefgh");
        runtimeService.startProcessInstanceByKey("methodExpressionProcess", vars);

        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("methodExpressionProcess").count()).isZero();
    }

    @Test
    @Deployment
    public void testExecutionAvailable() {
        Map<String, Object> vars = new HashMap<>();

        vars.put("myVar", new ExecutionTestVariable());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testExecutionAvailableProcess", vars);

        // Check of the testMethod has been called with the current execution
        String value = (String) runtimeService.getVariable(processInstance.getId(), "testVar");
        assertThat(value).isNotNull();
        assertThat(value).isEqualTo("myValue");
    }

    @Test
    @Deployment
    public void testAuthenticatedUserIdAvailable() {
        try {
            // Setup authentication
            Authentication.setAuthenticatedUserId("frederik");
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testAuthenticatedUserIdAvailableProcess");

            // Check if the variable that has been set in service-task is the
            // authenticated user
            String value = (String) runtimeService.getVariable(processInstance.getId(), "theUser");
            assertThat(value).isNotNull();
            assertThat(value).isEqualTo("frederik");
        } finally {
            // Cleanup
            Authentication.setAuthenticatedUserId(null);
        }
    }
}
