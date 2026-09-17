package com.lowdragmc.lowdraglib2.gui.ui.elements;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.Style;
import com.lowdragmc.lowdraglib2.gui.ui.style.Property;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.DelegatingUIElementRenderer;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.IGUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelperClient;
import com.lowdragmc.lowdraglib2.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.lowdragmc.lowdraglib2.integration.xei.XEITooltipContext;
import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.syncdata.ISubscription;
import com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue;
import com.lowdragmc.lowdraglib2.utils.FluidHelper;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import com.lowdragmc.lowdraglib2.misc.IFluidHandler;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.fabric.ingredients.fluids.JeiFluidIngredient;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.w3c.dom.Element;

import org.jetbrains.annotations.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(chain = true)
@KJSBindings
@LDLRegister(name = "fluid-slot", group = "inventory", registry = "ldlib2:ui_element")
public class FluidSlot extends BindableUIElement<FluidStack> {
    @Configurable(name = "SlotStyle")
    public class SlotStyle extends Style {
        private static final Property<?>[] PROPERTIES = new Property[] {
                PropertyRegistry.HOVER_OVERLAY,
                PropertyRegistry.SLOT_OVERLAY,
                PropertyRegistry.SHOW_SLOT_OVERLAY_ONLY_EMPTY,
                PropertyRegistry.FILL_DIRECTION,
                PropertyRegistry.SHOW_FLUID_TOOLTIPS,
        };
        public SlotStyle() {
            super(FluidSlot.this);
            setDefault(PropertyRegistry.HOVER_OVERLAY, new ColorRectTexture(0x80FFFFFF));
        }

        @Override
        protected Property<?>[] getProperties() {
            return PROPERTIES;
        }

        public IGuiTexture hoverOverlay() {
            return getValueSave(PropertyRegistry.HOVER_OVERLAY);
        }

        public SlotStyle hoverOverlay(IGuiTexture texture) {
            set(PropertyRegistry.HOVER_OVERLAY, texture);
            return this;
        }

        public IGuiTexture slotOverlay() {
            return getValueSave(PropertyRegistry.SLOT_OVERLAY);
        }

        public SlotStyle slotOverlay(IGuiTexture texture) {
            set(PropertyRegistry.SLOT_OVERLAY, texture);
            return this;
        }

        public boolean showSlotOverlayOnlyEmpty() {
            return getValueSave(PropertyRegistry.SHOW_SLOT_OVERLAY_ONLY_EMPTY);
        }

        public SlotStyle showSlotOverlayOnlyEmpty(boolean value) {
            set(PropertyRegistry.SHOW_SLOT_OVERLAY_ONLY_EMPTY, value);
            return this;
        }

        public FillDirection fillDirection() {
            return getValueSave(PropertyRegistry.FILL_DIRECTION);
        }

        public SlotStyle fillDirection(FillDirection fillDirection) {
            set(PropertyRegistry.FILL_DIRECTION, fillDirection);
            return this;
        }

        public boolean showFluidTooltips() {
            return getValueSave(PropertyRegistry.SHOW_FLUID_TOOLTIPS);
        }

        public SlotStyle showFluidTooltips(boolean showFluidTooltips) {
            set(PropertyRegistry.SHOW_FLUID_TOOLTIPS, showFluidTooltips);
            return this;
        }
    }

    public final Label amountLabel = new Label();
    @Getter
    private final SlotStyle slotStyle = new SlotStyle();
    @Getter @Setter
    private boolean allowClickFilled = true;
    @Getter @Setter
    private boolean allowClickDrained = true;
    // editor support
    @Configurable(name = "EditorFluidDisplay")
    private FluidStack editorFluidDisplay = FluidStack.empty();
    @Configurable(name = "EditorAllowXEILookup")
    private boolean allowXEILookup = true;
    // runtime
    @Getter
    private FluidStack fluid = FluidStack.empty();
    @Getter @Setter
    @Configurable(name = "Capacity")
    @ConfigNumber(range = {0, Integer.MAX_VALUE})
    private int capacity = 0;
    private final RPCEmitter clickEvent;

    @Nullable
    private IFluidHandler boundHandler;
    private int tankIndex;
    @Nullable
    private ISubscription fluidTankSubscription;

    public FluidSlot() {
        getLayout().width(18);
        getLayout().height(18);
        getLayout().paddingAll(1);
        getStyle().backgroundTexture(Sprites.RECT_DARK);
        addEventListener(UIEvents.HOVER_TOOLTIPS, this::onHoverTooltips);
        addEventListener(UIEvents.MOUSE_DOWN, this::onMouseDown);
        if (LDLib2.isClient() && !LDLib2.isServer()) {
            if (LDLib2.isJeiLoaded()) {
                JEISupport.clickableIngredient(this);
            }
        }
        clickEvent = addRPCEvent(RPCEventBuilder.simple(Boolean.class, this::tryClickContainer));

        amountLabel.addClass("__fluid-slot_amount-label__");
        amountLabel.layout(layout -> layout.widthPercent(100).heightPercent(100));
        amountLabel.textStyle(textStyle -> textStyle
                .textAlignVertical(Vertical.BOTTOM)
                .textAlignHorizontal(Horizontal.RIGHT)
                .fontSize(4.5f)
        );
        amountLabel.bindDataSource(SupplierDataSource.of(this::getFluidAmountText));
        addChild(amountLabel);
        internalSetup();
    }


    public FluidSlot slotStyle(Consumer<SlotStyle> style) {
        style.accept(slotStyle);
        return this;
    }

    /**
     * Binds this FluidSlot to an IFluidHandler tank.
     */
    public FluidSlot bind(@Nullable IFluidHandler fluidHandler, int tankIndex) {
        if (fluidTankSubscription != null) {
            fluidTankSubscription.unsubscribe();
        }

        this.boundHandler = fluidHandler;
        if (boundHandler == null) return this;
        this.tankIndex = tankIndex;

        if (tankIndex < 0 || tankIndex >= boundHandler.getTanks()) throw new IllegalArgumentException("Invalid tank index: " + tankIndex);
        var fluidBinding = DataBindingBuilder.fluidStackS2C(() -> boundHandler.getFluidInTank(this.tankIndex))
                .build();
        var capacitySyncValue = DataBindingBuilder.intValS2C(() -> boundHandler.getTankCapacity(this.tankIndex))
                .remoteSetter(this::setCapacity)
                .build()
                .getSyncValue();

        bind(fluidBinding);
        addSyncValue(capacitySyncValue);
        fluidTankSubscription = () -> {
            unbind(fluidBinding);
            removeSyncValue(capacitySyncValue);
            fluidTankSubscription = null;
        };

        return this;
    }

    public FluidSlot xeiPhantom() {
        if (LDLib2.isJeiLoaded()) {
            JEISupport.ghostIngredient(this);
        }
        return this;
    }

    public FluidSlot xeiRecipeIngredient(IngredientIO io) {
        if (LDLib2.isJeiLoaded()) {
            JEISupport.recipeIngredient(this, io);
        }
        return this;
    }

    public FluidSlot xeiRecipeIngredient(IngredientIO io, Supplier<Stream<FluidStack>> allPossibleFluids) {
        if (LDLib2.isJeiLoaded()) {
            JEISupport.recipeIngredient(this, io, allPossibleFluids);
        }
        return this;
    }

    public FluidSlot xeiRecipeSlot() {
        return xeiRecipeSlot(IngredientIO.NONE, 1);
    }

    public FluidSlot xeiRecipeSlot(IngredientIO io, float chance) {
        if (LDLib2.isJeiLoaded()) {
            JEISupport.recipeSlot(this, io);
        }
        return this;
    }

    public FluidSlot xeiRecipeSlot(IngredientIO io, float chance, int amount, Supplier<Stream<FluidStack>> allPossibleFluids) {
        if (LDLib2.isJeiLoaded()) {
            JEISupport.recipeSlot(this, io, allPossibleFluids);
        }
        return this;
    }

    /**
     * Container interaction for the item held on the cursor. NeoForge ports this through
     * FluidUtil + Capabilities; Fabric's equivalent is the Fabric Transfer API's
     * {@link ContainerItemContext} / {@link FluidStorage#ITEM}, which moves fluid and performs
     * the bucket &lt;-&gt; filled-bucket item exchange atomically through the cursor slot.
     * The {@code IFluidHandler} simulated/executed actions below mirror FluidUtil's
     * {@code simulate / execute} two-phase pattern.
     */
    private void tryClickContainer(boolean isShiftKeyDown) {
        if (boundHandler == null) return;
        var mui = getModularUI();
        if (mui == null || mui.getMenu() == null) return;
        var player = mui.player;
        if (player == null) return;
        clickContainer(boundHandler, tankIndex, player, mui.getMenu(), isShiftKeyDown, allowClickFilled, allowClickDrained);
    }

    /**
     * Static, UI-independent core of {@link #tryClickContainer(boolean)}, so the exact interaction can
     * be exercised without a live screen. Returns whether any fluid was moved, so callers/tests can tell
     * a successful exchange from a no-op.
     */
    public static boolean clickContainer(IFluidHandler handler, int tankIndex, Player player, AbstractContainerMenu menu,
                                  boolean isShiftKeyDown, boolean allowClickFilled, boolean allowClickDrained) {
        if (handler == null) return false;
        if (tankIndex < 0 || tankIndex >= handler.getTanks()) return false;
        var carried = menu.getCarried();
        if (carried.isEmpty()) return false;

        var context = ContainerItemContext.ofPlayerCursor(player, menu);
        var itemStorage = FluidStorage.ITEM.find(carried, context);
        if (itemStorage == null) return false;

        int maxAttempts = isShiftKeyDown ? carried.getCount() : 1;
        var initialFluid = handler.getFluidInTank(tankIndex);
        if (allowClickFilled && initialFluid.getAmount() > 0) {
            var variant = FluidStackHooksFabric.toFabric(initialFluid);
            long totalFilled = 0;
            for (int i = 0; i < maxAttempts; i++) {
                long moved = moveTankToItem(handler, itemStorage, variant, initialFluid.getAmount());
                if (moved <= 0) break;
                totalFilled += moved;
            }
            if (totalFilled > 0) {
                playSound(player, FluidHelper.getFillSound(initialFluid));
                return true;
            }
        }

        if (allowClickDrained) {
            long totalEmptied = 0;
            for (int i = 0; i < maxAttempts; i++) {
                long moved = moveItemToTank(handler, itemStorage);
                if (moved <= 0) break;
                totalEmptied += moved;
            }
            if (totalEmptied > 0) {
                playSound(player, FluidHelper.getEmptySound(handler.getFluidInTank(tankIndex)));
                return true;
            }
        }
        return false;
    }

    /**
     * Fills the cursor item from this slot's tank (equivalent to FluidUtil.tryFillContainer).
     */
    private static long moveTankToItem(IFluidHandler handler, Storage<FluidVariant> itemStorage, FluidVariant variant, long maxAmount) {
        long accepted;
        try (var tx = Transaction.openOuter()) {
            accepted = itemStorage.insert(variant, maxAmount, tx);
            // deliberately not committed: this is the simulated pass
        }
        if (accepted <= 0) return 0;
        var drainable = handler.drain(FluidStackHooksFabric.fromFabric(variant, accepted), IFluidHandler.FluidAction.SIMULATE).getAmount();
        long amount = Math.min(accepted, drainable);
        if (amount <= 0) return 0;
        try (var tx = Transaction.openOuter()) {
            long inserted = itemStorage.insert(variant, amount, tx);
            if (inserted <= 0) return 0;
            var drained = handler.drain(FluidStackHooksFabric.fromFabric(variant, inserted), IFluidHandler.FluidAction.EXECUTE);
            if (drained.getAmount() == inserted) {
                tx.commit();
                return inserted;
            }
        }
        return 0;
    }

    /**
     * Empties the cursor item into this slot's tank (equivalent to FluidUtil.tryEmptyContainer).
     */
    private static long moveItemToTank(IFluidHandler handler, Storage<FluidVariant> itemStorage) {
        try (var tx = Transaction.openOuter()) {
            var content = StorageUtil.findExtractableContent(itemStorage, tx);
            if (content == null || content.amount() <= 0) return 0;
            var resource = content.resource();
            var stack = FluidStackHooksFabric.fromFabric(resource, content.amount());
            long accepted = handler.fill(stack, IFluidHandler.FluidAction.SIMULATE);
            long amount = Math.min(content.amount(), accepted);
            if (amount <= 0) return 0;
            long extracted = itemStorage.extract(resource, amount, tx);
            if (extracted <= 0) return 0;
            long filled = handler.fill(FluidStackHooksFabric.fromFabric(resource, extracted), IFluidHandler.FluidAction.EXECUTE);
            if (filled == extracted) {
                tx.commit();
                return extracted;
            }
        }
        return 0;
    }

    private static void playSound(Player player, @Nullable SoundEvent sound) {
        if (sound == null) return;
        player.level().playSound(null, player.position().x, player.position().y + 0.5, player.position().z,
                sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    protected void onMouseDown(UIEvent event) {
        clickEvent.send(event.isShiftDown());
    }

    public FluidSlot setFluid(FluidStack fluid) {
        return setValue(fluid, true);
    }

    public FluidSlot setFluid(FluidStack fluid, boolean notify) {
        return setValue(fluid, notify);
    }

    /**
     * Delegates to {@link #getFullTooltipTexts(boolean)}, override that one instead of this.
     */
    public List<Component> getFullTooltipTexts() {
        return getFullTooltipTexts(true);
    }

    /**
     * @param withFluidName whether the display name of the fluid should be included.
     */
    public List<Component> getFullTooltipTexts(boolean withFluidName) {
        var tooltips = new ArrayList<Component>();
        if (slotStyle.showFluidTooltips()) {
            var fluidStack = getFluid();
            capacity = Math.max(capacity, (int) fluidStack.getAmount());
            if (!fluidStack.isEmpty()) {
                if (withFluidName) tooltips.add(FluidHelper.getDisplayName(fluidStack));
                tooltips.add(Component.translatable("ldlib.fluid.amount", fluidStack.getAmount(), capacity).append(" " + FluidHelper.getUnit()));
                tooltips.add(Component.translatable("ldlib.fluid.temperature", FluidHelper.getTemperature(fluidStack)));
                tooltips.add(Component.translatable(FluidHelper.isLighterThanAir(fluidStack) ? "ldlib.fluid.state_gas" : "ldlib.fluid.state_liquid"));
            } else {
                tooltips.add(Component.translatable("ldlib.fluid.empty"));
                tooltips.add(Component.translatable("ldlib.fluid.amount", 0, capacity).append(" " + FluidHelper.getUnit()));
            }
        }
        tooltips.addAll(getStyle().tooltips().asList());
        return tooltips;
    }

    public Component getFluidAmountText() {
        var renderedFluid = getValue();
        if (renderedFluid.isEmpty()) return Component.empty();
        return Component.literal(TextFormattingUtil.formatLongToCompactStringBuckets(renderedFluid.getAmount(), 3) + "B");
    }

    protected void onHoverTooltips(UIEvent event) {
        var item = getValue();
        if (item.isEmpty()) return;
        var withFluidName = event.customData != XEITooltipContext.RECIPE_SLOT;
        event.hoverTooltips = HoverTooltips.create(getFullTooltipTexts(withFluidName).toArray());
    }

    @Override
    public FluidStack getValue() {
        return fluid;
    }

    @Override
    public FluidSlot setValue(@Nullable FluidStack value, boolean notify) {
        if (value == null) value = FluidStack.empty();
        if (value.getFluid() == fluid.getFluid() && value.getAmount() == fluid.getAmount()) return this;
        this.fluid = value;
        if (notify) notifyListeners();
        return this;
    }

    /// Editor Support
    @ConfigSetter(field = "editorFluidDisplay")
    private void setEditorFluidDisplay(FluidStack fluidStack) {
        this.editorFluidDisplay = fluidStack;
        setValue(fluidStack, false);
        amountLabel.setValue(getFluidAmountText());
    }

    @SkipPersistedValue(field = "editorFluidDisplay")
    private boolean skipEditorFluidDisplay(FluidStack fluid) {
        return fluid.isEmpty();
    }

    @ConfigSetter(field = "allowXEILookup")
    private void setAllowXEILookup(boolean allowXEILookup) {
        this.allowXEILookup = allowXEILookup;
    }

    @SkipPersistedValue(field = "allowXEILookup")
    private boolean skipAllowXEILookup(boolean allowXEILookup) {
        return allowXEILookup;
    }

    @SkipPersistedValue(field = "capacity")
    private boolean skipCapacity(int capacity) {
        return capacity == 0;
    }

    @Override
    public void beforeDeserialize() {
        super.beforeDeserialize();
        this.editorFluidDisplay = FluidStack.empty();
    }

    @Override
    public void afterDeserialize() {
        super.afterDeserialize();
        if (!editorFluidDisplay.isEmpty()) {
            setValue(editorFluidDisplay, false);
        }
    }

    @Override
    public void loadXml(Element element) {
        if (element.hasAttribute("capacity")) {
            setCapacity(XmlUtils.getAsInt(element, "capacity", capacity));
        }
        if (element.hasAttribute("allow-xei-lookup")) {
            setAllowXEILookup(XmlUtils.getAsBoolean(element, "allow-xei-Lookup", allowXEILookup));
        }
        var fluid = XmlUtils.getFluidStack(element);
        if (!fluid.isEmpty()) {
            setEditorFluidDisplay(fluid);
        }
        super.loadXml(element);
    }

    protected void drawSlotOverlay(IGUIContext context, float contentX, float contentY, float contentWidth, float contentHeight) {
        context.drawTexture(this.getSlotStyle().slotOverlay(), contentX, contentY, contentWidth, contentHeight);
    }

    protected void drawFluid(IGUIContext context, FluidStack renderedFluid, float contentX, float contentY, float contentWidth, float contentHeight) {
        if (context instanceof GUIContext guiContext) {
            var fillDirection = this.getSlotStyle().fillDirection();
            double progress = renderedFluid.getAmount() * 1.0 / Math.max(Math.max(renderedFluid.getAmount(), this.getCapacity()), 1);
            float drawnU = (float) fillDirection.getDrawnU(progress);
            float drawnV = (float) fillDirection.getDrawnV(progress);
            float drawnWidth = (float) fillDirection.getDrawnWidth(progress);
            float drawnHeight = (float) fillDirection.getDrawnHeight(progress);
            DrawerHelperClient.drawFluidForGui(guiContext, renderedFluid,
                    contentX + drawnU * contentWidth,
                    contentY + drawnV * contentHeight,
                    contentWidth * drawnWidth,
                    contentHeight * drawnHeight, -1);
        }
    }

    protected void drawHover(IGUIContext context, float contentX, float contentY, float contentWidth, float contentHeight) {
        context.drawTexture(this.getSlotStyle().hoverOverlay(), contentX, contentY, contentWidth, contentHeight);
    }

    // region XEI Support
    // Fabric JEI fluid ingredients are FabricTypes.FLUID_STACK (Fluid, IJeiFluidIngredient), not
    // Architectury FluidStack. FluidStackHooksFabric carries the components both ways, so the
    // bridge is a real conversion rather than a lossy shim.
    public static class JEISupport {
        private static IJeiFluidIngredient toJei(FluidStack fluidStack) {
            return new JeiFluidIngredient(FluidStackHooksFabric.toFabric(fluidStack), fluidStack.getAmount());
        }

        private static FluidStack fromJei(IJeiFluidIngredient ingredient) {
            return FluidStackHooksFabric.fromFabric(ingredient.getFluidVariant(), ingredient.getAmount());
        }

        public static void clickableIngredient(FluidSlot fluidSlot) {
            LDLibJEIPlugin.clickableIngredient(fluidSlot, () -> {
                if (!fluidSlot.allowXEILookup) return null;
                var current = fluidSlot.getValue();
                if (current.isEmpty()) return null;
                return LDLibJEIPlugin.createTypedIngredient(FabricTypes.FLUID_STACK, toJei(current)).orElse(null);
            });
        }

        public static void ghostIngredient(FluidSlot fluidSlot) {
            LDLibJEIPlugin.ghostIngredient(fluidSlot, FabricTypes.FLUID_STACK,
                    ingredient -> true,
                    ingredient -> fluidSlot.setValue(fromJei(ingredient)));
        }

        public static void recipeIngredient(FluidSlot fluidSlot, IngredientIO io) {
            recipeIngredient(fluidSlot, io, () -> Stream.of(fluidSlot.getFluid()));
        }

        public static void recipeIngredient(FluidSlot fluidSlot, IngredientIO io, Supplier<Stream<FluidStack>> allPossibleFluids) {
            LDLibJEIPlugin.recipeIngredient(fluidSlot, io, () -> allPossibleFluids.get()
                    .map(fluidStack -> LDLibJEIPlugin.createTypedIngredient(FabricTypes.FLUID_STACK, toJei(fluidStack)))
                    .flatMap(Optional::stream)
                    .toList());
        }

        public static void recipeSlot(FluidSlot fluidSlot, IngredientIO io) {
            recipeSlot(fluidSlot, io, () -> Stream.of(fluidSlot.getFluid()));
        }

        public static void recipeSlot(FluidSlot fluidSlot, IngredientIO io, Supplier<Stream<FluidStack>> allPossibleFluids) {
            var updater = LDLibJEIPlugin.recipeSlot(
                    fluidSlot, io, FabricTypes.FLUID_STACK,
                    () -> allPossibleFluids.get().map(JEISupport::toJei),
                    ingredient -> fluidSlot.setFluid(ingredient == null ? FluidStack.empty() : fromJei(ingredient), false));
            fluidSlot.registerValueListener(fluidStack ->
                    updater.accept(fluidStack.isEmpty() ? null : toJei(fluidStack)));
        }
    }
    // endregion

    @LDLRegisterClient(name = "fluid_slot", registry = "ldlib2:ui_element_renderer")
    public static final class FluidSlotRenderer extends DelegatingUIElementRenderer<FluidSlot, FluidSlotRenderer> {
        @Override
        public Class<FluidSlot> type() {
            return FluidSlot.class;
        }

        @Override
        public void drawBackgroundAdditional(FluidSlot fluidSlot, IGUIContext context) {
            if (!(context instanceof GUIContext guiContext)) {
                drawParentBackgroundAdditional(fluidSlot, context);
                return;
            }
            drawBackgroundAdditional(fluidSlot, guiContext);
        }

        static void drawBackgroundAdditional(FluidSlot fluidSlot, GUIContext context) {
            var renderedFluid = fluidSlot.getValue();
            var hovered = fluidSlot.isHover() || fluidSlot.isSelfOrChildHover();
            var drawSlotOverlay = fluidSlot.getSlotStyle().showSlotOverlayOnlyEmpty() || !renderedFluid.isEmpty();

            if (renderedFluid.isEmpty() && !hovered && !drawSlotOverlay) return;

            var contentX = fluidSlot.getContentX();
            var contentY = fluidSlot.getContentY();
            var contentWidth = fluidSlot.getContentWidth();
            var contentHeight = fluidSlot.getContentHeight();

            if (renderedFluid.isEmpty() || !fluidSlot.getSlotStyle().showSlotOverlayOnlyEmpty()) {
                fluidSlot.drawSlotOverlay(context, contentX, contentY, contentWidth, contentHeight);
            }
            if (!renderedFluid.isEmpty()) {
                fluidSlot.drawFluid(context, renderedFluid, contentX, contentY, contentWidth, contentHeight);
            }
            if (hovered) {
                fluidSlot.drawHover(context, contentX, contentY, contentWidth, contentHeight);
            }
        }
    }
}
