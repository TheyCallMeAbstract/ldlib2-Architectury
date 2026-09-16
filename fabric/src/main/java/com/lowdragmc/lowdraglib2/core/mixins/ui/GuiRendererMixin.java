package com.lowdragmc.lowdraglib2.core.mixins.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.lowdragmc.lowdraglib2.client.scene.ScenePIPRenderer;
import com.lowdragmc.lowdraglib2.client.scene.SceneRenderState;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.IGuiRendererExt;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.IPreciseScissor;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.PreciseScissor;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.PictureInPictureRendererPool;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.PictureInPictureRendererRegistration;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.VisualLayerPipRenderer;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.VisualLayerPipState;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.*;

/// Surgical mixin to support visual-layer sub-renderers:
/// (1) make `renderState` swappable so a sub-GuiRenderer can adopt a captured sub-state
/// (2) redirect main render target inside `draw()` to allow writing into an off-target
/// (3) add picture-in-picture renderer pools (vanilla uses one renderer per state type, which
///     causes crashes when multiple states of the same type are submitted in one frame)
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin implements IGuiRendererExt {
    @Shadow @Final @Mutable private GuiRenderState renderState;
    @Shadow @Final private MultiBufferSource.BufferSource bufferSource;
    @Shadow @Final private SubmitNodeCollector submitNodeCollector;
    @Shadow @Final private FeatureRenderDispatcher featureRenderDispatcher;
    @Shadow @Final private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> pictureInPictureRenderers;

    /// Pools for picture-in-picture renderers, keyed by state class.
    /// Added via @Unique because vanilla has {@code pictureInPictureRenderers} (a simple map of
    /// renderer per state type) rather than pools. This field replaces that approach with pooled
    /// renderers that can be reused across frames without interference.
    @Unique
    @Mutable
    private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> pictureInPictureRendererPools;

    /// Pipeline of the draw currently being executed by {@code executeDraw}, captured at
    /// {@code RenderPass.setPipeline} so {@code drawIndexed} can select a mode-correct index buffer.
    @Unique
    private RenderPipeline ldlib2$currentPipeline;

    /// The vanilla quad index buffer/type handed to {@code executeDraw}, captured at
    /// {@code RenderPass.setIndexBuffer}. The bind is deferred until {@code drawIndexed}, where the
    /// exact index count (and therefore the mode-appropriate sequential buffer) is known.
    @Unique
    private GpuBuffer ldlib2$quadIndexBuffer;

    @Unique
    private VertexFormat.IndexType ldlib2$quadIndexType;

    @Override
    public void ldlib2$setRenderState(GuiRenderState state) {
        this.renderState = state;
    }

    @Override
    public MultiBufferSource.BufferSource ldlib2$getBufferSource() {
        return this.bufferSource;
    }

    @Override
    public SubmitNodeCollector ldlib2$getSubmitNodeCollector() {
        return this.submitNodeCollector;
    }

    @Override
    public FeatureRenderDispatcher ldlib2$getFeatureRenderDispatcher() {
        return this.featureRenderDispatcher;
    }

    @Override
    public Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> ldlib2$getPictureInPictureRendererPools() {
        return this.pictureInPictureRendererPools;
    }

    @Override
    public void ldlib2$setPictureInPictureRendererPools(
            Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> pools) {
        this.pictureInPictureRendererPools = pools;
    }

    /// Initialize pools from the vanilla {@code pictureInPictureRenderers} map.
    /// Called at the end of the constructor, after the map is populated.
    @Inject(method = "<init>", at = @At("RETURN"))
    private void ldlib2$initPipelines(GuiRenderState renderState, MultiBufferSource.BufferSource bufferSource,
                                      SubmitNodeCollector submitNodeCollector,
                                      FeatureRenderDispatcher featureRenderDispatcher,
                                      List<PictureInPictureRenderer<?>> renderers, CallbackInfo ci) {
        var pools = new HashMap<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>>();
        for (var renderer : renderers) {
            var stateClass = renderer.getRenderStateClass();
            @SuppressWarnings({"unchecked", "rawtypes"})
            var pool = new PictureInPictureRendererPool(
                    new PictureInPictureRendererRegistration(stateClass, r -> renderer),
                    bufferSource);
            pools.put(stateClass, pool);
        }
        // LDLib's own picture-in-picture kinds. Vanilla's constructor only hands over renderer
        // instances and knows nothing about these, so unlike the kinds above they are registered
        // with a real factory: that is what lets the pool grow a renderer per simultaneous state.
        // This is the Fabric stand-in for NeoForge's RegisterPictureInPictureRenderersEvent.
        pools.put(VisualLayerPipState.class, new PictureInPictureRendererPool<VisualLayerPipState>(
                new PictureInPictureRendererRegistration<>(VisualLayerPipState.class, VisualLayerPipRenderer::new),
                bufferSource));
        pools.put(SceneRenderState.class, new PictureInPictureRendererPool<SceneRenderState>(
                new PictureInPictureRendererRegistration<>(SceneRenderState.class, ScenePIPRenderer::new),
                bufferSource));
        this.pictureInPictureRendererPools = pools;
    }

    /// Redirect PIP rendering to use pools instead of vanilla's single-renderer map.
    /// This prevents crashes when multiple states of the same type are submitted in one frame.
    @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
    private <T extends PictureInPictureRenderState> void ldlib2$redirectPipPrepare(T state, int light, CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        var pool = (PictureInPictureRendererPool<T>) pictureInPictureRendererPools.get(state.getClass());
        if (pool != null) {
            var renderer = pool.getRenderer(state);
            if (renderer != null) {
                renderer.prepare(state, renderState, light);
            }
            ci.cancel();
        }
    }

    /// Reset pool reuse state at the end of each frame.
    @Inject(method = "endFrame", at = @At("HEAD"))
    private void ldlib2$resetPipelines(CallbackInfo ci) {
        if (pictureInPictureRendererPools != null) {
            for (var pool : pictureInPictureRendererPools.values()) {
                pool.reset();
            }
        }
    }

    /// Close pools when the renderer is closed.
    @Inject(method = "close", at = @At("HEAD"))
    private void ldlib2$closePipelines(CallbackInfo ci) {
        if (pictureInPictureRendererPools != null) {
            for (var pool : pictureInPictureRendererPools.values()) {
                pool.close();
            }
        }
    }

    @Redirect(
            method = "draw",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;")
    )
    private RenderTarget ldlib2$redirectRenderTarget(Minecraft mc) {
        RenderTarget override = IGuiRendererExt.ldlib2$peekTargetOverride();
        return override != null ? override : mc.getMainRenderTarget();
    }

    /// Size the orthographic projection to the off-target being drawn into, when there is one.
    @ModifyArgs(
            method = "draw",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Projection;setupOrtho(FFFFZ)V")
    )
    private void ldlib2$overrideOrtho(Args args) {
        var override = IGuiRendererExt.ldlib2$peekOrthoOverride();
        if (override == null) return;
        args.set(2, override.width());
        args.set(3, override.height());
    }

    /// Resolve a clip rectangle against the target actually being drawn into, at that target's
    /// resolution.
    @Inject(method = "enableScissor", at = @At("HEAD"), cancellable = true)
    private void ldlib2$scissorPrecisely(ScreenRectangle rectangle, com.mojang.blaze3d.systems.RenderPass renderPass, CallbackInfo ci) {
        var override = IGuiRendererExt.ldlib2$peekOrthoOverride();
        if (override == null && IPreciseScissor.of(rectangle) == null) return;

        int targetWidth, targetHeight;
        double scaleX, scaleY;
        if (override != null) {
            targetWidth = override.framebufferWidth();
            targetHeight = override.framebufferHeight();
            scaleX = targetWidth / (double) override.width();
            scaleY = targetHeight / (double) override.height();
        } else {
            var window = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState;
            targetWidth = window.width;
            targetHeight = window.height;
            scaleX = scaleY = window.guiScale;
        }

        var box = PreciseScissor.quantize(IPreciseScissor.clipOf(rectangle),
                scaleX, scaleY, targetWidth, targetHeight);
        renderPass.enableScissor(box.x(), box.y(), box.width(), box.height());
        ci.cancel();
    }

    /// Two clip rectangles that round to the same integer box are not the same clip.
    @ModifyReturnValue(method = "scissorChanged", at = @At("RETURN"))
    private boolean ldlib2$preciseScissorChanged(boolean original,
                                                 @Nullable ScreenRectangle newScissor,
                                                 @Nullable ScreenRectangle oldScissor) {
        if (original || newScissor == null || oldScissor == null) return original;
        return !Objects.equals(IPreciseScissor.of(newScissor), IPreciseScissor.of(oldScissor));
    }

    /// Keep elements that share an integer box but not a precise clip from interleaving.
    @ModifyArg(
            method = "prepare",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;sortElements(Ljava/util/Comparator;)V")
    )
    private Comparator<GuiElementRenderState> ldlib2$groupByPreciseScissor(Comparator<GuiElementRenderState> original) {
        return original.thenComparing(GuiElementRenderState::scissorArea, IPreciseScissor.COMPARATOR);
    }

    /// Cache the latest fog buffer passed to {@code render()} so sub-renderers
    /// (visual layers) can reuse it without needing access to the private
    /// {@code GameRenderer.fogRenderer}.
    @Inject(method = "render", at = @At("HEAD"))
    private void ldlib2$captureFogBuffer(GpuBufferSlice fogBuffer, CallbackInfo ci) {
        IGuiRendererExt.ldlib2$setLastFogBuffer(fogBuffer);
    }

    /// Capture the pipeline of the draw being executed so the deferred index-buffer bind in
    /// {@link #ldlib2$drawIndexed} can pick the matching vertex format mode.
    @Redirect(
            method = "executeDraw",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V")
    )
    private void ldlib2$capturePipeline(RenderPass pass, RenderPipeline pipeline) {
        this.ldlib2$currentPipeline = pipeline;
        pass.setPipeline(pipeline);
    }

    /// Stash the vanilla quad index buffer/type instead of binding it. The bind is deferred to
    /// {@link #ldlib2$drawIndexed}, where the exact index count is available.
    @Redirect(
            method = "executeDraw",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V")
    )
    private void ldlib2$captureIndexBuffer(RenderPass pass, GpuBuffer indexBuffer, VertexFormat.IndexType indexType) {
        this.ldlib2$quadIndexBuffer = indexBuffer;
        this.ldlib2$quadIndexType = indexType;
    }

    /// Bind a mode-correct index buffer before drawing. Vanilla hardcodes the QUADS pattern; every
    /// other vertex format mode needs the sequential generator, matching NeoForge's patch.
    @Redirect(
            method = "executeDraw",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V")
    )
    private void ldlib2$drawIndexed(RenderPass pass, int baseVertex, int firstIndex, int indexCount, int instanceCount) {
        var pipeline = this.ldlib2$currentPipeline;
        var mode = pipeline != null ? pipeline.getVertexFormatMode() : VertexFormat.Mode.QUADS;
        if (mode == VertexFormat.Mode.QUADS) {
            pass.setIndexBuffer(this.ldlib2$quadIndexBuffer, this.ldlib2$quadIndexType);
        } else {
            var sequential = RenderSystem.getSequentialBuffer(mode);
            pass.setIndexBuffer(sequential.getBuffer(indexCount), sequential.type());
        }
        pass.drawIndexed(baseVertex, firstIndex, indexCount, instanceCount);
    }

    /// Force connected-primitive pipelines to flush each element into its own mesh. The source
    /// condition is {@code !textureSetup.equals(previousTextureSetup)}, so returning
    /// {@code original && !connectedPrimitives} yields {@code !equals || connectedPrimitives},
    /// matching NeoForge's added disjunct.
    @ModifyExpressionValue(
            method = "addElementToMesh",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/TextureSetup;equals(Ljava/lang/Object;)Z")
    )
    private boolean ldlib2$flushConnectedPrimitives(boolean original, GuiElementRenderState elementState) {
        if (!original) return false;
        return !elementState.pipeline().getVertexFormatMode().connectedPrimitives;
    }
}
