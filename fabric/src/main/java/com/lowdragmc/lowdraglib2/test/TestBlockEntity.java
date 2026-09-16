package com.lowdragmc.lowdraglib2.test;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.syncdata.IManaged;
import com.lowdragmc.lowdraglib2.syncdata.storage.IManagedStorage;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.syncdata.annotation.*;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.FlexDirection;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TestBlockEntity extends BlockEntity implements IManaged {
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Persisted
    @DescSynced
    @UpdateListener(methodName = "onIntValueChanged")
    @ConditionalSynced(methodName = "shouldSyncIntValue")
    private int intValue = 10;

    public TestBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(com.lowdragmc.lowdraglib2.LDLib2Registries.TEST_BE_TYPE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return syncStorage;
    }

    @Override
    public void notifyPersistence() {
        setChanged();
    }

    private void onIntValueChanged(int oldValue, int newValue) {
        LDLib2.LOGGER.info("Int value changed from {} to {}", oldValue, newValue);
    }

    private boolean shouldSyncIntValue(int value) {
        return value > 0;
    }

    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        var root = new UIElement().layout(layout -> layout
                .paddingAll(4)
                .gapAll(2)
                .justifyContent(AlignContent.CENTER)
        ).addClass("panel_bg");
        root.addChild(new Label().setText("Test Block UI"));
        root.addChild(new TextField());
        root.addChild(new Button().setText("Change Random Value").setOnServerClick(e -> {
            intValue = (int) (Math.random() * 100);
        }));
        root.addChild(
                new UIElement().layout(layout -> layout.widthPercent(100).flexDirection(FlexDirection.ROW))
                        .addChildren(
                                new Button().setText("+").setOnServerClick(e -> intValue++).layout(l -> l.flex(1)).setId("btn_inc"),
                                new Button().setText("-").setOnServerClick(e -> intValue--).layout(l -> l.flex(1)).setId("btn_dec")
                        )
        );
        root.addChild(new Label().bindDataSource(com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource.of(() -> Component.literal(String.valueOf(intValue)))).setId("int_value_label"));
        return new ModularUI(UI.of(root), holder.player);
    }
}
