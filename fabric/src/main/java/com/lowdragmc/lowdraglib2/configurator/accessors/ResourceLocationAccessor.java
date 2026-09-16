package com.lowdragmc.lowdraglib2.configurator.accessors;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigRL;
import com.lowdragmc.lowdraglib2.configurator.annotation.DefaultValue;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.configurator.ui.SearchComponentConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.StringConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.TagKeySearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote ResourceLocationAccessor
 */
@LDLRegisterClient(name = "resource_location", registry = "ldlib2:configurator_accessor")
public class ResourceLocationAccessor extends TypesAccessor<Identifier> {

    public ResourceLocationAccessor() {
        super(Identifier.class);
    }

    @Override
    public Identifier defaultValue(@Nullable Field field, @Nullable Class<?> type) {
        if (field != null && field.isAnnotationPresent(DefaultValue.class)) {
            return Identifier.parse(field.getAnnotation(DefaultValue.class).stringValue()[0]);
        }
        return LDLib2.id("default");
    }

    @Override
    public Configurator create(String name, Supplier<Identifier> supplier, Consumer<Identifier> consumer, boolean forceUpdate, @Nullable Field field, @Nullable Object owner) {
        if (field != null && field.isAnnotationPresent(ConfigRL.class)) {
            var rlConfig = field.getAnnotation(ConfigRL.class);
            return switch (rlConfig.value()) {
                case FONT -> new SearchComponentConfigurator<>(name, supplier, consumer, defaultValue(field, String.class), forceUpdate,
                        (word, handler) -> {
                            var search = word.toLowerCase();
                            // fontManager.fontSets is private in Fabric, use reflection
                            try {
                                var fontManagerField = Minecraft.class.getDeclaredField("fontManager");
                                fontManagerField.setAccessible(true);
                                var fontManager = fontManagerField.get(Minecraft.getInstance());
                                var fontSetsField = fontManager.getClass().getDeclaredField("fontSets");
                                fontSetsField.setAccessible(true);
                                @SuppressWarnings("unchecked")
                                var fontSets = (Map<Identifier, ?>) fontSetsField.get(fontManager);
                                for (var fontName : fontSets.keySet()) {
                                    if (Thread.currentThread().isInterrupted()) return;
                                    if (fontName.toString().contains(search)) {
                                        handler.accept(fontName);
                                    }
                                }
                            } catch (Exception e) {
                                LDLib2.LOGGER.error("Failed to access fontSets via reflection", e);
                            }
                        }, Identifier::toString, UIElementProvider.text(font -> font == null ?
                        Component.literal("---") : Component.literal(font.toString()))
                );
                case ITEM_TAG_KEY -> new TagKeySearchComponent.Item(name,
                        () -> TagKey.create(Registries.ITEM, supplier.get()), tagKey -> consumer.accept(tagKey.location()),
                        TagKey.create(Registries.ITEM, defaultValue(field, Identifier.class)),
                        forceUpdate
                );
                case BLOCK_TAG_KEY -> new TagKeySearchComponent.Block(name,
                        () -> TagKey.create(Registries.BLOCK, supplier.get()), tagKey -> consumer.accept(tagKey.location()),
                        TagKey.create(Registries.BLOCK, defaultValue(field, Identifier.class)),
                        forceUpdate
                );
                case FLUID_TAG_KEY -> new TagKeySearchComponent.Fluid(name,
                        () -> TagKey.create(Registries.FLUID, supplier.get()), tagKey -> consumer.accept(tagKey.location()),
                        TagKey.create(Registries.FLUID, defaultValue(field, Identifier.class)),
                        forceUpdate
                );
                case ENTITY_TYPE_TAG_KEY -> new TagKeySearchComponent<net.minecraft.world.entity.EntityType<?>>(name,
                        () -> (TagKey) TagKey.create(Registries.ENTITY_TYPE, supplier.get()), tagKey -> consumer.accept(((TagKey) tagKey).location()),
                        (TagKey) TagKey.create(Registries.ENTITY_TYPE, defaultValue(field, Identifier.class)),
                        forceUpdate, null, null
                );
            };
        }
        var configurator = new StringConfigurator(name,
                () -> supplier.get().toString(),
                s -> consumer.accept(Identifier.parse(s)),
                defaultValue(field, String.class).toString(),
                forceUpdate).setResourceLocation(true);
        configurator.setPastable(String.class, pasted -> {
            if (pasted != null && LDLib2.isValidResourceLocation(pasted)) {
                consumer.accept(Identifier.parse(pasted));
                configurator.notifyChanges();
            }
        });
        return configurator;
    }
}
