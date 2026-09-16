package com.lowdragmc.lowdraglib2.gui.holder;

import com.lowdragmc.lowdraglib2.core.mixins.accessor.AbstractContainerScreenAccessor;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUIClientAccess;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModularUIContainerScreen extends AbstractContainerScreen<ModularUIContainerMenu> {

    public ModularUIContainerScreen(ModularUIContainerMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        // On NeoForge this is done from ClientEventListener's ScreenEvent.Init.Pre handler. On Fabric there is
        // no equivalent deterministic hook wired for menus, so lay the UI out and attach the widget here,
        // mirroring ModularUIScreen.init(). Without this the screen renders blank.
        var mui = getMenu().getModularUI();
        if (mui == null) {
            super.init();
            return;
        }
        ModularUIClientAccess.setScreenAndInit(mui, this);
        ((AbstractContainerScreenAccessor) this).ldlib2$setImageWidth((int) mui.getWidth());
        ((AbstractContainerScreenAccessor) this).ldlib2$setImageHeight((int) mui.getHeight());
        super.init();
        // add + init the modular widget after super.init() so it lands in the screen's renderables
        var widget = ModularUIClientAccess.getWidget(mui);
        this.addRenderableWidget(widget);
        // initial focus
        setFocused(widget);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // todo
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (!ModularUIClientAccess.onFilesDrop(getMenu().getModularUI(), paths.stream().map(Path::toFile).toList())) {
            super.onFilesDrop(paths);
        }
    }
}
