package com.lowdragmc.lowdraglib2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LDLib2 {
    public static final String MOD_ID = "ldlib2";
    public static final String NAME = "LowDragLib2";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static final String MODID_JEI = "jei";
    public static final String MODID_RUBIDIUM = "rubidium";
    public static final String MODID_REI = "roughlyenoughitems";
    public static final String MODID_EMI = "emi";
    public static final RandomSource RANDOM = RandomSource.createThreadSafe();
    public static final Gson GSON = new GsonBuilder().create();
    private static File assetsLocation;

    public static void init() {
        LOGGER.info("{} is initializing on platform: {}", NAME, Platform.platformName());
        getAssetsDir();
    }

    public static File getAssetsDir() {
        if (assetsLocation == null) {
            assetsLocation = new File(Platform.getGamePath().toFile(), "ldlib2/assets");
            if (assetsLocation.mkdirs()) {
                LOGGER.info("Created assets folder {}", assetsLocation.getPath());
            }
            if (new File(assetsLocation, "ldlib2").mkdirs()) {
                LOGGER.info("Created ldlib2 assets folder {}", assetsLocation.getPath());
            }
        }
        return assetsLocation;
    }

    public static boolean isValidResourceLocation(String string) {
        int i = string.indexOf(":");
        if (i == -1) {
            for (int j = 0; j < string.length(); j++) {
                if (!Identifier.isAllowedInIdentifier(string.charAt(j))) {
                    return false;
                }
            }
        } else {
            var namespace = string.substring(0, i);
            var path = string.substring(i + 1);
            return Identifier.isValidNamespace(namespace) && Identifier.isValidPath(path);
        }
        return true;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isClient() {
        return Platform.isClient();
    }

    public static boolean isRemote() {
        var minecraft = Platform.getMinecraftClient();
        return minecraft != null && minecraft.isSameThread();
    }

    public static boolean isServer() {
        if (!isClient()) return true;
        var server = Platform.getMinecraftServer();
        if (server != null) {
            return server.isSameThread();
        }
        return false;
    }

    public static boolean isModLoaded(String mod) {
        return Platform.isModLoaded(mod);
    }

    public static boolean isJeiLoaded() {
        return isModLoaded(MODID_JEI);
    }

    public static boolean isReiLoaded() {
        return isModLoaded(MODID_REI);
    }

    public static boolean isEmiLoaded() {
        return isModLoaded(MODID_EMI);
    }

    public static boolean isKubejsLoaded() {
        return Platform.isModLoaded("kubejs");
    }
}
