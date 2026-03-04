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
package org.flowable.variable.service.impl.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import org.flowable.common.engine.impl.persistence.entity.AbstractEntityNoRevision;

/**
 * Implementation of {@link VariableTraceEntity}. Insert-only entity (never updated).
 *
 * @author Filip Hrisafov
 */
public class VariableTraceEntityImpl extends AbstractEntityNoRevision implements VariableTraceEntity, Serializable {

    private static final long serialVersionUID = 1L;

    protected String traceId;
    protected long sequence;
    protected Date timestamp;
    protected String operationType;
    protected String variableName;
    protected String variableType;
    protected String valueText;
    protected boolean transientVariable;
    protected String elementId;
    protected String scopeId;
    protected String scopeType;
    protected String definitionId;
    protected String variableScopeId;
    protected String variableScopeType;
    protected String mappingId;

    @Override
    public Object getPersistentState() {
        // Insert-only entity — return a constant to prevent UPDATE generation
        return VariableTraceEntityImpl.class;
    }

    @Override
    public String getIdPrefix() {
        return VariableServiceEntityConstants.VARIABLE_SERVICE_ID_PREFIX;
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getOperationType() {
        return operationType;
    }

    @Override
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

    @Override
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String getVariableType() {
        return variableType;
    }

    @Override
    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    @Override
    public String getValueText() {
        return valueText;
    }

    @Override
    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    @Override
    public boolean isTransientVariable() {
        return transientVariable;
    }

    @Override
    public void setTransientVariable(boolean transientVariable) {
        this.transientVariable = transientVariable;
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public String getScopeId() {
        return scopeId;
    }

    @Override
    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    @Override
    public String getScopeType() {
        return scopeType;
    }

    @Override
    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public String getDefinitionId() {
        return definitionId;
    }

    @Override
    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    @Override
    public String getVariableScopeId() {
        return variableScopeId;
    }

    @Override
    public void setVariableScopeId(String variableScopeId) {
        this.variableScopeId = variableScopeId;
    }

    @Override
    public String getVariableScopeType() {
        return variableScopeType;
    }

    @Override
    public void setVariableScopeType(String variableScopeType) {
        this.variableScopeType = variableScopeType;
    }

    @Override
    public String getMappingId() {
        return mappingId;
    }

    @Override
    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }
}
