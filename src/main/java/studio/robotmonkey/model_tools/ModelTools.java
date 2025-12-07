package studio.robotmonkey.model_tools;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.robotmonkey.model_tools.packets.Packets;

public class ModelTools implements ModInitializer {
	public static final String MOD_ID = "model-tools";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		PayloadTypeRegistry.playS2C().register(Packets.ConnectServer2C.ID, Packets.ConnectServer2C.CODEC);
		PayloadTypeRegistry.playC2S().register(Packets.ConnectClient2S.ID, Packets.ConnectClient2S.CODEC);
		PayloadTypeRegistry.playC2S().register(Packets.ApplyOperation.ID, Packets.ApplyOperation.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(Packets.ConnectClient2S.ID, ((payload, context) -> {
			LOGGER.info(context.player().getPlainTextName() + " connected with Model Tools Installed!");
			ServerPlayNetworking.send(context.player(), new Packets.ConnectServer2C(context.player().getOnPos()));
		}));

		ServerPlayNetworking.registerGlobalReceiver(Packets.ApplyOperation.ID, ((payload, context) -> {
			switch(payload.operation())
			{
				case ITEM -> context.player().getMainHandItem().set(DataComponents.ITEM_MODEL, payload.model());
				case ARMOR -> {

					Equippable.Builder builder = Equippable.builder(payload.slot());

					if(!payload.model().getPath().isEmpty())
					{
						ResourceKey<EquipmentAsset> assetResourceKey = ResourceKey.create(EquipmentAssets.ROOT_ID, payload.model());
						builder.setAsset(assetResourceKey);
					}

					context.player().getMainHandItem().set(DataComponents.EQUIPPABLE, builder.build());
				}
			}
		}));

		LOGGER.info("Model Tools has loaded!");
	}
}