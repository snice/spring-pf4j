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

import com.github.snice.spring.pf4j.listener.PluginApplicationListener;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;

/**
 * @author Decebal Suiu
 */
public abstract class SpringPlugin extends Plugin {

    private ApplicationContext applicationContext;

    public SpringPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    public final ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = createApplicationContext();
        }

        return applicationContext;
    }

    @Override
    public void stop() {
        // close applicationContext
        if ((applicationContext != null) && (applicationContext instanceof ConfigurableApplicationContext)) {
            ((ConfigurableApplicationContext) applicationContext).close();
        }
        applicationContext = null;
    }

    protected ApplicationContext createApplicationContext() {
        ApplicationContext appContext = ((SpringPluginManager) getWrapper().getPluginManager()).getApplicationContext();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setClassLoader(getWrapper().getPluginClassLoader());
        applicationContext.setParent(appContext);
        applicationContext.register(componentClasses());
        Collection<PluginApplicationListener> pluginListeners = appContext.getBeansOfType(PluginApplicationListener.class).values();
        for (PluginApplicationListener listener : pluginListeners)
            applicationContext.addApplicationListener(listener);
        applicationContext.refresh();
        return applicationContext;
    }

    public abstract Class[] componentClasses();

    public abstract String basePackage();

}
