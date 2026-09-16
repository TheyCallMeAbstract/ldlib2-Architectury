package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor for GuiGraphicsExtractor's private fields.
 * NeoForge adds peekScissorStack() as a public method, but Fabric doesn't have it.
 * guiSprites is private in Fabric; scissorStack is public, so the scissor-stack peek
 * logic in {@code GUIContext.peekScissorStack} reads that field directly.
 * Kept as a pure accessor interface so Mixin classifies it as an Accessor Mixin,
 * which is allowed to target a class.
 */
@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
    @Accessor("guiSprites")
    TextureAtlas ldlib2$getGuiSprites();

    @Invoker("setTooltipForNextFrameInternal")
    void ldlib2$setTooltipForNextFrameInternal(
            net.minecraft.client.gui.Font font,
            java.util.List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> components,
            int mouseX, int mouseY,
            net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner positioner,
            net.minecraft.resources.Identifier background,
            boolean noFallback
    );
}
