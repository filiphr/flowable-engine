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
package org.flowable.bpmn.converter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.util.BpmnXMLUtil;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ReceiveTask;

/**
 * @author Tijs Rademakers
 */
public class ReceiveTaskXMLConverter extends BaseBpmnXMLConverter {

    @Override
    public Class<? extends BaseElement> getBpmnElementType() {
        return ReceiveTask.class;
    }

    @Override
    protected String getXMLElementName() {
        return ELEMENT_TASK_RECEIVE;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
        ReceiveTask receiveTask = new ReceiveTask();
        BpmnXMLUtil.addXMLLocation(receiveTask, xtr);
        
        String skipExpression = BpmnXMLUtil.getAttributeValue(ATTRIBUTE_TASK_SCRIPT_SKIP_EXPRESSION, xtr);
        if (StringUtils.isNotEmpty(skipExpression)) {
            receiveTask.setSkipExpression(skipExpression);
        }
        
        BpmnXMLUtil.addCustomAttributes(xtr, receiveTask, defaultElementAttributes, defaultActivityAttributes);
        
        parseChildElements(getXMLElementName(), receiveTask, model, xtr);
        return receiveTask;
    }

    @Override
    protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
        ReceiveTask receiveTask = (ReceiveTask) element;
        writeQualifiedAttribute(ATTRIBUTE_TASK_SCRIPT_SKIP_EXPRESSION, receiveTask.getSkipExpression(), xtw);
    }

    @Override
    protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
    }
}
