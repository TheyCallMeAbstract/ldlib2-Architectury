package com.lowdragmc.lowdraglib2.editor.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Event posted when editor resources are built.
 * <p>
 * On NeoForge this extended {@code net.neoforged.bus.api.Event} and was posted via
 * {@code ModLoader.postEvent}. On Fabric there is no mod event bus, so listeners
 * are registered through {@link #registerListener(Consumer)} and posted through
 * {@link #post(EditorResourceEvent)}.
 */
public abstract class EditorResourceEvent {
    public final ResourceInstance<?> resourceInstance;

    public EditorResourceEvent(ResourceInstance<?> resourceInstance) {
        this.resourceInstance = resourceInstance;
    }

    private static final List<Consumer<EditorResourceEvent>> LISTENERS = new ArrayList<>();

    public static void registerListener(Consumer<EditorResourceEvent> listener) {
        LISTENERS.add(listener);
    }

    public static void post(EditorResourceEvent event) {
        for (var listener : LISTENERS) {
            listener.accept(event);
        }
    }

    public static class LoadBuiltin extends EditorResourceEvent {
        public <T> LoadBuiltin(ResourceInstance<T> resourceInstance) {
            super(resourceInstance);
        }
    }
}
