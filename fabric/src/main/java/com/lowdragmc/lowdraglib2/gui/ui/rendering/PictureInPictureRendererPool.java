package com.lowdragmc.lowdraglib2.gui.ui.rendering;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Pools {@link PictureInPictureRenderer} instances for a single state type, so several states of the
 * same kind submitted in one frame each get a renderer of their own.
 *
 * <p>Vanilla's {@code GuiRenderer} keeps one renderer per state type. When two states of that type
 * arrive in the same frame, the second {@code prepare} rewrites the shared renderer's texture — and
 * on a size change it reallocates it, leaving the first state's already-recorded blit pointing at a
 * texture that no longer exists. Handing each simultaneous state its own renderer avoids both.
 *
 * <p>Reuse is keyed on <em>the renderers used this frame</em>: a renderer is handed out at most once
 * per frame and is only eligible again after {@link #reset()}. Consistency across frames is not a
 * concern — the previous frame's draws have already been executed by the time {@code reset} runs —
 * so a renderer may be reused next frame regardless of the size it last held; vanilla's own
 * {@code prepare} reallocates the texture when the requested size differs.
 *
 * <p>Replaces NeoForge's {@code net.neoforged.neoforge.client.gui.PictureInPictureRendererPool}.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PictureInPictureRendererPool<T extends PictureInPictureRenderState> {
    private final PictureInPictureRendererRegistration<T> factory;
    private final MultiBufferSource.BufferSource buffers;
    private final List<PictureInPictureRenderer<? super T>> renderers = new ArrayList<>();
    private final Set<PictureInPictureRenderer<? super T>> inUseThisFrame =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public PictureInPictureRendererPool(PictureInPictureRendererRegistration<T> factory,
                                        MultiBufferSource.BufferSource buffers) {
        this.factory = factory;
        this.buffers = buffers;
    }

    /**
     * Returns a renderer this frame has not yet handed out, creating one if every pooled renderer is
     * already in use. The caller renders with it before requesting another, so a renderer is only
     * ever in flight for one state at a time.
     */
    @SuppressWarnings("unchecked")
    public PictureInPictureRenderer<? super T> getRenderer(T state) {
        for (var renderer : renderers) {
            if (!inUseThisFrame.contains(renderer)) {
                inUseThisFrame.add(renderer);
                return renderer;
            }
        }
        var created = (PictureInPictureRenderer<? super T>) factory.factory().apply(buffers);
        inUseThisFrame.add(created);
        // A registration may legitimately return the same instance every time: vanilla's constructor
        // only receives instances, not factories, so the pools built there register the fixed
        // instance. Adding it twice would make the pool grow without bound, so an already-pooled
        // renderer is simply shared — the same behaviour vanilla itself has for that type.
        if (!renderers.contains(created)) {
            renderers.add(created);
        }
        return created;
    }

    /**
     * Clears the per-frame bookkeeping so every renderer is eligible again next frame. Called once
     * the frame's picture-in-picture passes have finished.
     */
    public void reset() {
        inUseThisFrame.clear();
    }

    /**
     * Closes all renderers in the pool.
     */
    public void close() {
        for (var renderer : renderers) {
            renderer.close();
        }
        renderers.clear();
        inUseThisFrame.clear();
    }
}
