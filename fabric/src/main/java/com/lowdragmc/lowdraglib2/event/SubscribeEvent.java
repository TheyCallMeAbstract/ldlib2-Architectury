package com.lowdragmc.lowdraglib2.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an {@link EventBus} listener.
 *
 * <p>The annotated method must declare exactly one parameter — the {@link Event} subtype it
 * consumes. Both static and instance methods are supported: static methods are discovered
 * when the declaring class is registered, instance methods when a listener object is
 * registered.
 *
 * <p>Fabric's counterpart to NeoForge's {@code net.neoforged.bus.api.SubscribeEvent}; the
 * simple name resolves to the loader-appropriate annotation on each platform.
 *
 * @see EventBus
 * @see LDLib2Events
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
}
