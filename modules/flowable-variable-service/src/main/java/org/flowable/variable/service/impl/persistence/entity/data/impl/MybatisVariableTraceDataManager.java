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
package org.flowable.variable.service.impl.persistence.entity.data.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.impl.cfg.IdGenerator;
import org.flowable.common.engine.impl.db.AbstractDataManager;
import org.flowable.variable.service.VariableServiceConfiguration;
import org.flowable.variable.service.impl.persistence.entity.VariableTraceEntity;
import org.flowable.variable.service.impl.persistence.entity.VariableTraceEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.data.VariableTraceDataManager;

/**
 * @author Filip Hrisafov
 */
public class MybatisVariableTraceDataManager extends AbstractDataManager<VariableTraceEntity> implements VariableTraceDataManager {

    protected VariableServiceConfiguration variableServiceConfiguration;

    public MybatisVariableTraceDataManager(VariableServiceConfiguration variableServiceConfiguration) {
        this.variableServiceConfiguration = variableServiceConfiguration;
    }

    @Override
    public Class<? extends VariableTraceEntity> getManagedEntityClass() {
        return VariableTraceEntityImpl.class;
    }

    @Override
    public VariableTraceEntity create() {
        return new VariableTraceEntityImpl();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<VariableTraceEntity> findByTraceId(String traceId) {
        return getDbSqlSession().selectList("selectVariableTraceByTraceId", traceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<VariableTraceEntity> findByVariableScopeId(String variableScopeId, String variableScopeType) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("variableScopeId", variableScopeId);
        params.put("variableScopeType", variableScopeType);
        return getDbSqlSession().selectListWithRawParameter("selectVariableTraceByVariableScopeId", params);
    }

    @Override
    public void deleteByTraceId(String traceId) {
        getDbSqlSession().delete("deleteVariableTraceByTraceId", traceId, getManagedEntityClass());
    }

    @Override
    public void deleteByVariableScopeIdAndType(String variableScopeId, String variableScopeType) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("variableScopeId", variableScopeId);
        params.put("variableScopeType", variableScopeType);
        getDbSqlSession().delete("deleteVariableTraceByVariableScopeIdAndType", params, getManagedEntityClass());
    }

    @Override
    protected IdGenerator getIdGenerator() {
        return variableServiceConfiguration.getIdGenerator();
    }
}
