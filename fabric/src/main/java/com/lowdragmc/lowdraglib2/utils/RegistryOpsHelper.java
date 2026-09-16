package com.lowdragmc.lowdraglib2.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;

import java.lang.reflect.Field;

/**
 * Helper to extract the {@link HolderLookup.Provider} held by a {@link RegistryOps},
 * replacing NeoForge's {@code CommonHooks.extractLookupProvider}.
 *
 * <p>Accesses the private {@code lookupProvider} field reflectively, then unwraps the
 * package-private {@code RegistryOps.HolderLookupAdapter} to reach the real provider.</p>
 *
 * <p>Deliberately outside {@code com.lowdragmc.lowdraglib2.core.mixins.*}: Mixin forbids
 * loading any class from a declared mixin package directly, and callers like
 * {@link PersistedParser} are not mixins.</p>
 */
public final class RegistryOpsHelper {
    private RegistryOpsHelper() {}

    /**
     * @return the {@link HolderLookup.Provider} backing {@code registryOps}, or {@code null}
     *         if it could not be resolved.
     */
    public static HolderLookup.Provider extractLookupProvider(RegistryOps<?> registryOps) {
        try {
            Field lookupField = RegistryOps.class.getDeclaredField("lookupProvider");
            lookupField.setAccessible(true);
            Object lookupProvider = lookupField.get(registryOps);
            if (lookupProvider == null) return null;
            for (Class<?> innerClass : RegistryOps.class.getDeclaredClasses()) {
                if (HolderLookup.Provider.class.isAssignableFrom(innerClass) && innerClass.isInstance(lookupProvider)) {
                    Field providerField = innerClass.getDeclaredField("lookupProvider");
                    providerField.setAccessible(true);
                    Object provider = providerField.get(lookupProvider);
                    if (provider instanceof HolderLookup.Provider holderLookupProvider) {
                        return holderLookupProvider;
                    }
                }
            }
        } catch (Exception e) {
            // Fall through and let the caller use its fallback registry.
        }
        return null;
    }
}
