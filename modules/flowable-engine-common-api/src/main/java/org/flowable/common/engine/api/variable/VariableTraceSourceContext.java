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

/**
 * Captures the source context (the element that initiated a variable operation) during scope walking.
 * Bound via {@link ScopedValue} at the first/originating scope in a variable operation chain,
 * and carried through recursive parent scope calls without any parameter threading.
 *
 * @param sourceElementId the element ID (activity/plan item) that initiated the operation
 * @param sourceScopeId the scope ID (process/case instance) of the initiating element
 * @param sourceScopeType the scope type (e.g., "bpmn", "cmmn") of the initiating element
 * @param sourceDefinitionId the definition ID (process/case definition) of the initiating element
 */
public record VariableTraceSourceContext(
        String sourceElementId,
        String sourceScopeId,
        String sourceScopeType,
        String sourceDefinitionId) {

    public static final ScopedValue<VariableTraceSourceContext> CURRENT = ScopedValue.newInstance();
}
