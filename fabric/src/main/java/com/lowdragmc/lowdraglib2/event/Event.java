package com.lowdragmc.lowdraglib2.event;

/**
 * Base class for every event dispatched through an {@link EventBus}.
 *
 * <p>On Fabric this is the loader-appropriate counterpart to NeoForge's
 * {@code net.neoforged.bus.api.Event}. LDLib2 event objects extend this type so a handler
 * written against the loader-neutral LDLib2 API has a single supertype on each platform.
 *
 * @see SubscribeEvent
 * @see EventBus
 */
public abstract class Event {
}
