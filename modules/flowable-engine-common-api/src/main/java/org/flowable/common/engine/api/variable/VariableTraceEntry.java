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
package org.flowable.common.engine.api.variable;

import java.time.Instant;

/**
 * A single entry in a variable trace, recording a variable operation (read, create, update, delete)
 * with full source and target context.
 *
 * @param sequence the order of this entry within the trace (monotonically increasing)
 * @param timestamp the wall-clock time when this entry was recorded
 * @param sourceElementId the element (activity/plan item) that initiated the operation
 * @param sourceScopeId the scope ID (process/case instance) of the initiating element
 * @param sourceScopeType the scope type (e.g., "bpmn", "cmmn") of the initiating element
 * @param sourceDefinitionId the definition ID (process/case definition) of the initiating element
 * @param targetScopeId the scope ID where the variable is actually stored
 * @param targetScopeType the scope type where the variable is actually stored
 * @param variableName the name of the variable
 * @param variableType the Flowable variable type name (e.g., "string", "json", "integer", "boolean", "transient")
 * @param value the variable value ({@code null} for transient variables and deletes)
 * @param operationType the type of operation
 * @param transientVariable whether this is a transient variable (only the name is meaningful, value is always null)
 */
public record VariableTraceEntry(
        long sequence,
        Instant timestamp,
        String sourceElementId,
        String sourceScopeId,
        String sourceScopeType,
        String sourceDefinitionId,
        String targetScopeId,
        String targetScopeType,
        String variableName,
        String variableType,
        Object value,
        VariableTraceOperationType operationType,
        boolean transientVariable) {

}
