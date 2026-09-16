package com.lowdragmc.lowdraglib2.registry;

import com.lowdragmc.lowdraglib2.Platform;
// TODO: Fabric stub — NeoForge ModAnnotation
// import net.neoforged.fml.loading.modscan.ModAnnotation;

import java.util.Map;

/**
 * Controls when an annotated element should be registered based on the runtime environment.
 */
public enum RegistrationEnvironment {
    /**
     * Always register, regardless of environment.
     */
    ALWAYS,
    /**
     * Only register in development environment.
     */
    DEV_ONLY,
    /**
     * Only register in production environment.
     */
    PRODUCTION_ONLY,
    /**
     * Do not register automatically. Must be registered manually.
     */
    MANUAL;

    /**
     * Whether this environment allows automatic registration in the current runtime.
     */
    public boolean shouldRegister() {
        return switch (this) {
            case ALWAYS -> true;
            case DEV_ONLY -> Platform.isDevEnv();
            case PRODUCTION_ONLY -> !Platform.isDevEnv();
            case MANUAL -> false;
        };
    }

    /**
     * Checks annotation data map for environment and legacy manual fields.
     * Use this in annotation filters for {@link AutoRegistry}.
     */
    public static boolean shouldRegister(Map<String, Object> annotationData) {
        // ASM AnnotationNode stores enum values as a two-element String[]: {descriptor, constantName}
        Object envValue = annotationData.get("environment");
        if (envValue instanceof String[] enumValue && enumValue.length == 2) {
            return RegistrationEnvironment.valueOf(enumValue[1]).shouldRegister();
        }
        if (envValue instanceof org.objectweb.asm.Type[] types && types.length > 0) {
            // Get the simple name from the internal name, e.g. "com/lowdragmc/.../RegistrationEnvironment$ALWAYS" -> "ALWAYS"
            String internalName = types[0].getInternalName();
            String enumName = internalName.contains("$") ? internalName.substring(internalName.lastIndexOf('$') + 1) : internalName;
            return RegistrationEnvironment.valueOf(enumName).shouldRegister();
        }
        // Also handle direct string values (e.g. from Fabric annotation processing)
        if (envValue instanceof String str) {
            return RegistrationEnvironment.valueOf(str).shouldRegister();
        }
        return true;
    }
}
