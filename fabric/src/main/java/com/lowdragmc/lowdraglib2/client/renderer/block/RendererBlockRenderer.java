package com.lowdragmc.lowdraglib2.client.renderer.block;

import com.lowdragmc.lowdraglib2.client.renderer.IRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

class RendererBlockRenderer implements IRenderer {

    public Optional<RendererBlockEntity> getMachine(@Nullable BlockEntity blockEntity) {
        return Optional.ofNullable(blockEntity).filter(RendererBlockEntity.class::isInstance).map(RendererBlockEntity.class::cast);
    }

    public Optional<RendererBlockEntity> getMachine(@Nullable BlockAndLightGetter level, @Nullable BlockPos pos) {
        if (level == null || pos == null) return Optional.empty();
        return getMachine(level.getBlockEntity(pos));
    }
}
