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

import java.util.List;

import org.flowable.common.engine.impl.persistence.entity.AbstractServiceEngineEntityManager;
import org.flowable.variable.service.VariableServiceConfiguration;
import org.flowable.variable.service.impl.persistence.entity.data.VariableTraceDataManager;

/**
 * @author Filip Hrisafov
 */
public class VariableTraceEntityManagerImpl
        extends AbstractServiceEngineEntityManager<VariableServiceConfiguration, VariableTraceEntity, VariableTraceDataManager>
        implements VariableTraceEntityManager {

    public VariableTraceEntityManagerImpl(VariableServiceConfiguration variableServiceConfiguration,
            VariableTraceDataManager variableTraceDataManager) {
        super(variableServiceConfiguration, variableServiceConfiguration.getEngineName(), variableTraceDataManager);
    }

    @Override
    public List<VariableTraceEntity> findByTraceId(String traceId) {
        return dataManager.findByTraceId(traceId);
    }

    @Override
    public List<VariableTraceEntity> findByVariableScopeId(String variableScopeId, String variableScopeType) {
        return dataManager.findByVariableScopeId(variableScopeId, variableScopeType);
    }

    @Override
    public List<VariableTraceEntity> findByScopeIdOrVariableScopeId(String scopeId, String scopeType) {
        return dataManager.findByScopeIdOrVariableScopeId(scopeId, scopeType);
    }

    @Override
    public void deleteByTraceId(String traceId) {
        dataManager.deleteByTraceId(traceId);
    }

    @Override
    public void deleteByVariableScopeIdAndType(String variableScopeId, String variableScopeType) {
        dataManager.deleteByVariableScopeIdAndType(variableScopeId, variableScopeType);
    }
}
