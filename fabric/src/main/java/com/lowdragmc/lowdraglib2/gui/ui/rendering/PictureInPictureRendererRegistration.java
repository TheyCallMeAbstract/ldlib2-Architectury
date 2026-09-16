package com.lowdragmc.lowdraglib2.gui.ui.rendering;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import java.util.function.Function;

/**
 * Holds the state class and renderer factory for a picture-in-picture renderer.
 * Replaces NeoForge's {@code PictureInPictureRendererRegistration}.
 */
public record PictureInPictureRendererRegistration<T extends PictureInPictureRenderState>(
        Class<T> stateClass,
        Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<? super T>> factory
) {
}
