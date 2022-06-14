package com.github.snice.spring.pf4j.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@FunctionalInterface
public interface PluginApplicationListener<E extends ApplicationEvent> extends ApplicationListener<E> {
}
