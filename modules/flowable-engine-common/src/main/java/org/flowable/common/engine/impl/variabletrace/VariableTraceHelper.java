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
package org.flowable.common.engine.impl.variabletrace;

import java.util.concurrent.Callable;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.variable.VariableTrace;
import org.flowable.common.engine.api.variable.VariableTraceHandler;
import org.flowable.common.engine.api.variable.VariableTracePredicate;

/**
 * Helper that manages {@link VariableTrace} lifecycle via {@link ScopedValue} binding.
 * Used by service methods and builders to activate variable tracing only where needed,
 * instead of wrapping every command execution.
 * <p>
 * Supports two modes:
 * <ul>
 *     <li><b>Auto-trace</b>: The helper creates a fresh {@link VariableTrace}, binds it,
 *         executes the operation, and flushes non-empty traces to the configured handler.</li>
 *     <li><b>Caller-controlled</b>: The caller provides a {@link VariableTrace} that is bound
 *         for the operation. No handler flush — the caller owns the trace lifecycle.</li>
 * </ul>
 *
 * @author Filip Hrisafov
 */
public class VariableTraceHelper {

    protected VariableTraceHandler handler;
    protected final VariableTracePredicate predicate;

    public VariableTraceHelper(VariableTraceHandler handler, VariableTracePredicate predicate) {
        this.handler = handler;
        this.predicate = predicate;
    }

    /**
     * Executes the callable with engine auto-tracing. Creates a fresh {@link VariableTrace},
     * binds it via {@link ScopedValue}, executes the callable, and flushes to the handler
     * if the trace is non-empty.
     * <p>
     * If a trace is already bound (nested call), the existing trace is reused and no
     * new trace is created or flushed.
     */
    public <T> T executeWithAutoTrace(Callable<T> callable) {
        if (VariableTrace.CURRENT.isBound()) {
            return callUnchecked(callable);
        }
        VariableTrace trace = new VariableTrace(predicate);
        try {
            T result = ScopedValue.where(VariableTrace.CURRENT, trace)
                    .call(() -> callUnchecked(callable));
            if (!trace.isEmpty() && handler != null) {
                handler.handle(trace);
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowableException("Unexpected checked exception during variable-traced execution", e);
        }
    }

    /**
     * Void variant of {@link #executeWithAutoTrace(Callable)}.
     */
    public void runWithAutoTrace(Runnable runnable) {
        executeWithAutoTrace(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Executes the callable with a caller-provided {@link VariableTrace}.
     * The trace is bound via {@link ScopedValue} for the duration of the call.
     * No handler flush — the caller owns the trace.
     * <p>
     * This is a static method because it does not depend on any engine configuration
     * (handler or predicate). Caller-controlled tracing works regardless of whether
     * variable tracing is enabled in the engine.
     */
    public static <T> T executeWithCallerTrace(VariableTrace trace, Callable<T> callable) {
        try {
            return ScopedValue.where(VariableTrace.CURRENT, trace)
                    .call(() -> callUnchecked(callable));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowableException("Unexpected checked exception during variable-traced execution", e);
        }
    }

    /**
     * Void variant of {@link #executeWithCallerTrace(VariableTrace, Callable)}.
     */
    public static void runWithCallerTrace(VariableTrace trace, Runnable runnable) {
        executeWithCallerTrace(trace, () -> {
            runnable.run();
            return null;
        });
    }

    public VariableTraceHandler getHandler() {
        return handler;
    }

    public void setHandler(VariableTraceHandler handler) {
        this.handler = handler;
    }

    public static <T> T callUnchecked(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowableException("Unexpected checked exception during variable-traced execution", e);
        }
    }
}
