package com.github.snice.spring.pf4j.listener;

import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

public interface PluginAppListener {

    void onPluginStarted(ContextStartedEvent event);

    void onPluginStopped(ContextStoppedEvent event);
}
