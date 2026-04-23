package ro.cofi.openwaterdetector;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenWaterDetectorClient implements ClientModInitializer {

    public static final String MOD_ID = "open-water-detector";
    public static final String MOD_NAME = "OpenWaterDetector";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {}", MOD_NAME);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.open-water-detector.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.open-water-detector"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(new MainTickHandler());

        LOGGER.info("{} successfully loaded", MOD_NAME);
    }
}
