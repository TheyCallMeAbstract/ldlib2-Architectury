package com.lowdragmc.lowdraglib2.test.uitest;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.lowdragmc.lowdraglib2.uitest.input.Keys;
import net.minecraft.client.gui.screens.ChatScreen;

/**
 * Drives the real chat-command path: opens a {@link ChatScreen}, types
 * {@code /ldlib2_screen_test doc} one character at a time, submits it with ENTER, and asserts the
 * screen test actually opened.
 *
 * <p>This closes a gap the {@code screen_test_sweep} scenario cannot reach. That sweep opens each
 * screen test by calling {@code Minecraft#setScreen} directly from the harness frame loop, so no
 * {@code ChatScreen} is ever involved. Here the screen is set from inside the command handler, which
 * Fabric runs <em>synchronously</em> while {@code ChatScreen#keyPressed} is still on the stack — and
 * vanilla then calls {@code Minecraft#setScreen(null)} to close the chat, clobbering whatever the
 * handler set. The sweep sets the screen after ChatScreen has already torn itself down, so it never
 * sees that race at all.
 *
 * <p>Vanilla has no equivalent of the NeoForge {@code ChatScreen} patch that guards that teardown,
 * so the Fabric {@code ClientCommands} handler does not open the screen from the command callback at
 * all: it arms a pending screen which {@code ClientCommands#applyPendingScreen} opens from the
 * per-frame {@code FrameEvents} hook, on a frame strictly after the one the command ran on. This
 * scenario is the regression guard for that Fabric-only workaround: remove the indirection and the
 * {@code waitUntil} below times out because the chat screen's {@code setScreen(null)} wins.
 *
 * <p>{@code doc} is used because it is deterministic, cheap to build, and produces a non-trivial
 * element tree; any {@code ldlib2:screen_test} entry would exercise the same command path.
 */
@LDLRegisterClient(name = "chat_command_screen", group = "ldlib2", registry = UIScenario.REGISTRY,
        environment = RegistrationEnvironment.DEV_ONLY)
public class ChatCommandScreenScenario implements UIScenario {

    /** The literal command a user would type, including the leading slash. */
    private static final String COMMAND = "/ldlib2_screen_test doc";

    @Override
    public void configure(ScenarioOptions options) {
        // A ChatScreen needs a player: ChatScreen#init reads the player's chat abilities, and the
        // command handler needs a player to build the screen test's UI.
        options.defaultSettleMs(30).tags("screen_tests", "chat_commands").requiresWorld(true).guiScale(3);
    }

    @Override
    public void define(ScenarioBuilder s) {
        s.openScreen("the chat screen", ctx -> new ChatScreen("", false))
                .checkScreen(ChatScreen.class)
                // One character per step: charTyped fills the EditBox through the same focus path a
                // real keyboard uses. ENTER then goes to ChatScreen#keyPressed -> handleChatInput ->
                // ClientPacketListener#sendCommand, where Fabric dispatches client commands
                // synchronously. This is the path the sweep never touches.
                .type(COMMAND)
                .key(Keys.ENTER)
                // The core assertion. With the workaround in place the handler arms a pending screen
                // that the frame hook opens on a later frame, so it survives ChatScreen's own
                // setScreen(null); without it this never becomes true and the step times out with
                // this exact description.
                .waitUntil("the screen test opened", ctx -> ctx.mc().screen instanceof ModularUIScreen)
                .checkScreen(ModularUIScreen.class)
                .awaitModularUI()
                // Same generic render assertion the screen sweep uses: #root only exists on some
                // screen tests, so instead of a selector we prove the registry-backed ModularUI has
                // more than its root element.
                .check("the doc screen rendered a non-trivial element tree",
                        ctx -> ctx.requireUI().getAllElements().size() > 1)
                .screenshot("01_after_chat_command")
                .closeScreen()
                .check("the screen closed", ctx -> ctx.screen() == null)
                .teardown("close any screen the chat command left open", ctx -> {
                    var player = ctx.player();
                    if (player != null) player.closeContainer();
                    ctx.mc().setScreen(null);
                });
    }
}
