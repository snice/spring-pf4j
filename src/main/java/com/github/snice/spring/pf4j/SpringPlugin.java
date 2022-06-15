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

import com.github.snice.spring.pf4j.listener.PluginAppListener;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

import java.util.Collection;

/**
 * @author Decebal Suiu
 */
public abstract class SpringPlugin extends Plugin {

    private ApplicationContext applicationContext;

    private ApplicationListener<ContextStartedEvent> startedEventApplicationListener = event -> {
        Collection<PluginAppListener> pluginAppListeners = getApplicationContext().getParent().getBeansOfType(PluginAppListener.class).values();
        for (PluginAppListener listener : pluginAppListeners) {
            listener.onPluginStarted(event);
        }
    };

    private ApplicationListener<ContextStoppedEvent> stoppedEventApplicationListener = event -> {
        Collection<PluginAppListener> pluginAppListeners = getApplicationContext().getParent().getBeansOfType(PluginAppListener.class).values();
        for (PluginAppListener listener : pluginAppListeners) {
            listener.onPluginStopped(event);
        }
    };

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
            ((ConfigurableApplicationContext) applicationContext).stop();
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
        applicationContext.addApplicationListener(startedEventApplicationListener);
        applicationContext.addApplicationListener(stoppedEventApplicationListener);
        applicationContext.refresh();
        new Thread(() -> {
            try {
                Thread.sleep(delayStart());
                applicationContext.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        return applicationContext;
    }

    public long delayStart() {
        return 500L;
    }

    public abstract Class[] componentClasses();

    public abstract String basePackage();

}
