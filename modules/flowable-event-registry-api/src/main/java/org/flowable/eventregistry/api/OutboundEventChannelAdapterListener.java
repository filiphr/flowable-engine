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
package org.flowable.eventregistry.api;

/**
 * A listener invoked around the {@link OutboundEventChannelAdapter}.
 *
 * @author Filip Hrisafov
 */
public interface OutboundEventChannelAdapterListener {

    /**
     * Method invoked before the raw event is sent to the channel adapter.
     *
     * @param channelKey the key of the channel that triggered the event
     * @param tenantId the tenant id for which the channel was deployed
     * @param rawEvent the raw event that will be sent out
     */
    default void beforeSendEvent(String channelKey, String tenantId, Object rawEvent) {
    }

    /**
     * Method invoked after the raw event is sent to the channel adapter.
     *
     * @param channelKey the key of the channel that triggered the event
     * @param tenantId the tenant id for which the channel was deployed
     * @param rawEvent the raw event that will be sent out
     */

    default void afterSendEvent(String channelKey, String tenantId, Object rawEvent) {
    }

    /**
     * Method invoked when an exception happens during the sending of the event through the adapter
     *
     * @param channelKey the key of the channel that triggered the event
     * @param tenantId the tenant id for which the channel was deployed
     * @param rawEvent the raw event that will be sent out
     * @param exception the exception that was thrown from sending
     */
    default void exceptionOnSendEvent(String channelKey, String tenantId, Object rawEvent, Exception exception) {
    }

}
