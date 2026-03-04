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

import java.util.Date;

import org.flowable.common.engine.impl.persistence.entity.Entity;

/**
 * Entity representing a single variable trace entry persisted to the {@code ACT_HI_VAR_TRACE} table.
 * Trace entries are insert-only (never updated).
 *
 * @author Filip Hrisafov
 */
public interface VariableTraceEntity extends Entity {

    String getTraceId();
    void setTraceId(String traceId);

    long getSequence();
    void setSequence(long sequence);

    Date getTimestamp();
    void setTimestamp(Date timestamp);

    String getOperationType();
    void setOperationType(String operationType);

    String getVariableName();
    void setVariableName(String variableName);

    String getVariableType();
    void setVariableType(String variableType);

    String getValueText();
    void setValueText(String valueText);

    boolean isTransientVariable();
    void setTransientVariable(boolean transientVariable);

    String getElementId();
    void setElementId(String elementId);

    String getScopeId();
    void setScopeId(String scopeId);

    String getScopeType();
    void setScopeType(String scopeType);

    String getDefinitionId();
    void setDefinitionId(String definitionId);

    String getVariableScopeId();
    void setVariableScopeId(String variableScopeId);

    String getVariableScopeType();
    void setVariableScopeType(String variableScopeType);

    String getMappingId();
    void setMappingId(String mappingId);
}
