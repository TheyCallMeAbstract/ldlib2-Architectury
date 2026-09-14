package com.lowdragmc.lowdraglib2.configurator.ui;

import com.google.common.base.Predicates;
import com.lowdragmc.lowdraglib2.gui.texture.FluidStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import dev.architectury.fluid.FluidStack;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TagKeySearchComponent<T> extends SearchComponentConfigurator<TagKey<T>> {
    public final Registry<T> registry;
    @Setter @Accessors(chain = true)
    protected Predicate<TagKey<T>> filter = Predicates.alwaysTrue();

    public TagKeySearchComponent(String name, Supplier<TagKey<T>> supplier, Consumer<TagKey<T>> onUpdate, TagKey<T> defaultValue,
                                 boolean forceUpdate, Registry<T> registry, UIElementProvider<TagKey<T>> uiProvider) {
        super(name, supplier, onUpdate, new SearchComponentConfigurator.ISearchConfigurator<>() {
            @Override
            public TagKey<T> defaultValue() {
                return defaultValue;
            }

            @Override
            public void search(String word, IResultHandler<TagKey<T>> searchHandler) {}

            @Override
            public String resultText(TagKey<T> value) {
                return value.location().toString();
            }

            @Override
            public UIElementProvider<TagKey<T>> candidateUIProvider() {
                return uiProvider;
            }
        }, forceUpdate);
        this.registry = registry;
    }

    @Override
    public void search(String word, IResultHandler<TagKey<T>> searchHandler) {
        if (registry == null) return;
        var lowerWord = word.toLowerCase();
        for (var tag : registry.getTags().toList()) {
            if (Thread.currentThread().isInterrupted()) return;
            var tagKey = tag.key();
            if (!filter.test(tagKey)) continue;
            if (tagKey.location().toString().contains(lowerWord)) {
                searchHandler.acceptResult(tagKey);
            }
        }
    }

    public static class Item extends TagKeySearchComponent<net.minecraft.world.item.Item> {
        public Item(String name, Supplier<TagKey<net.minecraft.world.item.Item>> supplier, Consumer<TagKey<net.minecraft.world.item.Item>> onUpdate, TagKey<net.minecraft.world.item.Item> defaultValue, boolean forceUpdate) {
            super(name, supplier, onUpdate, defaultValue, forceUpdate, BuiltInRegistries.ITEM, UIElementProvider.iconText(
                    tagKey -> IGuiTexture.EMPTY,
                    tagKey -> Component.literal(tagKey.location().toString())
            ));
        }
    }

    public static class Block extends TagKeySearchComponent<net.minecraft.world.level.block.Block> {
        public Block(String name, Supplier<TagKey<net.minecraft.world.level.block.Block>> supplier, Consumer<TagKey<net.minecraft.world.level.block.Block>> onUpdate, TagKey<net.minecraft.world.level.block.Block> defaultValue, boolean forceUpdate) {
            super(name, supplier, onUpdate, defaultValue, forceUpdate, BuiltInRegistries.BLOCK, UIElementProvider.iconText(
                    tagKey -> IGuiTexture.EMPTY,
                    tagKey -> Component.literal(tagKey.location().toString())
            ));
        }
    }

    public static class Fluid extends TagKeySearchComponent<net.minecraft.world.level.material.Fluid> {
        public Fluid(String name, Supplier<TagKey<net.minecraft.world.level.material.Fluid>> supplier, Consumer<TagKey<net.minecraft.world.level.material.Fluid>> onUpdate, TagKey<net.minecraft.world.level.material.Fluid> defaultValue, boolean forceUpdate) {
            super(name, supplier, onUpdate, defaultValue, forceUpdate, BuiltInRegistries.FLUID, UIElementProvider.iconText(
                    tagKey -> {
                        return IGuiTexture.EMPTY;
                    },
                    tagKey -> Component.literal(tagKey.location().toString())
            ));
        }
    }
}
