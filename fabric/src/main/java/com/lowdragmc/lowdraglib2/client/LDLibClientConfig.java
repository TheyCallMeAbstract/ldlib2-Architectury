package com.lowdragmc.lowdraglib2.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.client.font.LDFontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Client only settings for LDLib2, everything currently under the {@code font} section.
 * <p>
 * This is the only place these are configured. Changes reach
 * {@link com.lowdragmc.lowdraglib2.client.font.LDFontManager} through config reload, so editing the
 * file, using the config screen and calling a setter here all take effect on their own.
 * <p>
 * <b>Fabric implementation:</b> On NeoForge this uses {@code ModConfigSpec} for type-safe config
 * management and automatic file I/O. On Fabric, YACL provides the GUI but not the config data
 * layer, so we use a simple GSON-backed JSON5 config file with the same field names and defaults.
 * The public API (static getters/setters) is identical on both platforms.
 */
public class LDLibClientConfig {

    /**
     * Prefix for the translation keys the configuration screen looks up. It matches the key NeoForge uses for
     * the screen title ({@code <modid>.configuration.title}), and every entry additionally gets a
     * {@code .tooltip} key. Without these the screen falls back to showing raw field names.
     */
    private static final String LANG = LDLib2.MOD_ID + ".configuration.";

    /**
     * How LDLib turns an outline into pixels.
     * <p>
     * On NeoForge this implements {@code TranslatableEnum} so NeoForge's config screen shows translated names.
     * On Fabric, YACL's {@code EnumControllerBuilder} handles the display using the enum's {@code toString()}
     * or a translation key, so we provide {@code getTranslatedName()} for parity.
     */
    public enum FontRenderMode {
        /**
         * Hand text to Minecraft's own font renderer and stay out of the way entirely. The baseline everything
         * else is compared against.
         */
        VANILLA,
        /**
         * Always sample a signed distance field. One atlas serves every size, scales and rotates smoothly, but
         * has no hinting so small text is softer than a hand tuned bitmap font, and sharp corners round off.
         */
        SDF,
        /**
         * Rasterize every glyph at the size it is actually drawn at, the way desktop UI toolkits do. Nothing is
         * approximated, so this is as sharp as the font gets, but each size needs its own atlas and a new size
         * has to be baked before it can be drawn. Only text beyond {@code fontRasterMaxSize} falls back, and
         * that is a memory guard rather than a judgement about how it looks.
         */
        RASTER,
        /**
         * Rasterize text that is sitting still on the pixel grid, and use the distance field for anything
         * scaled, rotated, skewed or animated, where it is both smoother and free of re-baking.
         */
        AUTO;

        public Component getTranslatedName() {
            return Component.translatable(LANG + "font.fontRenderMode." + name().toLowerCase(Locale.ROOT));
        }
    }

    // Config file path — Fabric uses the config directory, same as NeoForge
    private static final Path CONFIG_PATH = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getConfigDir().resolve("ldlib2_client.json5");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Config values with defaults matching the NeoForge ModConfigSpec defaults
    private static FontRenderMode fontRenderMode = FontRenderMode.AUTO;
    private static int fontAtlasSize = 1024;
    private static int sdfEmSize = 48;
    private static double sdfSharpness = 1.0;
    private static double sdfWeight = 0.0;
    private static int fontRasterMaxSize = 256;
    private static int fontRasterEvictSeconds = 30;
    private static boolean textLayoutCache = true;

    // Whether the config has been loaded from disk
    private static boolean loaded = false;

    /**
     * Loads config values from the JSON5 config file. Called on client init and after config screen save.
     * If the file doesn't exist, defaults are used and the file is created.
     */
    public static void load() {
        if (Files.notExists(CONFIG_PATH)) {
            loaded = true;
            save(); // create default config file
            return;
        }
        JsonObject json = null;
        try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
            json = GSON.fromJson(reader, JsonObject.class);
        } catch (IOException | JsonParseException e) {
            LDLib2.LOGGER.error("Failed to load LDLib2 client config, using defaults", e);
        }

        // Gson parses an empty or blank file to null. An earlier save() left exactly such a file
        // behind by never closing its writer, so treat it as "no config yet" and rewrite defaults
        // rather than dereferencing null.
        if (json == null) {
            loaded = true;
            save(); // rewrite a well formed default config
            return;
        }

        if (json.has("fontRenderMode")) {
            var mode = json.get("fontRenderMode").getAsString();
            try {
                fontRenderMode = FontRenderMode.valueOf(mode);
            } catch (IllegalArgumentException e) {
                LDLib2.LOGGER.warn("Unknown fontRenderMode '{}' in LDLib2 client config, keeping {}", mode, fontRenderMode);
            }
        }
        if (json.has("fontAtlasSize")) fontAtlasSize = json.get("fontAtlasSize").getAsInt();
        if (json.has("sdfEmSize")) sdfEmSize = json.get("sdfEmSize").getAsInt();
        if (json.has("sdfSharpness")) sdfSharpness = json.get("sdfSharpness").getAsDouble();
        if (json.has("sdfWeight")) sdfWeight = json.get("sdfWeight").getAsDouble();
        if (json.has("fontRasterMaxSize")) fontRasterMaxSize = json.get("fontRasterMaxSize").getAsInt();
        if (json.has("fontRasterEvictSeconds")) fontRasterEvictSeconds = json.get("fontRasterEvictSeconds").getAsInt();
        if (json.has("textLayoutCache")) textLayoutCache = json.get("textLayoutCache").getAsBoolean();

        loaded = true;
        invalidateFonts();
    }

    /**
     * The client config decides how glyphs are baked, so a change to any glyph-affecting value invalidates
     * every atlas. Handed to the client thread because a config screen may reach this from its own thread and
     * freeing a texture is not thread safe.
     * <p>
     * Fabric has no {@code ModConfigEvent.Reloading} equivalent, so this is called directly from {@link #load()}
     * and from the setters (which is also YACL's save path).
     */
    private static void invalidateFonts() {
        var minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.execute(LDFontManager.INSTANCE::invalidate);
        } else {
            LDFontManager.INSTANCE.invalidate();
        }
    }

    /**
     * Saves config values to the JSON5 config file. Called after config screen save.
     */
    public static void save() {
        var root = new JsonObject();
        root.addProperty("fontRenderMode", fontRenderMode.name());
        root.addProperty("fontAtlasSize", fontAtlasSize);
        root.addProperty("sdfEmSize", sdfEmSize);
        root.addProperty("sdfSharpness", sdfSharpness);
        root.addProperty("sdfWeight", sdfWeight);
        root.addProperty("fontRasterMaxSize", fontRasterMaxSize);
        root.addProperty("fontRasterEvictSeconds", fontRasterEvictSeconds);
        root.addProperty("textLayoutCache", textLayoutCache);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            // try-with-resources matters here: an unclosed writer is never flushed, which leaves a
            // zero byte file that parses back as null and crashed the client on the next launch.
            try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
                writer.write(GSON.toJson(root));
            }
        } catch (IOException e) {
            LDLib2.LOGGER.error("Failed to save LDLib2 client config", e);
        }
    }

    /**
     * @return true if the config has been loaded from disk (vs using in-memory defaults)
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * @return true when LDLib renders text itself rather than handing it to the vanilla renderer
     */
    public static boolean isSmoothFont() {
        // fallback to modern ui if installed
        return fontRenderMode() != FontRenderMode.VANILLA && !LDLib2.isModLoaded("modernui");
    }

    public static int atlasSize() {
        return isLoaded() ? fontAtlasSize : 1024;
    }

    public static int emSize() {
        return isLoaded() ? sdfEmSize : 48;
    }

    public static float sharpness() {
        return isLoaded() ? (float) sdfSharpness : 1f;
    }

    public static float weight() {
        return isLoaded() ? (float) sdfWeight : 0f;
    }

    public static boolean isTextLayoutCache() {
        return isLoaded() ? textLayoutCache : true;
    }

    public static FontRenderMode fontRenderMode() {
        return isLoaded() ? fontRenderMode : FontRenderMode.AUTO;
    }

    public static int rasterMaxSize() {
        return isLoaded() ? fontRasterMaxSize : 256;
    }

    public static int rasterEvictSeconds() {
        return isLoaded() ? fontRasterEvictSeconds : 30;
    }

    // Setters — called by YACL config screen bindings and by the dev command
    public static void setFontRenderMode(FontRenderMode mode) {
        fontRenderMode = mode;
        save();
        invalidateFonts();
    }

    public static void setAtlasSize(int size) {
        fontAtlasSize = size;
        invalidateFonts();
    }

    public static void setEmSize(int size) {
        sdfEmSize = size;
        invalidateFonts();
    }

    public static void setSharpness(double sharpness) {
        sdfSharpness = sharpness;
        invalidateFonts();
    }

    public static void setWeight(double weight) {
        sdfWeight = weight;
        invalidateFonts();
    }

    public static void setRasterMaxSize(int size) {
        fontRasterMaxSize = size;
        invalidateFonts();
    }

    public static void setRasterEvictSeconds(int seconds) {
        fontRasterEvictSeconds = seconds;
        invalidateFonts();
    }

    public static void setTextLayoutCache(boolean cache) {
        textLayoutCache = cache;
        invalidateFonts();
    }
}
