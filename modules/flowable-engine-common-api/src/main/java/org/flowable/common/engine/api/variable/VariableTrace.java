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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Accumulates variable trace entries during a command execution. Thread-safe to support
 * parallel gateway branches recording concurrently.
 * <p>
 * Can be used in two ways:
 * <ul>
 *     <li><b>Caller-controlled</b>: Create a {@code VariableTrace} and pass it to a builder
 *         (e.g., {@code ProcessInstanceBuilder.variableTrace(trace)} or
 *         {@code TaskCompletionBuilder.variableTrace(trace)}). The trace is bound for that
 *         specific operation and the caller can inspect it afterward.</li>
 *     <li><b>Engine-level</b>: Configure a {@link VariableTraceHandler} on the engine configuration.
 *         The engine automatically creates and binds a trace for every command, and flushes
 *         non-empty traces to the handler after the command completes.</li>
 * </ul>
 */
public class VariableTrace {

    public static final ScopedValue<VariableTrace> CURRENT = ScopedValue.newInstance();

    private final ConcurrentLinkedQueue<VariableTraceEntry> entries = new ConcurrentLinkedQueue<>();
    private final AtomicLong sequenceCounter = new AtomicLong();
    private final VariableTracePredicate predicate;

    public VariableTrace() {
        this(null);
    }

    public VariableTrace(VariableTracePredicate predicate) {
        this.predicate = predicate;
    }

    /**
     * Records a variable read operation.
     */
    public void recordRead(VariableTraceSourceContext source,
            String targetScopeId, String targetScopeType,
            String variableName, String variableType, Object value, boolean transientVariable) {

        if (!shouldTrace(source)) {
            return;
        }
        entries.add(new VariableTraceEntry(
                sequenceCounter.getAndIncrement(),
                Instant.now(),
                source != null ? source.sourceElementId() : null,
                source != null ? source.sourceScopeId() : null,
                source != null ? source.sourceScopeType() : null,
                source != null ? source.sourceDefinitionId() : null,
                targetScopeId,
                targetScopeType,
                variableName,
                variableType,
                transientVariable ? null : value,
                VariableTraceOperationType.READ,
                transientVariable));
    }

    /**
     * Records a variable write operation (create, update, or delete).
     */
    public void recordWrite(VariableTraceSourceContext source,
            String targetScopeId, String targetScopeType,
            String variableName, String variableType, Object value,
            VariableTraceOperationType operationType, boolean transientVariable) {

        if (!shouldTrace(source)) {
            return;
        }
        entries.add(new VariableTraceEntry(
                sequenceCounter.getAndIncrement(),
                Instant.now(),
                source != null ? source.sourceElementId() : null,
                source != null ? source.sourceScopeId() : null,
                source != null ? source.sourceScopeType() : null,
                source != null ? source.sourceDefinitionId() : null,
                targetScopeId,
                targetScopeType,
                variableName,
                variableType,
                transientVariable ? null : value,
                operationType,
                transientVariable));
    }

    protected boolean shouldTrace(VariableTraceSourceContext source) {
        if (predicate == null) {
            return true;
        }
        return predicate.shouldTrace(
                source != null ? source.sourceDefinitionId() : null,
                source != null ? source.sourceScopeId() : null,
                source != null ? source.sourceScopeType() : null,
                source != null ? source.sourceElementId() : null);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Returns all trace entries as an unmodifiable list.
     */
    public List<VariableTraceEntry> getEntries() {
        return List.copyOf(entries);
    }

    /**
     * Returns trace entries grouped by source element ID.
     * Entries with a {@code null} source element ID are grouped under the key {@code null}.
     */
    public Map<String, List<VariableTraceEntry>> byElement() {
        Map<String, List<VariableTraceEntry>> result = new LinkedHashMap<>();
        for (VariableTraceEntry entry : entries) {
            result.computeIfAbsent(entry.sourceElementId(), k -> new ArrayList<>()).add(entry);
        }
        return result;
    }
}
