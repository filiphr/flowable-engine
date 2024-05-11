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
package org.flowable.entitylink.service.impl.persistence.entity.data.impl.cachematcher;

import java.util.Map;
import java.util.Objects;

import org.flowable.common.engine.impl.db.SingleCachedEntityMatcher;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.entitylink.api.EntityLinkInfo;

/**
 * @author Filip Hrisafov
 */
public class EntityLinkByScopeAndReferenceScopeAndType<EntityImpl extends Entity & EntityLinkInfo> implements SingleCachedEntityMatcher<EntityImpl> {

    @Override
    public boolean isRetained(EntityImpl entity, Object parameter) {
        @SuppressWarnings("unchecked")
        Map<String, String> parameterMap = (Map<String, String>) parameter;
        if (!Objects.equals(entity.getScopeId(), parameterMap.get("scopeId"))) {
            return false;
        }
        if (!Objects.equals(entity.getScopeType(), parameterMap.get("scopeType"))) {
            return false;
        }
        if (!Objects.equals(entity.getReferenceScopeId(), parameterMap.get("referenceScopeId"))) {
            return false;
        }
        if (!Objects.equals(entity.getReferenceScopeType(), parameterMap.get("referenceScopeType"))) {
            return false;
        }
        return Objects.equals(entity.getLinkType(), parameterMap.get("linkType"));
    }

}
