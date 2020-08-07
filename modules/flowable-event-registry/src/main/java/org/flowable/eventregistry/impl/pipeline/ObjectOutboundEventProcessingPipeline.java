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
package org.flowable.eventregistry.impl.pipeline;

import java.util.Collection;

import org.flowable.common.engine.api.FlowableIllegalStateException;
import org.flowable.eventregistry.api.OutboundEventProcessingPipeline;
import org.flowable.eventregistry.api.model.EventPayloadTypes;
import org.flowable.eventregistry.api.runtime.EventInstance;
import org.flowable.eventregistry.api.runtime.EventPayloadInstance;

/**
 * An {@link OutboundEventProcessingPipeline} that will use the single payload instance if the event has only one payload instance,
 * otherwise it will use the payload that is of type {@link EventPayloadTypes#OBJECT}
 *
 * @author Filip Hrisafov
 */
public class ObjectOutboundEventProcessingPipeline implements OutboundEventProcessingPipeline<Object> {

    protected static final ObjectOutboundEventProcessingPipeline INSTANCE = new ObjectOutboundEventProcessingPipeline();

    @Override
    public Object run(EventInstance eventInstance) {
        Collection<EventPayloadInstance> payloadInstances = eventInstance.getPayloadInstances();
        if (payloadInstances.size() == 1) {
            return payloadInstances.iterator().next().getValue();
        } else if (!payloadInstances.isEmpty()) {
            for (EventPayloadInstance payloadInstance : payloadInstances) {
                if (EventPayloadTypes.OBJECT.equals(payloadInstance.getDefinitionType())) {
                    return payloadInstance.getValue();
                }
            }

            throw new FlowableIllegalStateException("Event instance has more than one payload and none of them are of type 'object'");
        }

        return null;
    }
}
