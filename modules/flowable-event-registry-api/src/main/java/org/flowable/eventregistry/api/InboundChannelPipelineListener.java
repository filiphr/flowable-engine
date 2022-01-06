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

import java.util.Collection;

/**
 * A listener invoked around the {@link InboundEventProcessingPipeline}.
 *
 * @author Filip Hrisafov
 */
public interface InboundChannelPipelineListener {

    /**
     * Method invoked before the pipeline is run
     *
     * @param channelKey the key of the channel that triggered the event
     * @param tenantId the tenant id for which the channel was deployed
     * @param rawEvent the raw event that was received
     */
    default void beforePipelineRun(String channelKey, String tenantId, Object rawEvent) {
    }

    /**
     * Method invoked after the pipeline was run
     *
     * @param channelKey the key of the channel that triggered the event
     * @param tenantId the tenant id for which the channel was deployed
     * @param rawEvent the raw event that was received
     * @param events the events that were created from the pipeline run
     */

    default void afterPipelineRun(String channelKey, String tenantId, Object rawEvent, Collection<EventRegistryEvent> events) {
    }

    /**
     * Method invoked when an exception happens during the run of the pipeline
     *
     * @param channelKey the key of the channel that triggered the event
     * @param tenantId the tenant id for which the channel was deployed
     * @param rawEvent the raw event that was received
     * @param exception the exception that was thrown from the pipeline run
     */

    default void exceptionOnPipelineRun(String channelKey, String tenantId, Object rawEvent, Exception exception) {
    }

}
