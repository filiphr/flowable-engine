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

import org.flowable.eventregistry.api.EventRegistryEvent;
import org.flowable.eventregistry.api.InboundChannelPipelineListener;
import org.flowable.eventregistry.api.InboundEventProcessingPipeline;

/**
 * @author Filip Hrisafov
 */
public class ListenerInvokingInboundEventProcessingPipeline implements InboundEventProcessingPipeline {

    protected final InboundEventProcessingPipeline delegate;
    protected final String tenantId;
    protected final Collection<InboundChannelPipelineListener> listeners;

    public ListenerInvokingInboundEventProcessingPipeline(InboundEventProcessingPipeline delegate, String tenantId,
            Collection<InboundChannelPipelineListener> listeners) {
        this.delegate = delegate;
        this.tenantId = tenantId;
        this.listeners = listeners;
    }

    public InboundEventProcessingPipeline getDelegate() {
        return delegate;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Collection<InboundChannelPipelineListener> getListeners() {
        return listeners;
    }

    @Override
    public final Collection<EventRegistryEvent> run(String channelKey, String rawEvent) {
        beforePipelineRun(channelKey, rawEvent);
        try {
            Collection<EventRegistryEvent> events = delegate.run(channelKey, rawEvent);
            afterPipelineRun(channelKey, rawEvent, events);
            return events;
        } catch (Exception ex) {
            exceptionOnPipelineRun(channelKey, rawEvent, ex);
            throw ex;
        }
    }

    protected void beforePipelineRun(String channelKey, String rawEvent) {
        if (listeners != null) {
            for (InboundChannelPipelineListener listener : listeners) {
                listener.beforePipelineRun(channelKey, tenantId, rawEvent);
            }
        }
    }

    protected void afterPipelineRun(String channelKey, String rawEvent, Collection<EventRegistryEvent> events) {
        if (listeners != null) {
            for (InboundChannelPipelineListener listener : listeners) {
                listener.afterPipelineRun(channelKey, tenantId, rawEvent, events);
            }
        }
    }

    protected void exceptionOnPipelineRun(String channelKey, String rawEvent, Exception exception) {
        if (listeners != null) {
            for (InboundChannelPipelineListener listener : listeners) {
                listener.exceptionOnPipelineRun(channelKey, tenantId, rawEvent, exception);
            }
        }
    }

}
