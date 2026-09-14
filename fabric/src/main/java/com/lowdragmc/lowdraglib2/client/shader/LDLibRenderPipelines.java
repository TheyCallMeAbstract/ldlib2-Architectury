package com.lowdragmc.lowdraglib2.client.shader;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.client.renderer.RenderPipelines.*;

public class LDLibRenderPipelines {
    public static final RenderPipeline GUI_TRIANGLE = RenderPipeline.builder(GUI_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .withLocation(LDLib2.id("pipeline/gui_triangle")).build();

    public static final RenderPipeline POSITION_COLOR_NO_DEPTH = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .withLocation(LDLib2.id("pipeline/position_color_no_depth"))
            .build();

    public static final RenderPipeline BLOCK_OVERLAY = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/block_overlay"))
            .build();

    public static final RenderPipeline NO_DEPTH_LINES = RenderPipeline.builder(LINES_SNIPPET)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withLocation(LDLib2.id("pipeline/no_depth_lines"))
            .build();

    public static final RenderPipeline GRAPH_WIRE = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position_tex_color")
            .withFragmentShader(LDLib2.id("core/graph_wire"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .withLocation(LDLib2.id("pipeline/graph_wire"))
            .build();

    public static final RenderPipeline ROUNDED_RECT = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/rounded_rect"))
            .withFragmentShader(LDLib2.id("core/rounded_rect"))
            .withVertexFormat(LDLibShaders.ROUNDED_RECT_FORMAT, VertexFormat.Mode.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .withLocation(LDLib2.id("pipeline/rounded_rect"))
            .build();

    public static final RenderPipeline HSB = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/hsb_block"))
            .withFragmentShader(LDLib2.id("core/hsb_block"))
            .withVertexFormat(LDLibShaders.HSB_VERTEX_FORMAT, VertexFormat.Mode.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .withLocation(LDLib2.id("pipeline/hsb"))
            .build();

    /**
     * Multiplies destination color and alpha by mask sample's factor. Used by visual-layer
     * mask baking: after a UI subtree is rendered into an off-target, draw the mask
     * with this pipeline to punch the mask shape into the off-target while preserving
     * premultiplied-alpha semantics for the final GUI_TEXTURED_PREMULTIPLIED_ALPHA blit.
     * Blend: (ZERO, SRC_ALPHA, ZERO, SRC_ALPHA) -> dst.rgba *= src.alpha.
     */
    public static final RenderPipeline MASK_ALPHA_MULTIPLY = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/mask_alpha_multiply"))
            .withFragmentShader(LDLib2.id("core/mask_alpha_multiply"))
            .withSampler("Sampler0")
            .withColorTargetState(new ColorTargetState(new BlendFunction(
                    SourceFactor.ZERO, DestFactor.SRC_ALPHA,
                    SourceFactor.ZERO, DestFactor.SRC_ALPHA)))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/mask_alpha_multiply"))
            .build();

    public static final RenderPipeline STRIP_LINES = RenderPipeline.builder(GUI_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .withLocation(LDLib2.id("pipeline/strip_lines"))
            .build();

    public static final RenderPipeline SDF_TEXT_GUI = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/sdf_text"))
            .withFragmentShader(LDLib2.id("core/sdf_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/sdf_text_gui"))
            .build();

    public static final RenderPipeline SDF_TEXT = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/sdf_text"))
            .withFragmentShader(LDLib2.id("core/sdf_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/sdf_text"))
            .build();

    public static final RenderPipeline SDF_TEXT_POLYGON_OFFSET = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/sdf_text"))
            .withFragmentShader(LDLib2.id("core/sdf_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true, -1.0F, -10.0F))
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/sdf_text_polygon_offset"))
            .build();

    public static final RenderPipeline SDF_TEXT_SEE_THROUGH = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/sdf_text"))
            .withFragmentShader(LDLib2.id("core/sdf_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/sdf_text_see_through"))
            .build();

    public static final RenderPipeline RASTER_TEXT_GUI = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/raster_text"))
            .withFragmentShader(LDLib2.id("core/raster_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/raster_text_gui"))
            .build();

    public static final RenderPipeline RASTER_TEXT = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/raster_text"))
            .withFragmentShader(LDLib2.id("core/raster_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/raster_text"))
            .build();

    public static final RenderPipeline RASTER_TEXT_POLYGON_OFFSET = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/raster_text"))
            .withFragmentShader(LDLib2.id("core/raster_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true, -1.0F, -10.0F))
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/raster_text_polygon_offset"))
            .build();

    public static final RenderPipeline RASTER_TEXT_SEE_THROUGH = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
            .withVertexShader(LDLib2.id("core/raster_text"))
            .withFragmentShader(LDLib2.id("core/raster_text"))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexFormat(LDLibShaders.SDF_TEXT_FORMAT, VertexFormat.Mode.QUADS)
            .withLocation(LDLib2.id("pipeline/raster_text_see_through"))
            .build();

    /**
     * Registers all LDLib2 render pipelines into the vanilla {@link RenderPipelines#PIPELINES_BY_LOCATION} map.
     *
     * <p><b>Why this looks different from the NeoForge version:</b>
     * On NeoForge, pipeline registration goes through {@code RegisterRenderPipelinesEvent},
     * which fires during mod loading and calls {@code RenderPipelines.register()} internally.
     * On Fabric there is no equivalent event — but {@code RenderPipelines.register()} is a
     * public vanilla static method that does exactly the same thing: it puts the pipeline
     * into the {@code PIPELINES_BY_LOCATION} map so the renderer can look it up by location.
     * We call it directly here because the Architectury principle is to write shared code
     * against vanilla APIs whenever possible, and this is a vanilla API.
     *
     * <p><b>When to call this:</b>
     * Must be called on the client during mod initialization (e.g. from {@code ClientModInitializer}).
     * On NeoForge this is triggered by {@code RegisterRenderPipelinesEvent}; on Fabric we call
     * it explicitly from {@code ExampleModFabricClient.onInitializeClient()}.
     *
     * <p><b>Testing caveat:</b>
     * If you test these pipelines in a headless environment (e.g. Xvfb with no GPU), the
     * shader programs referenced by each pipeline ({@code core/rounded_rect}, {@code core/hsb_block},
     * etc.) may fail to compile silently or throw errors that are hard to distinguish from
     * other rendering issues. When debugging pipeline-related rendering problems:
     * <ul>
     *   <li>Check the game log for shader compilation errors near startup</li>
     *   <li>Verify {@code RenderPipelines.PIPELINES_BY_LOCATION.containsKey(LDLib2.id("pipeline/..."))}
     *       returns {@code true} after registration</li>
     *   <li>On NeoForge, the event fires at a specific point in mod loading; on Fabric we fire
     *       during {@code onInitializeClient()}, which is earlier. If any pipeline depends on
     *       resources loaded later (e.g. from a resource pack), it may need to be re-registered
     *       or deferred — but all current pipelines use built-in shaders so this is not an issue.</li>
     * </ul>
     */
    public static void register() {
        var LOGGER = LoggerFactory.getLogger("LDLibRenderPipelines");
        LOGGER.info("=== Registering LDLib2 render pipelines ===");

        RenderPipelines.register(GUI_TRIANGLE);
        RenderPipelines.register(POSITION_COLOR_NO_DEPTH);
        RenderPipelines.register(BLOCK_OVERLAY);
        RenderPipelines.register(NO_DEPTH_LINES);
        RenderPipelines.register(GRAPH_WIRE);
        RenderPipelines.register(ROUNDED_RECT);
        LOGGER.info("Registered ROUNDED_RECT pipeline with format stride={}",
                LDLibShaders.ROUNDED_RECT_FORMAT.getVertexSize());
        RenderPipelines.register(HSB);
        LOGGER.info("Registered HSB pipeline with format stride={}",
                LDLibShaders.HSB_VERTEX_FORMAT.getVertexSize());
        RenderPipelines.register(MASK_ALPHA_MULTIPLY);
        RenderPipelines.register(STRIP_LINES);
        RenderPipelines.register(SDF_TEXT_GUI);
        RenderPipelines.register(SDF_TEXT);
        RenderPipelines.register(SDF_TEXT_POLYGON_OFFSET);
        RenderPipelines.register(SDF_TEXT_SEE_THROUGH);
        RenderPipelines.register(RASTER_TEXT_GUI);
        RenderPipelines.register(RASTER_TEXT);
        RenderPipelines.register(RASTER_TEXT_POLYGON_OFFSET);
        RenderPipelines.register(RASTER_TEXT_SEE_THROUGH);

        LOGGER.info("=== LDLib2 render pipelines registered successfully ===");
    }
}
