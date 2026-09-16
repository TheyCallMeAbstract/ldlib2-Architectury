package com.lowdragmc.lowdraglib2.gui.util;

import com.lowdragmc.lowdraglib2.client.font.LDFonts;
import com.lowdragmc.lowdraglib2.client.shader.LDLibRenderPipelines;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.texture.renderstate.FloatLineStripRenderState;
import com.lowdragmc.lowdraglib2.utils.ColorUtils;
import com.lowdragmc.lowdraglib2.utils.FluidHelperClient;
import com.lowdragmc.lowdraglib2.math.Rect;
import dev.architectury.fluid.FluidStack;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DrawerHelperClient {
    public static void installSharedHooks() {
        ItemTooltipTextHelper.setProvider(DrawerHelperClient::getItemToolTip);
    }

    public static void drawFluidTexture(@NotNull GUIContext context,
                                        float xCoord, float yCoord, TextureAtlasSprite textureSprite,
                                        float maskTop, float maskRight, int fluidColor) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16f * (uMax - uMin);
        vMax = vMax - maskTop / 16f * (vMax - vMin);

        context.innerBlit(
                RenderPipelines.GUI_TEXTURED, textureSprite.atlasLocation(),
                xCoord, xCoord + 16 - maskRight, yCoord + maskTop, yCoord + 16,
                uMin, uMax, vMin, vMax,
                fluidColor
        );
    }

    public static void drawFluidForGui(@NotNull GUIContext context, FluidStack contents,
                                       float startX, float startY, float widthT, float heightT, int color) {
        var fluidStillSprite = FluidHelperClient.getStillMaterial(contents).sprite();
        int fluidColor = FluidHelperClient.getColor(contents) | 0xff000000;
        if (color != -1) {
            fluidColor = ColorUtils.mulColor(fluidColor, color);
        }

        final int xTileCount = (int) (widthT / 16);
        final float xRemainder = widthT - xTileCount * 16;
        final int yTileCount = (int) (heightT / 16);
        final float yRemainder = heightT - yTileCount * 16;

        final float yStart = startY + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                float width = xTile == xTileCount ? xRemainder : 16;
                float height = yTile == yTileCount ? yRemainder : 16;
                float x = startX + xTile * 16;
                float y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    float maskTop = 16 - height;
                    float maskRight = 16 - width;
                    drawFluidTexture(context, x, y, fluidStillSprite, maskTop, maskRight, fluidColor);
                }
            }
        }
    }

    public static void drawBorder(@NotNull GUIContext context, float x, float y, float width, float height, int color, int border) {
        if (border >= 0) {
            drawSolidRect(context,x - border, y + height, width + 2 * border, border, color);
            drawSolidRect(context,x - border, y, border, height, color);
            drawSolidRect(context,x + width, y, border, height, color);
            drawSolidRect(context,x - border, y - border, width + 2 * border, border, color);
        } else {
            float absBorder = Math.abs(border);
            drawSolidRect(context, x, y, width - absBorder, absBorder, color);
            drawSolidRect(context, x, y + absBorder, absBorder, height - absBorder, color);
            drawSolidRect(context, x + absBorder, y + height - absBorder, width - absBorder, absBorder, color);
            drawSolidRect(context, x + width - absBorder, y, absBorder, height - absBorder, color);
        }
    }

    public static void drawStringSized(@NotNull GUIContext context, String text, float x, float y, int color, boolean dropShadow, float scale, boolean center) {
        context.pose.pushPose();
        Font fontRenderer = LDFonts.font();
        var scaledTextWidth = center ? fontRenderer.getSplitter().stringWidth(text) * scale : 0f;
        context.pose.translate(x - scaledTextWidth / 2f, y);
        context.pose.scale(scale, scale);
        LDFonts.drawText(context, fontRenderer, text, 0, 0, color, dropShadow);
        context.pose.popPose();
    }

    public static void drawStringFixedCorner(@NotNull GUIContext context, String text, float x, float y, int color, boolean dropShadow, float scale) {
        Font fontRenderer = LDFonts.font();
        float scaledWidth = fontRenderer.getSplitter().stringWidth(text) * scale;
        float scaledHeight = fontRenderer.lineHeight * scale;
        drawStringSized(context, text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    public static void drawText(@NotNull GUIContext context, String text, float x, float y, float scale, int color) {
        drawText(context, text, x, y, scale, color, false);
    }

    public static void drawText(@NotNull GUIContext context, String text, float x, float y, float scale, int color, boolean shadow) {
        Font fontRenderer = LDFonts.font();
        context.pose.pushPose();
        context.pose.scale(scale, scale);
        float sf = 1 / scale;
        LDFonts.drawText(context, fontRenderer, text, (int) (x * sf), (int) (y * sf), color, shadow);
        context.pose.popPose();
    }

    public static void drawItemStack(@NotNull GUIContext context, ItemStack itemStack, int x, int y, int seed) {
        if (itemStack.isEmpty()) return;
        context.graphics.item(itemStack, x, y);
        context.graphics.itemDecorations(context.mc.font, itemStack, x, y);
    }

    public static List<Component> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getInstance();
        return Screen.getTooltipFromItem(mc, itemStack);
    }

    public static void drawSolidRect(@NotNull GUIContext context, Rect rect, int color) {
        drawSolidRect(context, rect.left, rect.up, rect.right, rect.down, color);
    }

    public static void drawSolidRect(@NotNull GUIContext context, float x, float y, float width, float height, int color) {
        drawSolidRect(context, RenderPipelines.GUI, x, y, width, height, color);
    }

    public static void drawSolidRect(@NotNull GUIContext context, RenderPipeline renderPipeline, float x, float y, float width, float height, int color) {
        context.fill(renderPipeline, x, y, x + width, y + height, color, color, color, color);
    }

    public static void drawGradientRect(@NotNull GUIContext context, float x, float y, float width, float height, int startColor, int endColor, boolean horizontal) {
        drawGradientRect(context, RenderPipelines.GUI, x, y, width, height, startColor, endColor, horizontal);
    }

    public static void drawGradientRect(@NotNull GUIContext context, RenderPipeline renderPipeline, float x, float y, float width, float height,
                                        int startColor, int endColor, boolean horizontal) {
        if (horizontal) {
            context.fill(renderPipeline, x, y, x + width, y + height, startColor, startColor, endColor, endColor);
        } else {
            context.fill(renderPipeline, x, y, x + width, y + height, startColor, endColor, endColor, startColor);
        }
    }

    public static void drawLines(@NotNull GUIContext context,
                                 List<Vector2f> points, int startColor, int endColor, float width) {
        context.addGuiElement(new FloatLineStripRenderState(
                LDLibRenderPipelines.STRIP_LINES,
                TextureSetup.noTexture(),
                context.pose.copyPose(),
                points,
                startColor,
                endColor,
                width,
                false,
                context.peekScissor()
        ));
    }

    public static void drawTexLines(@NotNull GUIContext context, RenderPipeline renderPipeline, TextureSetup textureSetup,
                                    List<Vector2f> points, int startColor, int endColor, float width) {
        context.addGuiElement(new FloatLineStripRenderState(
                renderPipeline,
                textureSetup,
                context.pose.copyPose(),
                points,
                startColor,
                endColor,
                width,
                true,
                context.peekScissor()
        ));
    }

    public static void drawTooltip(GUIContext context, HoverTooltips hoverTooltips) {
        drawTooltip(context, hoverTooltips, false, false);
    }

    public static void drawTooltip(GUIContext context, HoverTooltips hoverTooltips, boolean setNextFrame, boolean replacing) {
        if (hoverTooltips.tooltips().isEmpty()) return;
        var font = tooltipFont(hoverTooltips, context);
        var stack = Optional.ofNullable(hoverTooltips.tooltipStack()).orElse(ItemStack.EMPTY);
        var mouseX = context.mouseX;
        var mouseY = context.mouseY;
        if (setNextFrame) {
            var graphicsAccessor = (com.lowdragmc.lowdraglib2.core.mixins.accessor.GuiGraphicsExtractorAccessor) context.graphics;

            var clientTooltipComponents = toClientTooltips(hoverTooltips.tooltips(), stack, mouseX,
                    context.graphics.guiWidth(), context.graphics.guiHeight(), font);

            graphicsAccessor.ldlib2$setTooltipForNextFrameInternal(font,
                    clientTooltipComponents,
                    mouseX, mouseY,
                    tooltipPositioner(hoverTooltips), hoverTooltips.background(), replacing);
        } else {
            context.graphics.tooltip(
                    font,
                    toClientTooltips(hoverTooltips.tooltips(), stack, mouseX,
                            context.graphics.guiWidth(), context.graphics.guiHeight(), font),
                    (int) context.localMouseX, (int) context.localMouseY,
                    tooltipPositioner(hoverTooltips),
                    hoverTooltips.background());
        }
    }

    public static Font tooltipFont(HoverTooltips hoverTooltips, GUIContext context) {
        return hoverTooltips.tooltipFont() instanceof Font font ? font : context.mc.font;
    }

    public static ClientTooltipPositioner tooltipPositioner(HoverTooltips hoverTooltips) {
        return hoverTooltips.positioner() instanceof ClientTooltipPositioner positioner
                ? positioner
                : DefaultTooltipPositioner.INSTANCE;
    }

    /**
     * Converts tooltips into {@link ClientTooltipComponent}s.
     * Fabric port: uses vanilla tooltip gathering instead of NeoForge's ClientHooks.
     */
    public static List<ClientTooltipComponent> toClientTooltips(List<?> tooltips, ItemStack stack, int mouseX,
                                                                int screenWidth, int screenHeight, Font font) {
        if (tooltips.isEmpty()) {
            return List.of();
        }
        var clientTooltips = new ArrayList<ClientTooltipComponent>(tooltips.size());
        // Fabric: gather components directly, no NeoForge ClientHooks pipeline
        for (var tooltip : tooltips) {
            if (tooltip == null) continue;
            switch (tooltip) {
                case ClientTooltipComponent clientTooltipComponent -> clientTooltips.add(clientTooltipComponent);
                case TooltipComponent tooltipComponent -> clientTooltips.add(ClientTooltipComponent.create(tooltipComponent));
                case Component component -> clientTooltips.add(ClientTooltipComponent.create(component.getVisualOrderText()));
                case FormattedText formattedText -> {
                    var visualOrder = Component.literal(formattedText.getString());
                    clientTooltips.add(ClientTooltipComponent.create(formattedText instanceof Component c ? c.getVisualOrderText() : visualOrder.getVisualOrderText()));
                }
                case FormattedCharSequence formattedCharSequence ->
                        clientTooltips.add(ClientTooltipComponent.create(formattedCharSequence));
                default -> clientTooltips.add(ClientTooltipComponent.create(Component.literal(tooltip.toString()).getVisualOrderText()));
            }
        }
        return clientTooltips;
    }

    public static List<ClientTooltipComponent> toClientTooltips(List<?> tooltips) {
        if (tooltips.isEmpty()) {
            return List.of();
        }
        var clientTooltips = new ArrayList<ClientTooltipComponent>(tooltips.size());
        for (var tooltip : tooltips) {
            if (tooltip == null) continue;
            switch (tooltip) {
                case ClientTooltipComponent clientTooltipComponent -> clientTooltips.add(clientTooltipComponent);
                case TooltipComponent tooltipComponent -> clientTooltips.add(ClientTooltipComponent.create(tooltipComponent));
                case Component component -> clientTooltips.add(ClientTooltipComponent.create(component.getVisualOrderText()));
                case FormattedCharSequence formattedCharSequence ->
                        clientTooltips.add(ClientTooltipComponent.create(formattedCharSequence));
                default -> clientTooltips.add(ClientTooltipComponent.create(Component.literal(tooltip.toString()).getVisualOrderText()));
            }
        }
        return clientTooltips;
    }
}
