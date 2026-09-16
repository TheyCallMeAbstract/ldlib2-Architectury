package com.lowdragmc.lowdraglib2.test.gametest;

import com.lowdragmc.lowdraglib2.event.LDLib2Events;
import com.lowdragmc.lowdraglib2.event.SubscribeEvent;
import com.lowdragmc.lowdraglib2.gui.event.ContainerMenuEvent;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * {@link ContainerMenuEvent.Create} has to be posted on the server whenever a player opens a
 * container menu, so mods can react to the newly created {@link net.minecraft.world.inventory.AbstractContainerMenu}.
 * The server path is exercised here through {@link ServerPlayer#openMenu(net.minecraft.world.MenuProvider)}.
 */
public final class ContainerMenuEventGameTest {

    private ContainerMenuEventGameTest() {
    }

    public static void createFiresOnMenuOpen(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var listener = new CapturingListener(player);
        LDLib2Events.register(listener);

        int containerId = player.openMenu(new SimpleMenuProvider(
                (id, inventory, opener) -> new ChestMenu(MenuType.GENERIC_9x1, id, inventory, new SimpleContainer(9), 1),
                Component.literal("ldlib2 container menu event test"))).orElse(-1);

        var event = listener.captured;
        if (event == null) {
            helper.fail("ContainerMenuEvent.Create was not posted when a menu was opened");
            return;
        }
        if (event.menu == null) {
            helper.fail("The fired event carried a null menu");
            return;
        }
        if (event.menu != player.containerMenu) {
            helper.fail("The fired event's menu is not the player's open menu");
            return;
        }
        if (containerId < 0 || event.menu.containerId != containerId) {
            helper.fail("The fired event's menu does not match the opened container id " + containerId);
            return;
        }
        if (event.player != player) {
            helper.fail("The fired event's player is not the opener");
            return;
        }
        if (event.isRemote()) {
            helper.fail("The fired event reported a remote (client) side on the server");
            return;
        }
        helper.succeed();
    }

    private static final class CapturingListener {
        private final ServerPlayer opener;
        private ContainerMenuEvent.Create captured;

        private CapturingListener(ServerPlayer opener) {
            this.opener = opener;
        }

        @SubscribeEvent
        public void onCreate(ContainerMenuEvent.Create event) {
            if (event.player == opener) {
                this.captured = event;
            }
        }
    }
}
