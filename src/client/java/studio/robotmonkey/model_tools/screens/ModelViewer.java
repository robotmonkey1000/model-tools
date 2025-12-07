package studio.robotmonkey.model_tools.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import studio.robotmonkey.model_tools.ModelToolsClient;
import studio.robotmonkey.model_tools.gui.widgets.ToggleGroup;
import studio.robotmonkey.model_tools.gui.widgets.ToggleItemWidget;
import studio.robotmonkey.model_tools.mixin.client.ItemEntityAccessor;
import studio.robotmonkey.model_tools.packets.Packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class ModelViewer extends Screen {

//    private HeaderAndFooterLayout layout;
    enum RenderDisplay { ITEM,HELMET,CHEST,LEGS,BOOTS }

    RenderDisplay displayType = RenderDisplay.ITEM;

    private FrameLayout modelsFrame;
    private HashMap<String, ModelSelectionList> modelSelections = new HashMap<>();
    private String selected = "minecraft";
    public ModelViewer() {
        // The parameter is the title of the screen,
        // which will be narrated when you enter the screen.
        super(Component.literal("My tutorial screen"));
        this.modelsFrame = new FrameLayout();
        renderSelector = new ToggleGroup();
    }

    private final ArrayList<Button> packButtons = new ArrayList<>();
    public ToggleItemWidget itemRenderButton;
    public ToggleItemWidget helmetRenderButton;
    public ToggleItemWidget chestRenderButton;
    public ToggleItemWidget legsRenderButton;
    public ToggleItemWidget bootsRenderButton;

    int buttonTopPadding = 1;
    int buttonShift = 5;
    int buttonSize = 20;
    int iconPadding = 2;
    float rotation = 0;
    float rotationSpeed = 0.1f; //TODO tweak speed
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta); //0.0625F
        if(displayType == RenderDisplay.HELMET || displayType == RenderDisplay.CHEST || displayType == RenderDisplay.LEGS || displayType == RenderDisplay.BOOTS) {
            renderEntityInInventoryFollowsMouse(guiGraphics, width/2, 0, width, height, 75, 0.0625F, mouseX, mouseY, this.minecraft.player);
        }
        else
        {

            renderItemEntity(guiGraphics, this.minecraft.player);

        }

        guiGraphics.renderItem(new ItemStack(Items.CARVED_PUMPKIN),     iconPadding + width - buttonSize  * 5 - buttonShift * 4, iconPadding + buttonTopPadding);
        guiGraphics.renderItem(new ItemStack(Items.DIAMOND_HELMET),     iconPadding + width - buttonSize * 4 - buttonShift * 3, iconPadding + buttonTopPadding);
        guiGraphics.renderItem(new ItemStack(Items.DIAMOND_CHESTPLATE), iconPadding + width - buttonSize * 3 - buttonShift * 2, iconPadding + buttonTopPadding);
        guiGraphics.renderItem(new ItemStack(Items.DIAMOND_LEGGINGS),   iconPadding + width - buttonSize * 2 - buttonShift, iconPadding + buttonTopPadding);
        guiGraphics.renderItem(new ItemStack(Items.DIAMOND_BOOTS),      iconPadding + width - buttonSize, iconPadding + buttonTopPadding);


    }

    public void renderItemEntity(GuiGraphics guiGraphics, LivingEntity player) {
        ClientLevel level = Minecraft.getInstance().level;
        ItemEntity entity = new ItemEntity(EntityType.ITEM, level);

        ItemStack itemStack = player.getMainHandItem().copy();
        if(modelSelections.containsKey(selected))
        {
            if(modelSelections.get(selected).getSelected() != null)
            {
                String model = modelSelections.get(selected).getSelected().model_path.replaceFirst("items/", "");
                model = model.replaceFirst(".json", "");
                itemStack.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(selected, model));
            }
        }

        entity.setYRot(0);
        entity.setNoGravity(true);
        ((ItemEntityAccessor)(Object)entity).setAge(0);

        entity.setItem(itemStack);
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super ItemEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(entity);
//            ItemEntityRenderer
        EntityRenderState entityRenderState = entityRenderer.createRenderState(entity, 0.5f);
        ((ItemEntityRenderState)entityRenderState).bobOffset = 0;
        entityRenderState.lightCoords = 15728880;
        entityRenderState.hitboxesRenderState = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        entityRenderState.ageInTicks = rotation;
        rotation += rotationSpeed;

        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F).rotateY(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(1 * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        guiGraphics.submitEntityRenderState(entityRenderState, 75, new Vector3f(), quaternionf, quaternionf2, width/2, 0, width, height);

    }


    public void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, int scale, float f, float mouseX, float mouseY, LivingEntity livingEntity) {
        float width = (float)(topLeftX + bottomRightX) / 2.0F;
        float height = (float)(topLeftY + bottomRightY) / 2.0F;
//        guiGraphics.enableScissor(i, j, k, l);
        float p = (float)Math.atan((double)((width - mouseX) / 40.0F));
        float q = (float)Math.atan((double)((height - mouseY) / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(q * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);
        float r = livingEntity.yBodyRot;
        float s = livingEntity.getYRot();
        float t = livingEntity.getXRot();
        float u = livingEntity.yHeadRotO;
        float v = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + p * 20.0F;
        livingEntity.setYRot(180.0F + p * 40.0F);
        livingEntity.setXRot(-q * 20.0F);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        float w = livingEntity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, livingEntity.getBbHeight() / 2.0F + f * w, 0.0F);
        float x = (float)scale / w;

        ItemStack mainHand = livingEntity.getMainHandItem();
        ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = livingEntity.getItemBySlot(EquipmentSlot.FEET);

        //Use held item
        ItemStack itemStack = mainHand.copy();
        switch(displayType) {
            case HELMET -> {
                setModel(itemStack, EquipmentSlot.HEAD);
                livingEntity.setItemSlot(EquipmentSlot.HEAD, itemStack);
            }
            case CHEST -> {
                setModel(itemStack, EquipmentSlot.CHEST);
                livingEntity.setItemSlot(EquipmentSlot.CHEST, itemStack);
            }

            case LEGS -> {
                setModel(itemStack, EquipmentSlot.LEGS);
                livingEntity.setItemSlot(EquipmentSlot.LEGS, itemStack);
            }

            case BOOTS -> {
                setModel(itemStack, EquipmentSlot.FEET);
                livingEntity.setItemSlot(EquipmentSlot.FEET, itemStack);
            }
        }



        renderEntityInInventory(guiGraphics, topLeftX, topLeftY, bottomRightX, bottomRightY, x, vector3f, quaternionf, quaternionf2, livingEntity);

        //Reset slot here:
        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        livingEntity.setItemSlot(EquipmentSlot.HEAD, helmet);
        livingEntity.setItemSlot(EquipmentSlot.CHEST, chest);
        livingEntity.setItemSlot(EquipmentSlot.LEGS, legs);
        livingEntity.setItemSlot(EquipmentSlot.FEET, boots);
        livingEntity.yBodyRot = r;
        livingEntity.setYRot(s);
        livingEntity.setXRot(t);
        livingEntity.yHeadRotO = u;
        livingEntity.yHeadRot = v;
//        guiGraphics.disableScissor();
    }

    private void setModel(ItemStack itemStack, EquipmentSlot slot)
    {
        if(modelSelections.containsKey(selected))
        {
            if(modelSelections.get(selected).getSelected() != null)
            {
                String model = modelSelections.get(selected).getSelected().model_path.replaceFirst("items/", "");
                model = model.replaceFirst("equipment/", "");

                model = model.replaceFirst(".json", "");
                itemStack.set(DataComponents.ITEM_MODEL, ResourceLocation.fromNamespaceAndPath(selected, model));
                ResourceKey<EquipmentAsset> assetResourceKey = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(selected, model));
                if(!itemStack.isStackable())
                    itemStack.set(DataComponents.EQUIPPABLE, Equippable.builder(slot).setAsset(assetResourceKey).build());
            }
        }
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, int i, int j, int k, int l, float f, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(livingEntity, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.hitboxesRenderState = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        guiGraphics.submitEntityRenderState(entityRenderState, f, vector3f, quaternionf, quaternionf2, i, j, k, l);
    }

    private final int contentWidth = this.width / 8;
    private final int topPadding = 30;
    private final int bottomPadding = 30;

    private ToggleGroup renderSelector;


    private ToggleItemWidget setModelButton;
    private ToggleItemWidget setEquipmentButton;

    @Override
    protected void init() {
        renderSelector = new ToggleGroup();
        resetView();
        addRenderButtons();
        addPackButtons();

        this.modelsFrame.visitWidgets((guiEventListener) -> {
            AbstractWidget var10000 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });

        Component no_server = Component.literal("No server connection");
        setModelButton = new ToggleItemWidget(this.minecraft, 2,2, buttonSize, buttonSize,
                Component.literal("Set as item model"), new ItemStack(Items.ARMOR_STAND), false, false,
                button -> {
                    SendApplyPacket(Packets.ApplyOperation.ItemOperation.ITEM);
                });

        setEquipmentButton = new ToggleItemWidget(this.minecraft, 4 + buttonSize,2, buttonSize, buttonSize,
                Component.literal("Set equipment slot, and texture"), new ItemStack(Items.LEATHER_CHESTPLATE), false, false,
                button -> {
                    SendApplyPacket(Packets.ApplyOperation.ItemOperation.ARMOR);
                });

        refreshState();


        if(!ModelToolsClient.hasServerConnection)
        {
            setEquipmentButton.setTooltip(Tooltip.create(no_server));
            setModelButton.setTooltip(Tooltip.create(no_server));
        }
        addRenderableWidget(setModelButton);
        addRenderableWidget(setEquipmentButton);

//        ToggleItemWidget button = new ToggleItemWidget(this.minecraft,
//                2, 2,
//                buttonSize, buttonSize,
//                Component.literal("Set Booties!"),
//                new ItemStack(Items.CARVED_PUMPKIN),
//                false,
//                true,
//                button1 -> { button1.setStateTriggered(!button1.isStateTriggered());}
//        );
//
//        addRenderableWidget(button);

//        repositionElements();
    }

    private void SendApplyPacket(Packets.ApplyOperation.ItemOperation operation)
    {
        if(!ModelToolsClient.hasServerConnection) return;

        ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath("","");
        if(modelSelections.get(selected) == null || modelSelections.get(selected).getSelected() == null)
        {
            //TODO add a "select a model below" message?
            ModelToolsClient.LOGGER.info("No model selected.");

        }
        else {
            String model = modelSelections.get(selected).getSelected().model_path.replaceFirst("items/", "");
            model = model.replaceFirst("equipment/", "");

            model = model.replaceFirst(".json", "");

            modelLoc = ResourceLocation.fromNamespaceAndPath(selected, model);

        }


        EquipmentSlot slot = EquipmentSlot.MAINHAND;

        switch(displayType)
        {
            case ITEM -> {

            }
            case HELMET -> {
                slot = EquipmentSlot.HEAD;
            }
            case CHEST -> {
                slot = EquipmentSlot.CHEST;
            }
            case LEGS -> {
                slot = EquipmentSlot.LEGS;
            }
            case BOOTS -> {
                slot = EquipmentSlot.FEET;
            }
        }

        Packets.ApplyOperation payload = new Packets.ApplyOperation(operation, modelLoc, slot);
        ClientPlayNetworking.send(payload);
    }

    public void refreshState() {

        setModelButton.setStateTriggered(ModelToolsClient.hasServerConnection);
        setEquipmentButton.setStateTriggered(ModelToolsClient.hasServerConnection);

        if(ModelToolsClient.hasServerConnection) {
            setEquipmentButton.setStateTriggered(displayType != RenderDisplay.ITEM);
        }
    }


    @Override
    protected void repositionElements() {
//        rebuildWidgets();
        arrangeElements();
        if (this.modelSelections.get(selected) != null) {
            this.modelSelections.get(selected).updateSizeAndPosition(this.width/2, this.getContentHeight(), this.width/4, topPadding);
        }

        bootsRenderButton.setPosition(width - buttonSize, buttonTopPadding);
        legsRenderButton.setPosition(width - buttonSize * 2 - buttonShift, buttonTopPadding);
        chestRenderButton.setPosition(width - buttonSize * 3 - buttonShift * 2, buttonTopPadding);
        helmetRenderButton.setPosition(width - buttonSize * 4 - buttonShift * 3, buttonTopPadding);
        itemRenderButton.setPosition(width - buttonSize  * 5 - buttonShift * 4, buttonTopPadding);

    }

    private void resetView() {
        modelsFrame = new FrameLayout();
        modelSelections.clear();
    }

    private void addRenderButtons() {
        bootsRenderButton = new ToggleItemWidget(this.minecraft,
                width - buttonSize, buttonTopPadding,
                buttonSize, buttonSize,
                Component.literal("Preview as boots."),
                new ItemStack(Items.DIAMOND_BOOTS),
                false,
                false,
                button -> {
                    displayType = RenderDisplay.BOOTS;
                    refreshState();
                });

        legsRenderButton = new ToggleItemWidget(this.minecraft,
                width - buttonSize * 2 - buttonShift, buttonTopPadding,
                buttonSize, buttonSize,
                Component.literal("Preview as legs."),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                false,
                false,
                button -> {
                    displayType = RenderDisplay.LEGS;
                    refreshState();
                });

        chestRenderButton = new ToggleItemWidget(this.minecraft,
                width - buttonSize * 3 - buttonShift * 2, buttonTopPadding,
                buttonSize, buttonSize,
                Component.literal("Preview as chest."),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                false,
                false,
                button -> {
                    displayType = RenderDisplay.CHEST;
                    refreshState();
                });

        helmetRenderButton = new ToggleItemWidget(this.minecraft,
                width - buttonSize * 4 - buttonShift * 3, buttonTopPadding,
                buttonSize, buttonSize,
                Component.literal("Preview as helmet."),
                new ItemStack(Items.DIAMOND_HELMET),
                false,
                false,
                button -> {
                    displayType = RenderDisplay.HELMET;
                    refreshState();
                });

        itemRenderButton = new ToggleItemWidget(this.minecraft,
                width - buttonSize  * 5 - buttonShift * 4, buttonTopPadding,
                buttonSize, buttonSize,
                Component.literal("Preview as item."),
                new ItemStack(Items.CARVED_PUMPKIN),
                false,
                false,
                button -> {
                    displayType = RenderDisplay.ITEM;
                    refreshState();
                });

        renderSelector.AddWidget(bootsRenderButton);
        renderSelector.AddWidget(legsRenderButton);
        renderSelector.AddWidget(chestRenderButton);
        renderSelector.AddWidget(helmetRenderButton);
        renderSelector.AddWidget(itemRenderButton);
        renderSelector.WidgetPressed(itemRenderButton);

        addRenderableWidget(bootsRenderButton);
        addRenderableWidget(legsRenderButton);
        addRenderableWidget(chestRenderButton);
        addRenderableWidget(helmetRenderButton);
        addRenderableWidget(itemRenderButton);


    }

    private void addPackButtons() {
        packButtons.clear();

        int packCount = 0;
        for(String pack: ModelToolsClient.LoadedModels.keySet())
        {
//            ModelToolsClient.LOGGER.info(pack);
            Button packButton = Button.builder(Component.literal(pack), button -> {
                        selected = button.getMessage().getString();
                        this.modelsFrame.visitWidgets(this::removeWidget);


                        this.modelsFrame = new FrameLayout();

                        this.modelsFrame.addChild(modelSelections.get(button.getMessage().getString()));

                        this.modelsFrame.visitWidgets((guiEventListener) -> {
                            AbstractWidget var10000 = (AbstractWidget)this.addRenderableWidget(guiEventListener);

                        });

                        repositionElements();

                    })
                    .bounds(10, topPadding-1 + 25*packCount, 100, 20)
                .tooltip(Tooltip.create(Component.literal(pack + "'s models")))
                    .build();
            packCount++;

            packButtons.add(packButton);
            addRenderableWidget(packButton);

            modelSelections.put(pack, new ModelSelectionList(this.minecraft, pack));
        }
    }

    public int getContentHeight() {
        return this.height - this.topPadding - this.bottomPadding;
    }

    public void arrangeElements() {
        this.modelsFrame.setMinWidth(this.contentWidth);
        this.modelsFrame.arrangeElements();
        int k = topPadding + 30;
        int l = this.height - bottomPadding - this.modelsFrame.getHeight();
        this.modelsFrame.setPosition(0, Math.min(k, l));

    }

    @Environment(EnvType.CLIENT)
    private class ModelSelectionList extends ObjectSelectionList<ModelSelectionList.Entry> {

        public ModelSelectionList(final Minecraft minecraft, String namespace) {
            super(minecraft, ModelViewer.this.width/2, ModelViewer.this.getContentHeight(), topPadding, 18);
            ModelToolsClient.LoadedModels.get(namespace).forEach((path) -> {
                ModelViewer.ModelSelectionList.Entry entry = new ModelViewer.ModelSelectionList.Entry(path);
                this.addEntry(entry);
            });
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }

        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Environment(EnvType.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<ModelSelectionList.Entry> {

            String model_path;
            public Entry(String modelPath) {
                this.model_path = modelPath;
            }


            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.model_path);
            }


            @Override
            public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
                Font font = ModelViewer.this.font;
                Component text = Component.literal(this.model_path);
                int size = ModelViewer.this.width/2;
                int height = this.getContentYMiddle();
                Objects.requireNonNull(ModelViewer.this.font);
                guiGraphics.drawCenteredString(font, text, size, height - 9 / 2, -1);
            }

            public boolean keyPressed(KeyEvent keyEvent) {
                if (keyEvent.isSelection()) {
                    this.select();
//                ModelViewer.this.onDone(); //TODO selection logic
                    return true;
                } else {
                    return super.keyPressed(keyEvent);
                }
            }

            public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
                this.select();
                if (bl) {
//                    ModelViewer.this.onDone(); //TODO selection logic
                }

                return super.mouseClicked(mouseButtonEvent, bl);
            }

            private void select() {
                ModelViewer.ModelSelectionList.this.setSelected(this);
            }
        }



    }
}
