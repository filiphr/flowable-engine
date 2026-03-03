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
 * Predicate for filtering which variable operations should be traced.
 * Evaluated at recording time when a variable is read, created, updated, or deleted.
 * Can be used to limit tracing to specific process/case definitions or instances.
 */
@FunctionalInterface
public interface VariableTracePredicate {

    /**
     * Determines whether the variable operation should be traced.
     *
     * @param definitionId the definition ID (process or case definition) of the source element
     * @param scopeId the scope ID (process or case instance) of the source element
     * @param scopeType the scope type (e.g., "bpmn", "cmmn")
     * @param elementId the element ID (activity, plan item) that initiated the operation
     * @return {@code true} if the operation should be traced, {@code false} to skip it
     */
    boolean shouldTrace(String definitionId, String scopeId, String scopeType, String elementId);
}
