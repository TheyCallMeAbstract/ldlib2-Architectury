package com.lowdragmc.lowdraglib2.event;

import com.lowdragmc.lowdraglib2.LDLib2;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fabric's loader-appropriate replacement for NeoForge's {@code IEventBus}, restricted to the
 * one capability the documentation depends on: annotation-driven listener dispatch.
 *
 * <p>A listener is registered with {@link #register(Object)} (a listener object, supporting
 * static and instance methods) or {@link #register(Class)} (a listener class, supporting static
 * methods only). Registration discovers and caches every method annotated with
 * {@link SubscribeEvent} that declares exactly one parameter. {@link #post(Event)} then invokes
 * each cached method whose parameter type is assignable from the posted event's runtime class,
 * so a handler declared against a supertype still receives the subtype.
 *
 * <p>Dispatch is deliberately tolerant, mirroring the existing {@code FrameEvents} listener
 * pattern: a listener that throws is logged via {@link LDLib2#LOGGER} and skipped, never allowed
 * to abort the remaining listeners or the vanilla menu-open path that posts the event. Posting
 * while nothing is registered is a no-op.
 *
 * <p>Deliberately excluded (not used by any documented snippet): listener priority, event
 * cancellation, generic event hierarchies and annotation-driven auto-discovery.
 */
public final class EventBus {

    private record Listener(@Nullable Object target, Method method, Class<?> eventType) {
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registers the annotated listener methods of {@code listener}. A {@link Class} argument is
     * treated as a static listener class, matching NeoForge's
     * {@code NeoForge.EVENT_BUS.register(Class)}; any other object registers both its static and
     * instance listener methods. Registering the same listener twice is idempotent.
     */
    public void register(Object listener) {
        if (listener == null) return;
        if (listener instanceof Class<?> listenerClass) {
            register(listenerClass);
        } else {
            discover(listener, listener.getClass(), false);
        }
    }

    /** Registers the static annotated listener methods declared by {@code listenerClass}. */
    public void register(Class<?> listenerClass) {
        if (listenerClass != null) {
            discover(null, listenerClass, true);
        }
    }

    /**
     * Dispatches {@code event} to every registered listener whose parameter type accepts it.
     * No-op while nothing is registered. Any listener failure is logged and swallowed.
     */
    public void post(Event event) {
        if (event == null || listeners.isEmpty()) return;
        for (var listener : listeners) {
            if (!listener.eventType().isAssignableFrom(event.getClass())) continue;
            try {
                listener.method().invoke(listener.target(), event);
            } catch (Throwable throwable) {
                LDLib2.LOGGER.error("[event] listener {} failed handling {}",
                        listener.method(), event.getClass().getName(), throwable);
            }
        }
    }

    /** True while no listener is registered; posting is then a no-op. */
    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    private void discover(@Nullable Object target, Class<?> listenerClass, boolean staticOnly) {
        // Overridden methods would otherwise be discovered once per class in the hierarchy.
        Set<String> seen = new HashSet<>();
        for (Class<?> current = listenerClass; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(SubscribeEvent.class)) continue;
                if (!seen.add(signatureOf(method))) continue;

                boolean isStatic = Modifier.isStatic(method.getModifiers());
                if (staticOnly && !isStatic) {
                    LDLib2.LOGGER.warn("[event] ignoring non-static @SubscribeEvent method {} on class registration {}",
                            method, listenerClass.getName());
                    continue;
                }
                if (method.getParameterCount() != 1) {
                    LDLib2.LOGGER.warn("[event] ignoring @SubscribeEvent method {}: expected exactly one parameter", method);
                    continue;
                }
                if (!method.canAccess(isStatic ? null : target)) {
                    try {
                        method.setAccessible(true);
                    } catch (Throwable throwable) {
                        LDLib2.LOGGER.error("[event] cannot access @SubscribeEvent method {}", method, throwable);
                        continue;
                    }
                }
                var listener = new Listener(isStatic ? null : target, method, method.getParameterTypes()[0]);
                if (!listeners.contains(listener)) {
                    listeners.add(listener);
                }
            }
        }
    }

    private static String signatureOf(Method method) {
        return method.getName() + Arrays.toString(method.getParameterTypes());
    }
}
