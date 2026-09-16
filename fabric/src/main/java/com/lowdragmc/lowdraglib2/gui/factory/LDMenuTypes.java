package com.lowdragmc.lowdraglib2.gui.factory;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.editor.UIEditor;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class LDMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(LDLib2.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<ModularUIContainerMenu>> PLAYER_UI =
            MENUS.register("player_ui", () -> MenuRegistry.ofExtended(PlayerUIMenuType::create));

    public static final RegistrySupplier<MenuType<ModularUIContainerMenu>> HELD_ITEM_UI =
            MENUS.register("held_item_ui", () -> MenuRegistry.ofExtended(HeldItemUIMenuType::create));

    public static final RegistrySupplier<MenuType<ModularUIContainerMenu>> BLOCK_UI =
            MENUS.register("block_ui", () -> MenuRegistry.ofExtended(BlockUIMenuType::create));

    public static void init() {
        PlayerUIMenuType.register(UIEditor.WINDOW_ID, ignored -> player -> {
            if (player.level().isClientSide()) {
                return new ModularUI(UI.of(EditorWindow.open(UIEditor.WINDOW_ID, UIEditor::new)))
                        .shouldCloseOnEsc(false)
                        .shouldCloseOnKeyInventory(false);
            }
            return new ModularUI(UI.empty());
        });

        MENUS.register();
    }
}
