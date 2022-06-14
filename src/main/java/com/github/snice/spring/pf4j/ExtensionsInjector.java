/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.snice.spring.pf4j;

import com.github.snice.spring.pf4j.inject.ISpringInjector;
import com.google.common.reflect.ClassPath;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Decebal Suiu
 */
public class ExtensionsInjector {

    private static final Logger log = LoggerFactory.getLogger(ExtensionsInjector.class);

    protected final SpringPluginManager springPluginManager;
    protected final ApplicationContext applicationContext;
    protected final AbstractAutowireCapableBeanFactory beanFactory;

    protected final Collection<ISpringInjector> springInjectors;

    public ExtensionsInjector(SpringPluginManager springPluginManager, ApplicationContext applicationContext) {
        this.springPluginManager = springPluginManager;
        this.applicationContext = applicationContext;
        this.beanFactory = (AbstractAutowireCapableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        this.springInjectors = applicationContext.getBeansOfType(ISpringInjector.class).values();
    }

    /**
     * 注入插件组件
     *
     * @param pluginId 插件ID
     */
    public void injectExtensions(String pluginId) {
        PluginWrapper pluginWrapper = springPluginManager.getPlugin(pluginId);
        ClassLoader classLoader = pluginWrapper.getPluginClassLoader();
        try {
            if (pluginWrapper.getPlugin() instanceof SpringPlugin) {
                String basePackage = ((SpringPlugin) pluginWrapper.getPlugin()).basePackage();
                ClassPath classPath = ClassPath.from(classLoader);
                List<Class> classes =
                        classPath.getTopLevelClassesRecursive(basePackage).stream().filter(it -> it.load().getClassLoader() != getClass().getClassLoader()).map(it -> it.load()).collect(Collectors.toList());
                for (Class c : classes) {
                    List<ISpringInjector> list = springInjectors.stream().filter(it -> it.isSupport(c)).collect(Collectors.toList());
                    for (ISpringInjector injector : list) injector.register(c);
                }
            } else {
                Set<String> extensionClassNames = springPluginManager.getExtensionClassNames(pluginId);
                for (String extensionClassName : extensionClassNames) {
                    log.debug("Register extension '{}' as bean", extensionClassName);
                    Class<?> c = classLoader.loadClass(extensionClassName);
                    List<ISpringInjector> list = springInjectors.stream().filter(it -> it.isSupport(c)).collect(Collectors.toList());
                    for (ISpringInjector injector : list) injector.register(c);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void uninjectExtensions(String pluginId) {
        PluginWrapper pluginWrapper = springPluginManager.getPlugin(pluginId);
        ClassLoader classLoader = pluginWrapper.getPluginClassLoader();
        try {
            if (pluginWrapper.getPlugin() instanceof SpringPlugin) {
                String basePackage = ((SpringPlugin) pluginWrapper.getPlugin()).basePackage();
                ClassPath classPath = ClassPath.from(classLoader);
                List<Class> classes =
                        classPath.getTopLevelClassesRecursive(basePackage).stream().filter(it -> it.load().getClassLoader() != getClass().getClassLoader()).map(it -> it.load()).collect(Collectors.toList());
                for (Class c : classes) {
                    List<ISpringInjector> list = springInjectors.stream().filter(it -> it.isSupport(c)).collect(Collectors.toList());
                    for (ISpringInjector injector : list) injector.unregister(c);
                }
            } else {
                Set<String> extensionClassNames = springPluginManager.getExtensionClassNames(pluginId);
                for (String extensionClassName : extensionClassNames) {
                    Class<?> c = classLoader.loadClass(extensionClassName);
                    List<ISpringInjector> list = springInjectors.stream().filter(it -> it.isSupport(c)).collect(Collectors.toList());
                    for (ISpringInjector injector : list) injector.unregister(c);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
