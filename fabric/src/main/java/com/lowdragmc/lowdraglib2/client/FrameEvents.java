package com.lowdragmc.lowdraglib2.client;

import com.lowdragmc.lowdraglib2.LDLib2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fabric's replacement for NeoForge's {@code RenderFrameEvent.Post}: a tiny ordered listener list
 * fired once per rendered frame by
 * {@link com.lowdragmc.lowdraglib2.core.mixins.client.MinecraftFrameHookMixin}.
 *
 * <p>Copy-on-iterate ({@link CopyOnWriteArrayList}) so a listener that subscribes or unsubscribes
 * from inside a callback cannot corrupt the iteration. Listener failures are logged and swallowed:
 * neither the test harness nor an OS window may be able to kill the vanilla render loop.
 *
 * <p>Inert while nothing is subscribed, which is the production case except for the OS window pump.
 */
public final class FrameEvents {

    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    private FrameEvents() {
    }

    /** Subscribes a listener; idempotent, so registering twice still fires it once per frame. */
    public static void subscribe(Runnable listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    /** Fires every listener once, in subscription order. No-ops while nothing is subscribed. */
    public static void fire() {
        if (LISTENERS.isEmpty()) {
            return;
        }
        for (var listener : LISTENERS) {
            try {
                listener.run();
            } catch (Throwable t) {
                LDLib2.LOGGER.error("[frame] listener {} threw", listener, t);
            }
        }
    }
}
