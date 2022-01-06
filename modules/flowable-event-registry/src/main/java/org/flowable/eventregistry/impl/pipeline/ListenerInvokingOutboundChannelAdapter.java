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

import org.flowable.eventregistry.api.OutboundEventChannelAdapter;
import org.flowable.eventregistry.api.OutboundEventChannelAdapterListener;
import org.flowable.eventregistry.model.OutboundChannelModel;

/**
 * @author Filip Hrisafov
 */
public class ListenerInvokingOutboundChannelAdapter<T> implements OutboundEventChannelAdapter<T> {

    protected final OutboundEventChannelAdapter<T> delegate;
    protected final OutboundChannelModel channelModel;
    protected final String tenantId;
    protected final Collection<OutboundEventChannelAdapterListener> listeners;

    public ListenerInvokingOutboundChannelAdapter(OutboundEventChannelAdapter<T> delegate, OutboundChannelModel channelModel,
            String tenantId, Collection<OutboundEventChannelAdapterListener> listeners) {
        this.delegate = delegate;
        this.channelModel = channelModel;
        this.tenantId = tenantId;
        this.listeners = listeners;
    }

    public OutboundEventChannelAdapter<T> getDelegate() {
        return delegate;
    }

    public OutboundChannelModel getChannelModel() {
        return channelModel;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Collection<OutboundEventChannelAdapterListener> getListeners() {
        return listeners;
    }

    @Override
    public void sendEvent(T rawEvent) {
        beforeSendEvent(rawEvent);
        try {
            delegate.sendEvent(rawEvent);
            afterSendEvent(rawEvent);
        } catch (Exception ex) {
            exceptionOnSendEvent(rawEvent, ex);
        }
    }

    protected void beforeSendEvent(T rawEvent) {
        for (OutboundEventChannelAdapterListener listener : listeners) {
            listener.beforeSendEvent(channelModel.getKey(), tenantId, rawEvent);
        }
    }

    protected void afterSendEvent(T rawEvent) {
        for (OutboundEventChannelAdapterListener listener : listeners) {
            listener.afterSendEvent(channelModel.getKey(), tenantId, rawEvent);
        }
    }

    protected void exceptionOnSendEvent(T rawEvent, Exception ex) {
        for (OutboundEventChannelAdapterListener listener : listeners) {
            listener.exceptionOnSendEvent(channelModel.getKey(), tenantId, rawEvent, ex);
        }

    }

}
