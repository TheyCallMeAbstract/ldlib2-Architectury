package com.lowdragmc.lowdraglib2;

import com.lowdragmc.lowdraglib2.editor.resource.BuiltinResourceProvider;
import com.lowdragmc.lowdraglib2.editor.resource.FileResourceProvider;
import com.lowdragmc.lowdraglib2.editor.resource.ResourceProviderType;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.LDLRegistry;
import com.lowdragmc.lowdraglib2.test.TestBlock;
import com.lowdragmc.lowdraglib2.test.TestBlockEntity;
import com.lowdragmc.lowdraglib2.test.TestItem;
import com.lowdragmc.lowdraglib2.test.ui.IMenuTest;
import com.lowdragmc.lowdraglib2.uitest.mp.MPScenario;
import com.lowdragmc.lowdraglib2.client.renderer.block.RendererBlock;
import com.lowdragmc.lowdraglib2.client.renderer.block.RendererBlockEntity;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class LDLib2Registries {
    // Block / item / block entity type registrations using Architectury DeferredRegister
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(LDLib2.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(LDLib2.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(LDLib2.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(LDLib2.MOD_ID, Registries.CREATIVE_MODE_TAB);

    // Dev-only test block/item (mirrors CommonProxy), plus the unconditional renderer block/item.
    public static final RegistrySupplier<TestBlock> TEST_BLOCK = Platform.isDevEnv()
            ? BLOCKS.register("test", () -> new TestBlock(blockProperties("test")))
            : null;
    public static final RegistrySupplier<TestItem> TEST_ITEM = Platform.isDevEnv()
            ? ITEMS.register("test", () -> new TestItem(itemProperties("test")))
            : null;
    public static final RegistrySupplier<RendererBlock> RENDERER_BLOCK =
            BLOCKS.register("renderer_block", () -> new RendererBlock(blockProperties("renderer_block")));
    public static final RegistrySupplier<Item> RENDERER_BLOCK_ITEM =
            ITEMS.register("renderer_block", () -> new BlockItem(RendererBlock.BLOCK, itemProperties("renderer_block")));

    // Block entity types. Suppliers fail soft if the backing block is not registered.
    @Nullable
    public static final RegistrySupplier<BlockEntityType<TestBlockEntity>> TEST_BE_TYPE = Platform.isDevEnv()
            ? BLOCK_ENTITY_TYPES.register(LDLib2.id("test"), () -> buildBlockEntityType(TestBlockEntity::new, TestBlock.BLOCK, "test"))
            : null;
    public static final RegistrySupplier<BlockEntityType<RendererBlockEntity>> RENDERER_BE_TYPE =
            BLOCK_ENTITY_TYPES.register(LDLib2.id("renderer_block"), () -> buildBlockEntityType(RendererBlockEntity::new, RendererBlock.BLOCK, "renderer_block"));

    // Dev-only creative tab (mirrors CommonListeners.ModCreativeModeTab).
    @Nullable
    public static final RegistrySupplier<CreativeModeTab> LDLIB2_DEV_TAB = Platform.isDevEnv()
            ? CREATIVE_MODE_TABS.register("ldlib2_dev_tab", () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.ldlib2.dev_tab"))
                    .icon(() -> new ItemStack(TestItem.ITEM.getBlock()))
                    .displayItems((parameters, output) -> output.accept(TestItem.ITEM.getBlock()))))
            : null;

    public final static AutoRegistry.LDLibRegister<UIElement, Supplier<UIElement>> UI_ELEMENTS = AutoRegistry.LDLibRegister
            .create(LDLib2.id("ui_element"), UIElement.class, AutoRegistry::noArgsCreator);

    public final static LDLRegistry.String<ResourceProviderType> RESOURCE_PROVIDER_TYPES = new LDLRegistry.String<>(LDLib2.id("resource_provider_types"));

    @Nullable
    public static AutoRegistry.LDLibRegister<IMenuTest, Supplier<IMenuTest>> MENU_TESTS;

    /**
     * Multi-process test scenarios. Deliberately a dist-neutral registry: the dedicated-server
     * process of a {@code runMpTest} run discovers scenarios through it too.
     *
     * @see com.lowdragmc.lowdraglib2.uitest.mp.MPScenario
     */
    @Nullable
    public static AutoRegistry.LDLibRegister<MPScenario, Supplier<MPScenario>> MP_SCENARIOS;

    static {
        if (Platform.isDevEnv()) {
            MP_SCENARIOS = AutoRegistry.LDLibRegister.create(LDLib2.id("mp_scenario"), MPScenario.class, AutoRegistry::noArgsCreator);
            MENU_TESTS = AutoRegistry.LDLibRegister.create(LDLib2.id("menu_test"), IMenuTest.class, AutoRegistry::noArgsCreator);
            for (var menuTest : MENU_TESTS) {
                PlayerUIMenuType.register(LDLib2.id(menuTest.annotation().name()), player -> {
                    var test = menuTest.value().get();
                    test.init(player);
                    return test;
                });
            }
        }
    }

    public static void init() {
        RESOURCE_PROVIDER_TYPES.register(BuiltinResourceProvider.TYPE.getTypeName(), BuiltinResourceProvider.TYPE);
        RESOURCE_PROVIDER_TYPES.register(FileResourceProvider.TYPE.getTypeName(), FileResourceProvider.TYPE);

        // Register blocks before block entity types so their static BLOCK fields are populated
        // when the block entity suppliers are evaluated.
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
        if (LDLIB2_DEV_TAB != null) {
            CREATIVE_MODE_TABS.register();
        }
    }

    private static BlockBehaviour.Properties blockProperties(String name) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, LDLib2.id(name)));
    }

    private static Item.Properties itemProperties(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, LDLib2.id(name)));
    }

    private static <T extends BlockEntity> BlockEntityType<T> buildBlockEntityType(
            FabricBlockEntityTypeBuilder.Factory<T> factory, @Nullable Block block, String name) {
        if (block == null) {
            LDLib2.LOGGER.warn("Skipping block entity type {} registration: backing block is not registered", name);
            return FabricBlockEntityTypeBuilder.create(factory).build();
        }
        return FabricBlockEntityTypeBuilder.create(factory, block).build();
    }
}
