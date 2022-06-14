package com.github.snice.spring.pf4j.listener;

import org.springframework.context.ApplicationEvent;

@FunctionalInterface
public interface PluginListener<E extends ApplicationEvent> {
    void onPluginEvent(E event);
}
