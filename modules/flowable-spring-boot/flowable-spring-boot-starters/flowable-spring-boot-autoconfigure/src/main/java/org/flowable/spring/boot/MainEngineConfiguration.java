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
package org.flowable.spring.boot;

import java.util.List;

import org.flowable.app.engine.AppEngine;
import org.flowable.app.spring.AppEngineFactoryBean;
import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.cmmn.engine.CmmnEngine;
import org.flowable.cmmn.engine.CmmnEngines;
import org.flowable.cmmn.spring.CmmnEngineFactoryBean;
import org.flowable.cmmn.spring.SpringCmmnEngineConfiguration;
import org.flowable.cmmn.spring.configurator.SpringCmmnEngineConfigurator;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.content.engine.ContentEngine;
import org.flowable.content.engine.ContentEngines;
import org.flowable.content.spring.ContentEngineFactoryBean;
import org.flowable.content.spring.SpringContentEngineConfiguration;
import org.flowable.content.spring.configurator.SpringContentEngineConfigurator;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.engine.DmnEngines;
import org.flowable.dmn.spring.DmnEngineFactoryBean;
import org.flowable.dmn.spring.SpringDmnEngineConfiguration;
import org.flowable.dmn.spring.configurator.SpringDmnEngineConfigurator;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.spring.configurator.SpringProcessEngineConfigurator;
import org.flowable.eventregistry.impl.EventRegistryEngine;
import org.flowable.eventregistry.impl.EventRegistryEngines;
import org.flowable.eventregistry.spring.EventRegistryFactoryBean;
import org.flowable.eventregistry.spring.SpringEventRegistryEngineConfiguration;
import org.flowable.eventregistry.spring.configurator.SpringEventRegistryConfigurator;
import org.flowable.form.engine.FormEngine;
import org.flowable.form.engine.FormEngines;
import org.flowable.form.spring.FormEngineFactoryBean;
import org.flowable.form.spring.SpringFormEngineConfiguration;
import org.flowable.form.spring.configurator.SpringFormEngineConfigurator;
import org.flowable.idm.engine.IdmEngine;
import org.flowable.idm.engine.IdmEngines;
import org.flowable.idm.spring.IdmEngineFactoryBean;
import org.flowable.idm.spring.SpringIdmEngineConfiguration;
import org.flowable.idm.spring.configurator.SpringIdmEngineConfigurator;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main engine configurations imported by the different engine auto configurations.
 * This class is there to allow for correct import order and Spring Bean configuration scanning.
 * We can't have the inner classes in the main auto configurations because they will be scanned in the way they are ordered in the bytecode.
 * The order in the byte code is not deterministic. Therefore, we do it through {@link org.springframework.context.annotation.Import Import}.
 * This is similar how Spring Boot does for the {@link org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration DataSourceAutoConfiguration}.
 *
 * @author Filip Hrisafov
 */
public class MainEngineConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringAppEngineConfiguration.class)
    public static class AppEngineAsMain {

        @Bean(name = "flowableAppEngine")
        public AppEngineFactoryBean appEngine(SpringAppEngineConfiguration configuration,
                ObjectProvider<List<MainEngineConfigurationConfigurer<? super SpringAppEngineConfiguration>>> mainConfigurers) throws Exception {
            AppEngineFactoryBean appEngineFactoryBean = new AppEngineFactoryBean();
            appEngineFactoryBean.setAppEngineConfiguration(configuration);

            List<MainEngineConfigurationConfigurer<? super SpringAppEngineConfiguration>> configurers = mainConfigurers.getIfAvailable();

            if (configurers != null) {
                for (MainEngineConfigurationConfigurer<? super SpringAppEngineConfiguration> configurer : configurers) {
                    configurer.configure(configuration);
                }
            }

            return appEngineFactoryBean;
        }

        @Bean
        public MainEngineHolder<AppEngine> appMainEngineHolder(AppEngine appEngine) {
            return () -> appEngine;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringProcessEngineConfiguration.class)
    public static class ProcessEngineWithMain {

        @Bean
        public MainEngineConfiguratorHolder<SpringProcessEngineConfigurator> processMainEngineConfigurator(SpringProcessEngineConfiguration configuration) {
            SpringProcessEngineConfigurator configurator = new SpringProcessEngineConfigurator();
            configurator.setProcessEngineConfiguration(configuration);

            configuration.setDisableIdmEngine(true);
            configuration.setDisableEventRegistry(true);

            return () -> configurator;
        }

        @Bean
        public MainEngineConfigurationConfigurer<AbstractEngineConfiguration> processMainEngineConfigurer(
                MainEngineConfiguratorHolder<SpringProcessEngineConfigurator> configurator) {
            return engineConfiguration -> {
                engineConfiguration.addConfigurator(configurator.getEngineConfigurator());
            };
        }

        @Bean
        public ProcessEngine processEngine(@SuppressWarnings("unused") MainEngineHolder<?> mainEngine) {
            // The main engine needs to be injected, as otherwise it won't be initialized, which means that the ProcessEngine is not initialized yet
            if (!ProcessEngines.isInitialized()) {
                throw new IllegalStateException("BPMN engine has not been initialized");
            }
            return ProcessEngines.getDefaultProcessEngine();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringProcessEngineConfiguration.class)
    public static class ProcessEngineWithoutMain {

        @Bean
        public ProcessEngineFactoryBean processEngine(SpringProcessEngineConfiguration configuration,
                ObjectProvider<List<MainEngineConfigurationConfigurer<? super SpringProcessEngineConfiguration>>> mainConfigurers) throws Exception {
            ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
            processEngineFactoryBean.setProcessEngineConfiguration(configuration);

            List<MainEngineConfigurationConfigurer<? super SpringProcessEngineConfiguration>> configurers = mainConfigurers.getIfAvailable();

            if (configurers != null) {
                for (MainEngineConfigurationConfigurer<? super SpringProcessEngineConfiguration> configurer : configurers) {
                    configurer.configure(configuration);
                }
            }
            return processEngineFactoryBean;
        }

        @Bean
        public MainEngineHolder<ProcessEngine> processMainEngineHolder(ProcessEngine processEngine) {
            return () -> processEngine;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringCmmnEngineConfiguration.class)
    public static class CmmnEngineWithMain {

        @Bean
        public MainEngineConfiguratorHolder<SpringCmmnEngineConfigurator> cmmnMainEngineConfigurator(SpringCmmnEngineConfiguration configuration) {
            SpringCmmnEngineConfigurator configurator = new SpringCmmnEngineConfigurator();
            configurator.setCmmnEngineConfiguration(configuration);

            configuration.setDisableIdmEngine(true);
            configuration.setDisableEventRegistry(true);

            return () -> configurator;
        }

        @Bean
        public MainEngineConfigurationConfigurer<AbstractEngineConfiguration> cmmnMainEngineConfigurer(
                MainEngineConfiguratorHolder<SpringCmmnEngineConfigurator> configuratorHolder) {
            return engineConfiguration -> {
                engineConfiguration.addConfigurator(configuratorHolder.getEngineConfigurator());
            };
        }

        @Bean
        public CmmnEngine cmmnEngine(@SuppressWarnings("unused") MainEngineHolder<?> mainEngine) {
            // The main engine needs to be injected, as otherwise it won't be initialized, which means that the CmmnEngine is not initialized yet
            if (!CmmnEngines.isInitialized()) {
                throw new IllegalStateException("CMMN engine has not been initialized");
            }
            return CmmnEngines.getDefaultCmmnEngine();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringCmmnEngineConfiguration.class)
    public static class CmmnEngineWithoutMain {

        @Bean
        public CmmnEngineFactoryBean cmmnEngine(SpringCmmnEngineConfiguration configuration) throws Exception {
            CmmnEngineFactoryBean factoryBean = new CmmnEngineFactoryBean();
            factoryBean.setCmmnEngineConfiguration(configuration);

            return factoryBean;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringContentEngineConfiguration.class)
    public static class ContentEngineWithMain {

        @Bean
        public MainEngineConfiguratorHolder<SpringContentEngineConfigurator> contentMainEngineConfigurator(SpringContentEngineConfiguration configuration) {
            SpringContentEngineConfigurator configurator = new SpringContentEngineConfigurator();
            configurator.setContentEngineConfiguration(configuration);

            return () -> configurator;
        }

        @Bean
        public MainEngineConfigurationConfigurer<AbstractEngineConfiguration> contentMainEngineConfigurer(
                MainEngineConfiguratorHolder<SpringContentEngineConfigurator> configurator) {
            return engineConfiguration -> {
                engineConfiguration.addConfigurator(configurator.getEngineConfigurator());
            };
        }

        @Bean
        public ContentEngine contentEngine(@SuppressWarnings("unused") MainEngineHolder<?> mainEngine) {
            // The main engine needs to be injected, as otherwise it won't be initialized, which means that the ContentEngine is not initialized yet
            if (!ContentEngines.isInitialized()) {
                throw new IllegalStateException("Content engine has not been initialized");
            }
            return ContentEngines.getDefaultContentEngine();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringContentEngineConfiguration.class)
    public static class ContentEngineWithoutMain {

        @Bean
        public ContentEngineFactoryBean contentEngine(SpringContentEngineConfiguration configuration) throws Exception {
            ContentEngineFactoryBean factoryBean = new ContentEngineFactoryBean();
            factoryBean.setContentEngineConfiguration(configuration);

            return factoryBean;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringDmnEngineConfiguration.class)
    public static class DmnEngineWithMain {

        @Bean
        public MainEngineConfiguratorHolder<SpringDmnEngineConfigurator> dmnMainEngineConfigurator(SpringDmnEngineConfiguration configuration) {
            SpringDmnEngineConfigurator configurator = new SpringDmnEngineConfigurator();
            configurator.setDmnEngineConfiguration(configuration);

            return () -> configurator;
        }

        @Bean
        public MainEngineConfigurationConfigurer<AbstractEngineConfiguration> dmnMainEngineConfigurer(
                MainEngineConfiguratorHolder<SpringDmnEngineConfigurator> configurator) {
            return engineConfiguration -> {
                engineConfiguration.addConfigurator(configurator.getEngineConfigurator());
            };
        }

        @Bean
        public DmnEngine dmnEngine(@SuppressWarnings("unused") MainEngineHolder<?> mainEngine) {
            // The main engine needs to be injected, as otherwise it won't be initialized, which means that the DmnEngine is not initialized yet
            if (!DmnEngines.isInitialized()) {
                throw new IllegalStateException("DMN engine has not been initialized");
            }
            return DmnEngines.getDefaultDmnEngine();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringDmnEngineConfiguration.class)
    public static class DmnEngineWithoutMain {

        @Bean
        public DmnEngineFactoryBean dmnEngine(SpringDmnEngineConfiguration configuration) throws Exception {
            DmnEngineFactoryBean factoryBean = new DmnEngineFactoryBean();
            factoryBean.setDmnEngineConfiguration(configuration);

            return factoryBean;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringEventRegistryEngineConfiguration.class)
    public static class EventRegistryEngineWithMain {

        @Bean
        public MainEngineConfiguratorHolder<SpringEventRegistryConfigurator> eventRegistryMainEngineConfigurator(
                SpringEventRegistryEngineConfiguration configuration) {
            SpringEventRegistryConfigurator configurator = new SpringEventRegistryConfigurator();
            configurator.setEventEngineConfiguration(configuration);

            return () -> configurator;
        }

        @Bean
        public MainEngineConfigurationConfigurer<AbstractEngineConfiguration> eventRegistryMainEngineConfigurer(
                MainEngineConfiguratorHolder<SpringEventRegistryConfigurator> configurator) {
            return engineConfiguration -> {
                engineConfiguration.setEventRegistryConfigurator(configurator.getEngineConfigurator());
            };
        }

        @Bean
        public EventRegistryEngine eventRegistryEngine(@SuppressWarnings("unused") MainEngineHolder<?> mainEngine) {
            // The main engine needs to be injected, as otherwise it won't be initialized, which means that the EventRegistryEngine is not initialized yet
            if (!EventRegistryEngines.isInitialized()) {
                throw new IllegalStateException("Event registry has not been initialized");
            }
            return EventRegistryEngines.getDefaultEventRegistryEngine();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringEventRegistryEngineConfiguration.class)
    public static class EventRegistryEngineWithoutMain {

        @Bean
        public EventRegistryFactoryBean eventRegistryEngine(SpringEventRegistryEngineConfiguration configuration) throws Exception {
            EventRegistryFactoryBean factoryBean = new EventRegistryFactoryBean();
            factoryBean.setEventEngineConfiguration(configuration);

            return factoryBean;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringFormEngineConfiguration.class)
    public static class FormEngineWithMain {

        @Bean
        public MainEngineConfiguratorHolder<SpringFormEngineConfigurator> formMainEngineConfigurator(SpringFormEngineConfiguration configuration) {
            SpringFormEngineConfigurator configurator = new SpringFormEngineConfigurator();
            configurator.setFormEngineConfiguration(configuration);

            return () -> configurator;
        }

        @Bean
        public MainEngineConfigurationConfigurer<AbstractEngineConfiguration> formMainEngineConfigurer(
                MainEngineConfiguratorHolder<SpringFormEngineConfigurator> configurator) {
            return engineConfiguration -> {
                engineConfiguration.addConfigurator(configurator.getEngineConfigurator());
            };
        }

        @Bean
        public FormEngine formEngine(@SuppressWarnings("unused") MainEngineHolder<?> mainEngine) {
            // The main engine needs to be injected, as otherwise it won't be initialized, which means that the FormEngine is not initialized yet
            if (!FormEngines.isInitialized()) {
                throw new IllegalStateException("Form engine has not been initialized");
            }
            return FormEngines.getDefaultFormEngine();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringFormEngineConfiguration.class)
    public static class FormEngineWithoutMain {

        @Bean
        public FormEngineFactoryBean formEngine(SpringFormEngineConfiguration configuration) throws Exception {
            FormEngineFactoryBean factoryBean = new FormEngineFactoryBean();
            factoryBean.setFormEngineConfiguration(configuration);

            return factoryBean;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringIdmEngineConfiguration.class)
    public static class IdmEngineWithMain {

        @Bean
        public MainEngineConfiguratorHolder<SpringIdmEngineConfigurator> idmMainEngineConfigurator(SpringIdmEngineConfiguration configuration) {
            SpringIdmEngineConfigurator configurator = new SpringIdmEngineConfigurator();
            configurator.setIdmEngineConfiguration(configuration);

            return () -> configurator;
        }

        @Bean
        public MainEngineConfigurationConfigurer<AbstractEngineConfiguration> idmMainEngineConfigurer(
                MainEngineConfiguratorHolder<SpringIdmEngineConfigurator> configurator) {
            return engineConfiguration -> {
                engineConfiguration.setIdmEngineConfigurator(configurator.getEngineConfigurator());
            };
        }

        @Bean
        public IdmEngine idmEngine(@SuppressWarnings("unused") MainEngineHolder<?> mainEngine) {
            // The main engine needs to be injected, as otherwise it won't be initialized, which means that the IdmEngine is not initialized yet
            if (!IdmEngines.isInitialized()) {
                throw new IllegalStateException("Idm engine has not been initialized");
            }
            return IdmEngines.getDefaultIdmEngine();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(MainEngineHolder.class)
    @ConditionalOnClass(SpringIdmEngineConfiguration.class)
    public static class IdmEngineWithoutMain {

        @Bean
        public IdmEngineFactoryBean idmEngine(SpringIdmEngineConfiguration configuration) throws Exception {
            IdmEngineFactoryBean factoryBean = new IdmEngineFactoryBean();
            factoryBean.setIdmEngineConfiguration(configuration);

            return factoryBean;
        }

    }
}
