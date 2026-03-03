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
 * Handler that receives variable trace data after a command completes when engine-level
 * variable tracing is enabled. Implementations can log, store, or forward the trace data
 * as needed.
 */
@FunctionalInterface
public interface VariableTraceHandler {

    /**
     * Called after a command completes if the trace is non-empty.
     *
     * @param trace the variable trace collected during the command execution
     */
    void handle(VariableTrace trace);
}
