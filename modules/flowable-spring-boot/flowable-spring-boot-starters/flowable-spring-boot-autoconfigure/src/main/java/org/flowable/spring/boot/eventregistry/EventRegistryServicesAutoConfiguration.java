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
package org.flowable.spring.boot.eventregistry;

import org.flowable.eventregistry.api.EventManagementService;
import org.flowable.eventregistry.api.EventRegistry;
import org.flowable.eventregistry.api.EventRepositoryService;
import org.flowable.eventregistry.impl.EventRegistryEngine;
import org.flowable.spring.boot.FlowableProperties;
import org.flowable.spring.boot.ProcessEngineServicesAutoConfiguration;
import org.flowable.spring.boot.app.AppEngineServicesAutoConfiguration;
import org.flowable.spring.boot.condition.ConditionalOnEventRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for the event registry.
 *
 * @author Filip Hrisafov
 * @author Javier Casal
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnEventRegistry
@EnableConfigurationProperties({
    FlowableProperties.class,
    FlowableEventRegistryProperties.class
})
@AutoConfigureAfter({
    EventRegistryAutoConfiguration.class,
    AppEngineServicesAutoConfiguration.class,
    ProcessEngineServicesAutoConfiguration.class
})
public class EventRegistryServicesAutoConfiguration {

    @Bean
    public EventRepositoryService eventRepositoryService(EventRegistryEngine eventRegistryEngine) {
        return eventRegistryEngine.getEventRepositoryService();
    }
    
    @Bean
    public EventManagementService eventManagementService(EventRegistryEngine eventRegistryEngine) {
        return eventRegistryEngine.getEventManagementService();
    }

    @Bean
    public EventRegistry eventRegistry(EventRegistryEngine eventRegistryEngine) {
        return eventRegistryEngine.getEventRegistry();
    }
}

