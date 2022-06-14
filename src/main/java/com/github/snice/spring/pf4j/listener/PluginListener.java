package com.github.snice.spring.pf4j.listener;

import org.springframework.context.event.ContextRefreshedEvent;

@FunctionalInterface
public interface PluginListener {
    void onPluginEvent(ContextRefreshedEvent event);
}
