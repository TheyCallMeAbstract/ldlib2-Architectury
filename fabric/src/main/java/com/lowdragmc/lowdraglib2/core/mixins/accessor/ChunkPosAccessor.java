package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for ChunkPos's private x/z fields.
 * NeoForge makes these public; Fabric keeps them private.
 */
@Mixin(ChunkPos.class)
public interface ChunkPosAccessor {
    @Accessor("x")
    int ldlib2$getX();

    @Accessor("z")
    int ldlib2$getZ();
}
