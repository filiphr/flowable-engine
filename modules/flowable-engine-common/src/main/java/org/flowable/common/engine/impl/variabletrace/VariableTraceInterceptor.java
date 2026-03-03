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

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.variable.VariableTrace;
import org.flowable.common.engine.api.variable.VariableTraceHandler;
import org.flowable.common.engine.api.variable.VariableTracePredicate;
import org.flowable.common.engine.impl.interceptor.AbstractCommandInterceptor;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandConfig;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;

/**
 * Command interceptor that automatically binds a {@link VariableTrace} via {@link ScopedValue}
 * for every command execution when engine-level variable tracing is enabled.
 * <p>
 * If a caller has already bound a {@code VariableTrace} (e.g., via a builder's
 * {@code variableTrace()} method), this interceptor respects the existing binding
 * and does not create a new one.
 * <p>
 * After the command completes, non-empty traces are flushed to the configured
 * {@link VariableTraceHandler}.
 */
public class VariableTraceInterceptor extends AbstractCommandInterceptor {

    protected VariableTraceHandler handler;
    protected final VariableTracePredicate predicate;

    public VariableTraceInterceptor(VariableTraceHandler handler, VariableTracePredicate predicate) {
        this.handler = handler;
        this.predicate = predicate;
    }

    public VariableTraceHandler getHandler() {
        return handler;
    }

    public void setHandler(VariableTraceHandler handler) {
        this.handler = handler;
    }

    @Override
    public <T> T execute(CommandConfig config, Command<T> command, CommandExecutor commandExecutor) {
        if (VariableTrace.CURRENT.isBound()) {
            // Caller already bound a trace (via builder or explicit ScopedValue binding).
            // Don't rebind — the caller owns the trace lifecycle.
            return next.execute(config, command, commandExecutor);
        }

        // Engine auto-traces: bind a fresh trace for this command
        VariableTrace trace = new VariableTrace(predicate);
        try {
            T result = ScopedValue.where(VariableTrace.CURRENT, trace)
                    .call(() -> next.execute(config, command, commandExecutor));

            if (!trace.isEmpty() && handler != null) {
                handler.handle(trace);
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FlowableException("Unexpected checked exception during variable-traced command execution", e);
        }
    }
}
