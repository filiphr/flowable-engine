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
package org.flowable.spring.boot.dmn;

import org.flowable.dmn.api.DmnDecisionService;
import org.flowable.dmn.api.DmnHistoryService;
import org.flowable.dmn.api.DmnManagementService;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.spring.boot.FlowableProperties;
import org.flowable.spring.boot.ProcessEngineServicesAutoConfiguration;
import org.flowable.spring.boot.app.AppEngineServicesAutoConfiguration;
import org.flowable.spring.boot.condition.ConditionalOnDmnEngine;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration} for the DMN Engine
 *
 * @author Filip Hrisafov
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDmnEngine
@EnableConfigurationProperties({
    FlowableProperties.class,
    FlowableDmnProperties.class
})
@AutoConfigureAfter({
    DmnEngineAutoConfiguration.class,
    AppEngineServicesAutoConfiguration.class,
    ProcessEngineServicesAutoConfiguration.class
})
public class DmnEngineServicesAutoConfiguration {

    @Bean
    public DmnManagementService dmnManagementService(DmnEngine dmnEngine) {
        return dmnEngine.getDmnManagementService();
    }

    @Bean
    public DmnRepositoryService dmnRepositoryService(DmnEngine dmnEngine) {
        return dmnEngine.getDmnRepositoryService();
    }

    @Bean
    public DmnDecisionService dmnRuleService(DmnEngine dmnEngine) {
        return dmnEngine.getDmnDecisionService();
    }

    @Bean
    public DmnHistoryService dmnHistoryService(DmnEngine dmnEngine) {
        return dmnEngine.getDmnHistoryService();
    }
}
