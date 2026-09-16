package com.lowdragmc.lowdraglib2.editor.resource;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.fabric.client.FabricClientEventListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BuiltinTextureResourceResolutionTest {
    @TempDir
    static Path gameDir;

    @BeforeAll
    static void setUp() {
        // Resource.buildBuiltin creates a FileResourceProvider rooted at LDLib2.getAssetsDir(),
        // which needs a non-null Platform.getGamePath(); Fabric Loader is up (fabric-loader-junit
        // LauncherSessionListener) but no mod initializer has run in the test JVM.
        Platform.setInstance(new Platform() {
            @Override
            protected Path getGamePathImpl() {
                return gameDir;
            }
        });
        LDLib2.init(); // caches the assets dir under the temp game dir
        // Register the exact production handler under test.
        EditorResourceEvent.registerListener(FabricClientEventListener::onLoadBuiltinEditorResource);
    }

    @Test
    void oreBuiltinResolves() {
        assertNotNull(TexturesResource.INSTANCE.getResourceInstance()
                .getResource(new BuiltinPath("ui-ore:BORDER_7")));
    }

    @Test
    void mcBuiltinResolves() {
        assertNotNull(TexturesResource.INSTANCE.getResourceInstance()
                .getResource(new BuiltinPath("ui-mc:RECT")));
    }

    @Test
    void gdpBuiltinResolves() {
        assertNotNull(TexturesResource.INSTANCE.getResourceInstance()
                .getResource(new BuiltinPath("ui-gdp:RECT_SOLID")));
    }
}
