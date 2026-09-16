package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import net.minecraft.client.particle.ParticleResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for ParticleEngine's private resourceManager field.
 * Note: The field is named resourceManager but is actually ParticleResources in MC 26.1.
 */
@Mixin(targets = "net.minecraft.client.particle.ParticleEngine")
public interface ParticleEngineAccessor {
    @Accessor("resourceManager")
    ParticleResources ldlib2$getResourceManager();
}
