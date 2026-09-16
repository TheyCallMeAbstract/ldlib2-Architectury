package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import com.lowdragmc.lowdraglib2.gui.ui.rendering.PictureInPictureRendererRegistration;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.PictureInPictureRendererPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reaches the registration a pool was built from, so a second {@code GuiRenderer} can build pools of
 * its own with the same set of renderers.
 *
 * <p>Sharing the game's pools instead does not work: a pool keys its reuse bookkeeping on "the
 * renderers used this frame", which assumes one gui renderer per frame. With two — the game's and a
 * UI hosted in its own OS window — the second one's states can be handed a renderer the first has
 * already rendered into, and the blit then samples a texture drawn for somewhere else.
 *
 * Replaces NeoForge's mixin on {@code net.neoforged.neoforge.client.gui.PictureInPictureRendererPool}.
 */
@Mixin(PictureInPictureRendererPool.class)
public interface PictureInPictureRendererPoolAccessor {
    @Accessor("factory")
    PictureInPictureRendererRegistration<?> ldlib2$getFactory();
}
