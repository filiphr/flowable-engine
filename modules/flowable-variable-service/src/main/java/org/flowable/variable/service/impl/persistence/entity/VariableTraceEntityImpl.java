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
    protected String sourceElementId;
    protected String sourceScopeId;
    protected String sourceScopeType;
    protected String sourceDefinitionId;
    protected String targetScopeId;
    protected String targetScopeType;

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
    public String getSourceElementId() {
        return sourceElementId;
    }

    @Override
    public void setSourceElementId(String sourceElementId) {
        this.sourceElementId = sourceElementId;
    }

    @Override
    public String getSourceScopeId() {
        return sourceScopeId;
    }

    @Override
    public void setSourceScopeId(String sourceScopeId) {
        this.sourceScopeId = sourceScopeId;
    }

    @Override
    public String getSourceScopeType() {
        return sourceScopeType;
    }

    @Override
    public void setSourceScopeType(String sourceScopeType) {
        this.sourceScopeType = sourceScopeType;
    }

    @Override
    public String getSourceDefinitionId() {
        return sourceDefinitionId;
    }

    @Override
    public void setSourceDefinitionId(String sourceDefinitionId) {
        this.sourceDefinitionId = sourceDefinitionId;
    }

    @Override
    public String getTargetScopeId() {
        return targetScopeId;
    }

    @Override
    public void setTargetScopeId(String targetScopeId) {
        this.targetScopeId = targetScopeId;
    }

    @Override
    public String getTargetScopeType() {
        return targetScopeType;
    }

    @Override
    public void setTargetScopeType(String targetScopeType) {
        this.targetScopeType = targetScopeType;
    }
}
