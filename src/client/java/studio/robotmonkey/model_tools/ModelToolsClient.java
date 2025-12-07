package studio.robotmonkey.model_tools;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.robotmonkey.model_tools.packets.Packets;
import studio.robotmonkey.model_tools.screens.ModelViewer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ModelToolsClient implements ClientModInitializer {

	public static final String MOD_ID = "model-tools";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static KeyMapping keyBinding;
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "model_tools_category"));

	public static HashMap<String, ArrayList<String>> LoadedModels = new HashMap<>();

	public static boolean hasServerConnection = false;

	@Override
	public void onInitializeClient() {

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
				@Override
				public void onResourceManagerReload(ResourceManager resourceManager) {
					LoadedModels.clear();

					for(ResourceLocation id : resourceManager.listResources("equipment", path -> path.toString().endsWith(".json")).keySet()) {
						ArrayList<String> models = LoadedModels.getOrDefault(id.getNamespace(), new ArrayList<>());
						models.add(id.getPath());
						LoadedModels.put(id.getNamespace(), models);

					}

					for(ResourceLocation id : resourceManager.listResources("items", path -> path.toString().endsWith(".json")).keySet()) {
						ArrayList<String> models = LoadedModels.getOrDefault(id.getNamespace(), new ArrayList<>());
						models.add(id.getPath());
						LoadedModels.put(id.getNamespace(), models);

					}

				}

				@Override
				public ResourceLocation getFabricId() {
					return ResourceLocation.fromNamespaceAndPath(MOD_ID, "reloader");
				}
			}
		);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Packets.ConnectClient2S payload = new Packets.ConnectClient2S(client.player.getOnPos());
			ClientPlayNetworking.send(payload);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.consumeClick()) {
				if(client.player.getMainHandItem().isEmpty())  {
					client.player.displayClientMessage(Component.literal("Please hold an item in hand to preview."), false);
				}
				else {
					client.setScreen(new ModelViewer());
				}
			}
		});
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.model-tools.open_viewer", // The translation key of the keybinding's name
				InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_J, // The keycode of the key
				CATEGORY // The category of the key - you'll need to add a translation for this!
		));
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.


		ClientPlayNetworking.registerGlobalReceiver(Packets.ConnectServer2C.ID, (payload, context) -> {
			LOGGER.info("Connected to server side component!");
			ModelToolsClient.hasServerConnection = true;
		});

		LOGGER.info("Model Tools has loaded client side!");
	}
}