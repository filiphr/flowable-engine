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
package org.flowable.variable.service.impl.variabletrace;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.flowable.common.engine.api.variable.VariableTrace;
import org.flowable.common.engine.api.variable.VariableTraceEntry;
import org.flowable.common.engine.api.variable.VariableTraceHandler;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.flowable.variable.service.impl.persistence.entity.VariableTraceEntity;
import org.flowable.variable.service.impl.persistence.entity.VariableTraceEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;

/**
 * A {@link VariableTraceHandler} that persists trace entries to the {@code ACT_HI_VAR_TRACE}
 * database table. Each trace is stored with a unique {@code traceId} that groups all entries
 * from a single command execution.
 * <p>
 * Persistence is done in a separate command (and therefore a separate transaction) to avoid
 * interfering with the business transaction. If persistence fails, a warning is logged but
 * the exception is not propagated.
 *
 * @author Filip Hrisafov
 */
public class DbVariableTraceHandler implements VariableTraceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbVariableTraceHandler.class);

    protected static final int MAX_VALUE_TEXT_LENGTH = 4000;

    protected final CommandExecutor commandExecutor;
    protected final VariableTraceEntityManager variableTraceEntityManager;
    protected final ObjectMapper objectMapper;

    public DbVariableTraceHandler(CommandExecutor commandExecutor,
            VariableTraceEntityManager variableTraceEntityManager,
            ObjectMapper objectMapper) {
        this.commandExecutor = commandExecutor;
        this.variableTraceEntityManager = variableTraceEntityManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(VariableTrace trace) {
        List<VariableTraceEntry> entries = trace.getEntries();
        if (entries.isEmpty()) {
            return;
        }

        try {
            String traceId = UUID.randomUUID().toString();
            commandExecutor.execute(commandContext -> {
                for (VariableTraceEntry entry : entries) {
                    VariableTraceEntity entity = variableTraceEntityManager.create();
                    entity.setTraceId(traceId);
                    entity.setSequence(entry.sequence());
                    entity.setTimestamp(Date.from(entry.timestamp()));
                    entity.setOperationType(entry.operationType().name());
                    entity.setVariableName(entry.variableName());
                    entity.setVariableType(entry.variableType());
                    entity.setValueText(serializeValue(entry.value()));
                    entity.setTransientVariable(entry.transientVariable());
                    entity.setElementId(entry.elementId());
                    entity.setScopeId(entry.scopeId());
                    entity.setScopeType(entry.scopeType());
                    entity.setDefinitionId(entry.definitionId());
                    entity.setVariableScopeId(entry.variableScopeId());
                    entity.setVariableScopeType(entry.variableScopeType());
                    entity.setMappingId(entry.mappingId());
                    variableTraceEntityManager.insert(entity);
                }
                return null;
            });
        } catch (Exception e) {
            LOGGER.warn("Failed to persist variable trace entries", e);
        }
    }

    protected String serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        String text;
        if (value instanceof String s) {
            text = s;
        } else if (value instanceof Number || value instanceof Boolean) {
            text = value.toString();
        } else {
            try {
                text = objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                text = String.valueOf(value);
            }
        }
        if (text.length() > MAX_VALUE_TEXT_LENGTH) {
            return text.substring(0, MAX_VALUE_TEXT_LENGTH);
        }
        return text;
    }
}
