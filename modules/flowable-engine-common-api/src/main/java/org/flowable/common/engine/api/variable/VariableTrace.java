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

    /**
     * When bound, all trace entries recorded within the scope will be tagged with this mapping ID.
     * Used during in/out parameter mapping to correlate the READ(s) and CREATE that belong to the
     * same {@code IOParameter}.
     */
    public static final ScopedValue<String> CURRENT_MAPPING_ID = ScopedValue.newInstance();

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
            String variableScopeId, String variableScopeType,
            String variableName, String variableType, Object value, boolean transientVariable) {

        if (!shouldTrace(source)) {
            return;
        }
        String mappingId = CURRENT_MAPPING_ID.isBound() ? CURRENT_MAPPING_ID.get() : null;
        entries.add(new VariableTraceEntry(
                sequenceCounter.getAndIncrement(),
                Instant.now(),
                source != null ? source.elementId() : null,
                source != null ? source.scopeId() : null,
                source != null ? source.scopeType() : null,
                source != null ? source.definitionId() : null,
                variableScopeId,
                variableScopeType,
                variableName,
                variableType,
                transientVariable ? null : value,
                VariableTraceOperationType.READ,
                transientVariable,
                mappingId));
    }

    /**
     * Records a variable write operation (create, update, or delete).
     */
    public void recordWrite(VariableTraceSourceContext source,
            String variableScopeId, String variableScopeType,
            String variableName, String variableType, Object value,
            VariableTraceOperationType operationType, boolean transientVariable) {

        if (!shouldTrace(source)) {
            return;
        }
        String mappingId = CURRENT_MAPPING_ID.isBound() ? CURRENT_MAPPING_ID.get() : null;
        entries.add(new VariableTraceEntry(
                sequenceCounter.getAndIncrement(),
                Instant.now(),
                source != null ? source.elementId() : null,
                source != null ? source.scopeId() : null,
                source != null ? source.scopeType() : null,
                source != null ? source.definitionId() : null,
                variableScopeId,
                variableScopeType,
                variableName,
                variableType,
                transientVariable ? null : value,
                operationType,
                transientVariable,
                mappingId));
    }

    protected boolean shouldTrace(VariableTraceSourceContext source) {
        if (predicate == null) {
            return true;
        }
        return predicate.shouldTrace(
                source != null ? source.definitionId() : null,
                source != null ? source.scopeId() : null,
                source != null ? source.scopeType() : null,
                source != null ? source.elementId() : null);
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
     * Returns trace entries grouped by element ID.
     * Entries with a {@code null} element ID are grouped under the key {@code null}.
     */
    public Map<String, List<VariableTraceEntry>> byElement() {
        Map<String, List<VariableTraceEntry>> result = new LinkedHashMap<>();
        for (VariableTraceEntry entry : entries) {
            result.computeIfAbsent(entry.elementId(), k -> new ArrayList<>()).add(entry);
        }
        return result;
    }
}
