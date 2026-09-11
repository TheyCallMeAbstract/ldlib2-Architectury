package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.resources.DelegatingOps;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Reflection-based accessor utility that replaces mixin accessor interface casts
 * which fail across JPMS module boundaries in NeoForge 26.1.
 *
 * <p>When a mixin in the {@code ldlib2} module injects an interface into a class in the
 * {@code minecraft} module, the JVM module system silently prevents the interface from
 * being linked, causing {@code instanceof} and cast checks to fail at runtime. This class
 * uses cached {@link MethodHandle} and {@link Field} reflections to access private
 * fields/methods directly, bypassing the module boundary restriction.
 */
public final class AccessorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("LDLib2/AccessorHelper");

    private AccessorHelper() {}

    // ── GameRenderer.guiRenderer ──────────────────────────────────────────────
    private static final Field GR_GUI_RENDERER;
    static {
        Field f = null;
        try {
            f = net.minecraft.client.renderer.GameRenderer.class.getDeclaredField("guiRenderer");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect GameRenderer.guiRenderer", e);
        }
        GR_GUI_RENDERER = f;
    }

    /**
     * Accesses the private {@code guiRenderer} field of {@link net.minecraft.client.renderer.GameRenderer}.
     */
    public static GuiRenderer getGuiRenderer(net.minecraft.client.renderer.GameRenderer gameRenderer) {
        try {
            return (GuiRenderer) GR_GUI_RENDERER.get(gameRenderer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access GameRenderer.guiRenderer", e);
        }
    }

    // ── BufferBuilder.beginElement(VertexFormatElement) ───────────────────────
    private static final MethodHandle BB_BEGIN_ELEMENT;
    static {
        MethodHandle h = null;
        try {
            Method m = BufferBuilder.class.getDeclaredMethod("beginElement", VertexFormatElement.class);
            m.setAccessible(true);
            h = MethodHandles.lookup().unreflect(m);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect BufferBuilder.beginElement", e);
        }
        BB_BEGIN_ELEMENT = h;
    }

    /**
     * Invokes the private {@code beginElement} method on {@link BufferBuilder},
     * returning the native memory address where vertex data for the given element should be written.
     */
    public static long beginElement(BufferBuilder bufferBuilder, VertexFormatElement element) {
        try {
            return (long) BB_BEGIN_ELEMENT.invokeExact(bufferBuilder, element);
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Failed to invoke BufferBuilder.beginElement", e);
        }
    }

    // ── GuiRenderer.renderState ───────────────────────────────────────────────
    private static final Field GR_RENDER_STATE;
    private static final Field GR_RENDER_STATE_SETTER;
    static {
        Field getter = null;
        Field setter = null;
        try {
            getter = GuiRenderer.class.getDeclaredField("renderState");
            getter.setAccessible(true);
            setter = getter; // same field, just needs setAccessible
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect GuiRenderer.renderState", e);
        }
        GR_RENDER_STATE = getter;
        GR_RENDER_STATE_SETTER = setter;
    }

    /**
     * Accesses the private {@code renderState} field of {@link GuiRenderer}.
     */
    public static GuiRenderState getRenderState(GuiRenderer renderer) {
        try {
            return (GuiRenderState) GR_RENDER_STATE.get(renderer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access GuiRenderer.renderState", e);
        }
    }

    /**
     * Sets the private {@code renderState} field of {@link GuiRenderer}.
     */
    public static void setRenderState(GuiRenderer renderer, GuiRenderState state) {
        try {
            GR_RENDER_STATE_SETTER.set(renderer, state);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to set GuiRenderer.renderState", e);
        }
    }

    // ── GuiRenderer.bufferSource ──────────────────────────────────────────────
    private static final Field GR_BUFFER_SOURCE;
    static {
        Field f = null;
        try {
            f = GuiRenderer.class.getDeclaredField("bufferSource");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect GuiRenderer.bufferSource", e);
        }
        GR_BUFFER_SOURCE = f;
    }

    /**
     * Accesses the private {@code bufferSource} field of {@link GuiRenderer}.
     */
    @SuppressWarnings("unchecked")
    public static MultiBufferSource.BufferSource getBufferSource(GuiRenderer renderer) {
        try {
            return (MultiBufferSource.BufferSource) GR_BUFFER_SOURCE.get(renderer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access GuiRenderer.bufferSource", e);
        }
    }

    // ── GuiRenderer.submitNodeCollector ───────────────────────────────────────
    private static final Field GR_SUBMIT_COLLECTOR;
    static {
        Field f = null;
        try {
            f = GuiRenderer.class.getDeclaredField("submitNodeCollector");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect GuiRenderer.submitNodeCollector", e);
        }
        GR_SUBMIT_COLLECTOR = f;
    }

    /**
     * Accesses the private {@code submitNodeCollector} field of {@link GuiRenderer}.
     */
    public static SubmitNodeCollector getSubmitNodeCollector(GuiRenderer renderer) {
        try {
            return (SubmitNodeCollector) GR_SUBMIT_COLLECTOR.get(renderer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access GuiRenderer.submitNodeCollector", e);
        }
    }

    // ── GuiRenderer.featureRenderDispatcher ───────────────────────────────────
    private static final Field GR_FEATURE_DISPATCHER;
    static {
        Field f = null;
        try {
            f = GuiRenderer.class.getDeclaredField("featureRenderDispatcher");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect GuiRenderer.featureRenderDispatcher", e);
        }
        GR_FEATURE_DISPATCHER = f;
    }

    /**
     * Accesses the private {@code featureRenderDispatcher} field of {@link GuiRenderer}.
     */
    public static FeatureRenderDispatcher getFeatureRenderDispatcher(GuiRenderer renderer) {
        try {
            return (FeatureRenderDispatcher) GR_FEATURE_DISPATCHER.get(renderer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access GuiRenderer.featureRenderDispatcher", e);
        }
    }

    // ── GuiRenderer.pictureInPictureRendererPools ─────────────────────────────
    private static final Field GR_PIP_POOLS;
    static {
        Field f = null;
        try {
            f = GuiRenderer.class.getDeclaredField("pictureInPictureRendererPools");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect GuiRenderer.pictureInPictureRendererPools", e);
        }
        GR_PIP_POOLS = f;
    }

    /**
     * Accesses the private {@code pictureInPictureRendererPools} field of {@link GuiRenderer}.
     */
    @SuppressWarnings("unchecked")
    public static Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> getPictureInPictureRendererPools(GuiRenderer renderer) {
        try {
            return (Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>>) GR_PIP_POOLS.get(renderer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access GuiRenderer.pictureInPictureRendererPools", e);
        }
    }

    /**
     * Sets the private {@code pictureInPictureRendererPools} field of {@link GuiRenderer}.
     */
    @SuppressWarnings("unchecked")
    public static void setPictureInPictureRendererPools(GuiRenderer renderer, Map<? extends Class<? extends PictureInPictureRenderState>, ? extends PictureInPictureRendererPool<?>> pools) {
        try {
            GR_PIP_POOLS.set(renderer, pools);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to set GuiRenderer.pictureInPictureRendererPools", e);
        }
    }

    // ── PictureInPictureRendererPool.factory ──────────────────────────────────
    private static final Field POOL_FACTORY;
    static {
        Field f = null;
        try {
            f = PictureInPictureRendererPool.class.getDeclaredField("factory");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect PictureInPictureRendererPool.factory", e);
        }
        POOL_FACTORY = f;
    }

    /**
     * Accesses the private {@code factory} field of {@link PictureInPictureRendererPool}.
     */
    @SuppressWarnings("unchecked")
    public static PictureInPictureRendererRegistration<?> getPoolFactory(PictureInPictureRendererPool<?> pool) {
        try {
            return (PictureInPictureRendererRegistration<?>) POOL_FACTORY.get(pool);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access PictureInPictureRendererPool.factory", e);
        }
    }

    // ── PictureInPictureRenderer.textureView ──────────────────────────────────
    private static final Field PIP_TEXTURE_VIEW;
    static {
        Field f = null;
        try {
            f = PictureInPictureRenderer.class.getDeclaredField("textureView");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect PictureInPictureRenderer.textureView", e);
        }
        PIP_TEXTURE_VIEW = f;
    }

    /**
     * Accesses the private {@code textureView} field of {@link PictureInPictureRenderer}.
     */
    @SuppressWarnings("unchecked")
    public static GpuTextureView getTextureView(PictureInPictureRenderer<?> renderer) {
        try {
            return (GpuTextureView) PIP_TEXTURE_VIEW.get(renderer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access PictureInPictureRenderer.textureView", e);
        }
    }

    // ── MeshData.getIndexBuffer / setIndexBuffer ──────────────────────────────
    private static final Field MESH_INDEX_BUFFER;
    static {
        Field f = null;
        try {
            f = MeshData.class.getDeclaredField("indexBuffer");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect MeshData.indexBuffer", e);
        }
        MESH_INDEX_BUFFER = f;
    }

    /**
     * Accesses the private {@code indexBuffer} field of {@link MeshData}.
     */
    @SuppressWarnings("unchecked")
    public static ByteBufferBuilder.Result getIndexBuffer(MeshData meshData) {
        try {
            return (ByteBufferBuilder.Result) MESH_INDEX_BUFFER.get(meshData);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access MeshData.indexBuffer", e);
        }
    }

    /**
     * Sets the private {@code indexBuffer} field of {@link MeshData}.
     */
    @SuppressWarnings("unchecked")
    public static void setIndexBuffer(MeshData meshData, ByteBufferBuilder.Result indexBuffer) {
        try {
            MESH_INDEX_BUFFER.set(meshData, indexBuffer);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to set MeshData.indexBuffer", e);
        }
    }

    // ── DelegatingOps.getDelegate ─────────────────────────────────────────────
    private static final Field DELEGATING_OPS_DELEGATE;
    static {
        Field f = null;
        try {
            f = DelegatingOps.class.getDeclaredField("delegate");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect DelegatingOps.delegate", e);
        }
        DELEGATING_OPS_DELEGATE = f;
    }

    @SuppressWarnings("unchecked")
    public static <T> DynamicOps<T> getDelegate(DelegatingOps<T> delegatingOps) {
        try {
            return (DynamicOps<T>) DELEGATING_OPS_DELEGATE.get(delegatingOps);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access DelegatingOps.delegate", e);
        }
    }

    // ── Minecraft.clientTickCount ─────────────────────────────────────────────
    private static final Field MC_CLIENT_TICK_COUNT;
    static {
        Field f = null;
        try {
            f = Minecraft.class.getDeclaredField("clientTickCount");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect Minecraft.clientTickCount", e);
        }
        MC_CLIENT_TICK_COUNT = f;
    }

    public static long getClientTickCount(Minecraft mc) {
        try {
            return MC_CLIENT_TICK_COUNT.getLong(mc);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to access Minecraft.clientTickCount", e);
        }
    }

    // ── MouseHandler.mouseGrabbed setter ──────────────────────────────────────
    private static final Field MH_MOUSE_GRABBED;
    static {
        Field f = null;
        try {
            f = MouseHandler.class.getDeclaredField("mouseGrabbed");
            f.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[AccessorHelper] Failed to reflect MouseHandler.mouseGrabbed", e);
        }
        MH_MOUSE_GRABBED = f;
    }

    public static void setMouseGrabbed(MouseHandler mouseHandler, boolean grabbed) {
        try {
            MH_MOUSE_GRABBED.setBoolean(mouseHandler, grabbed);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to set MouseHandler.mouseGrabbed", e);
        }
    }
}
