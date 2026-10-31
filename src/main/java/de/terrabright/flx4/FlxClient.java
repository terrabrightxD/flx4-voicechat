package de.terrabright.flx4;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import java.util.List;

public final class FlxClient implements ClientModInitializer {
    static List<Capture.Device> devices = List.of();
    static int selected = -1;
    static KeyMapping openMenu;

    @Override public void onInitializeClient() {
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("flx4_voicechat", "controls"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.flx4_voicechat.open", InputConstants.Type.KEYSYM, InputConstants.KEY_F8, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) {
                if (client.player != null && client.gui.screen() == null)
                    client.gui.setScreen(new FlxScreen());
            }
        });
    }
}
