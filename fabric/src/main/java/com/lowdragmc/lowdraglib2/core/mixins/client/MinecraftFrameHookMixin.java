package com.lowdragmc.lowdraglib2.core.mixins.client;

import com.lowdragmc.lowdraglib2.client.FrameEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric replacement for the NeoForge patch that fires {@code RenderFrameEvent.Post}.
 *
 * <p>NeoForge runs that event immediately <em>after</em> the
 * {@code GameRenderer.render(DeltaTracker, boolean)} call inside {@code Minecraft.renderFrame(boolean)}.
 * We reproduce the exact same point: an {@code INVOKE} injection with {@link At.Shift#AFTER} on the
 * single such call in {@code renderFrame}. (On this MC version {@code runTick} delegates rendering
 * to {@code renderFrame}; there is no direct {@code GameRenderer.render} call in {@code runTick}.)
 *
 * <p>Verified against the 26.1.2 client jar:
 * <pre>
 * private void renderFrame(boolean);
 *   204: invokevirtual net/minecraft/client/renderer/GameRenderer.extract:(Lnet/minecraft/client/DeltaTracker;Z)V
 *   234: invokevirtual net/minecraft/client/renderer/GameRenderer.render:(Lnet/minecraft/client/DeltaTracker;Z)V
 * </pre>
 * There is exactly one matching invocation, so {@code ordinal = 0} is unambiguous. This matters:
 * Fabric's mixin config sets {@code injectors.defaultRequire = 1}, so a non-matching injection point
 * would crash the client at launch.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftFrameHookMixin {

    @Inject(
            method = "renderFrame(Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V",
                    shift = At.Shift.AFTER,
                    ordinal = 0
            )
    )
    private void ldlib2$fireFrameEvent(boolean renderLevel, CallbackInfo ci) {
        FrameEvents.fire();
    }
}
