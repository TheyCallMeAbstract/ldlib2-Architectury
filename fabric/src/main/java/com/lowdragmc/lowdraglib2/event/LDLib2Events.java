package com.lowdragmc.lowdraglib2.event;

import com.lowdragmc.lowdraglib2.LDLib2;

/**
 * Static facade over the single LDLib2 {@link EventBus}, mirroring NeoForge's
 * {@code NeoForge.EVENT_BUS} entry points on Fabric.
 *
 * <p>Mods (and LDLib2 itself) register {@link SubscribeEvent} listeners through
 * {@link #register(Object)} / {@link #register(Class)} and post events through {@link #game()}.
 * The bus is created eagerly, so posting is always safe once the class is loaded.
 *
 * <p>{@link #init()} is invoked once from {@code LDLib2Fabric.onInitialize()}, alongside the
 * other LDLib2 initialisation hooks.
 *
 * @see Event
 * @see SubscribeEvent
 * @see EventBus
 */
public final class LDLib2Events {

    private static final EventBus GAME = new EventBus();

    private LDLib2Events() {
    }

    /** The shared gameplay event bus, equivalent to NeoForge's {@code NeoForge.EVENT_BUS}. */
    public static EventBus game() {
        return GAME;
    }

    /** Registers a listener object; see {@link EventBus#register(Object)}. */
    public static void register(Object listener) {
        GAME.register(listener);
    }

    /** Registers a static listener class; see {@link EventBus#register(Class)}. */
    public static void register(Class<?> listenerClass) {
        GAME.register(listenerClass);
    }

    /**
     * Initialises the Fabric event subsystem. Idempotent and safe to call more than once.
     *
     * <p>LDLib2 currently owns no {@link SubscribeEvent} listeners of its own: its server and
     * world callbacks are hosted by Architectury events in {@code FabricCommonListeners}. This
     * hook remains the place where internal listeners would be registered as the library grows.
     */
    public static void init() {
        LDLib2.LOGGER.info("[event] {} event bus initialised", LDLib2.NAME);
    }
}
