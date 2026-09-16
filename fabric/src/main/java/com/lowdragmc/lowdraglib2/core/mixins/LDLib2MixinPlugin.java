package com.lowdragmc.lowdraglib2.core.mixins;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public class LDLib2MixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        // Called when Mixin loads the config
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Apply all mixins by default
        return true;
    }
}