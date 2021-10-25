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
package org.flowable.spring.boot.content;

import javax.sql.DataSource;

import org.flowable.content.spring.SpringContentEngineConfiguration;
import org.flowable.spring.boot.AbstractEngineAutoConfiguration;
import org.flowable.spring.boot.FlowableProperties;
import org.flowable.spring.boot.MainEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineAutoConfiguration;
import org.flowable.spring.boot.ProcessEngineServicesAutoConfiguration;
import org.flowable.spring.boot.app.AppEngineAutoConfiguration;
import org.flowable.spring.boot.app.AppEngineServicesAutoConfiguration;
import org.flowable.spring.boot.condition.ConditionalOnContentEngine;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-Configuration} for the Content Engine
 *
 * @author Filip Hrisafov
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnContentEngine
@EnableConfigurationProperties({
    FlowableProperties.class,
    FlowableContentProperties.class
})
@AutoConfigureAfter({
    AppEngineAutoConfiguration.class,
    ProcessEngineAutoConfiguration.class,
})
@AutoConfigureBefore({
    AppEngineServicesAutoConfiguration.class,
    ProcessEngineServicesAutoConfiguration.class,
})
// The order of the imports is important since they will be scanned by Spring in that order
@Import({
        MainEngineConfiguration.ContentEngineWithMain.class,
        MainEngineConfiguration.ContentEngineWithoutMain.class,
})
public class ContentEngineAutoConfiguration extends AbstractEngineAutoConfiguration<SpringContentEngineConfiguration> {

    protected final FlowableContentProperties contentProperties;

    public ContentEngineAutoConfiguration(FlowableProperties flowableProperties, FlowableContentProperties contentProperties) {
        super(flowableProperties);
        this.contentProperties = contentProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringContentEngineConfiguration contentEngineConfiguration(DataSource dataSource, PlatformTransactionManager platformTransactionManager) {
        SpringContentEngineConfiguration configuration = new SpringContentEngineConfiguration();

        configuration.setTransactionManager(platformTransactionManager);
        configureEngine(configuration, dataSource);

        FlowableContentProperties.Storage storage = contentProperties.getStorage();
        configuration.setContentRootFolder(storage.getRootFolder());
        configuration.setCreateContentRootFolder(storage.getCreateRoot());

        invokeConfigurers(configuration);

        return configuration;
    }
}
