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
package org.flowable.engine.impl.util.condition;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.api.variable.VariableTraceSourceContext;
import org.flowable.engine.DynamicBpmnConstants;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.Condition;
import org.flowable.engine.impl.context.BpmnOverrideContext;
import org.flowable.engine.impl.el.UelExpressionCondition;
import org.flowable.engine.impl.scripting.ScriptCondition;
import org.flowable.engine.impl.util.CommandContextUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ConditionUtil {

    public static boolean hasTrueCondition(SequenceFlow sequenceFlow, DelegateExecution execution) {
        String conditionExpression = null;
        if (CommandContextUtil.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
            ObjectNode elementProperties = BpmnOverrideContext.getBpmnOverrideElementProperties(sequenceFlow.getId(), execution.getProcessDefinitionId());
            conditionExpression = getActiveValue(sequenceFlow.getConditionExpression(), DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION, elementProperties);
        } else {
            conditionExpression = sequenceFlow.getConditionExpression();
        }

        if (StringUtils.isNotEmpty(conditionExpression)) {
	        String conditionLanguage = sequenceFlow.getConditionLanguage();
	        return hasTrueCondition(sequenceFlow.getId(), conditionExpression, conditionLanguage, execution);
        } else {
            return true;
        }

    }

	public static boolean hasTrueCondition(String elementId, String conditionExpression, String conditionLanguage, DelegateExecution execution) {
		Condition condition;
		if (conditionLanguage == null) {
			Expression expression = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager().createExpression(conditionExpression);
			condition = new UelExpressionCondition(expression);
		} else {
			condition = new ScriptCondition(conditionExpression, conditionLanguage);
		}

		// When variable tracing is active and source context is already bound (e.g., from a gateway),
		// rebind with the sequence flow element ID so that variable reads are attributed to the sequence flow.
		if (VariableTraceSourceContext.CURRENT.isBound()) {
			VariableTraceSourceContext parentContext = VariableTraceSourceContext.CURRENT.get();
			VariableTraceSourceContext sequenceFlowContext = new VariableTraceSourceContext(
					elementId,
					parentContext.scopeId(),
					parentContext.scopeType(),
					parentContext.definitionId());
			boolean[] result = new boolean[1];
			ScopedValue.where(VariableTraceSourceContext.CURRENT, sequenceFlowContext)
					.run(() -> result[0] = condition.evaluate(elementId, execution));
			return result[0];
		}

		return condition.evaluate(elementId, execution);
	}

    protected static String getActiveValue(String originalValue, String propertyName, ObjectNode elementProperties) {
        String activeValue = originalValue;
        if (elementProperties != null) {
            JsonNode overrideValueNode = elementProperties.get(propertyName);
            if (overrideValueNode != null) {
                if (overrideValueNode.isNull()) {
                    activeValue = null;
                } else {
                    activeValue = overrideValueNode.asString();
                }
            }
        }
        return activeValue;
    }

}
