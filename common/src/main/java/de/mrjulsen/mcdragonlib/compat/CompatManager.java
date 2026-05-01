package de.mrjulsen.mcdragonlib.compat;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public final class CompatManager {

    private static record Mod(String name, String id, String url) {}

    public static final Mod SODIUM = new Mod("Sodium", "sodium", "https://modrinth.com/mod/sodium");
    public static final Mod SODIUM_BLENDING_REGISTRY = new Mod("Sodium Blending Registry", "sodium-blendingregistry", "https://modrinth.com/mod/sodium-blendingregistry");

    private static boolean sendSodiumWarn = false;


    private CompatManager() {}

    public static void run() {
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((player) -> {
            // SODIUM
            if (sendSodiumWarn && !ModCommonConfig.HIDE_SODIUM_WARNING.get() && Platform.isFabric() &&
                Platform.isModLoaded(SODIUM.id()) && !Platform.isModLoaded(SODIUM_BLENDING_REGISTRY.id())
            ) {
                sendSodiumWarning(player);
            }
        });
    }

    public static void requiresFixForSodium() {
        sendSodiumWarn = true;
    }

	private static void sendSodiumWarning(LocalPlayer player) {
		if (player == null)
			return;

        MutableComponent text = TextUtils.empty()
            .append(TextUtils.text("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(TextUtils.text(DragonLib.MOD_NAME).withStyle(ChatFormatting.GOLD))
            .append(TextUtils.text("] ").withStyle(ChatFormatting.DARK_GRAY))
            .append(TextUtils.translate("compat." + DragonLib.MODID + ".sodium_warn",
                TextUtils.text(SODIUM.name()),
                TextUtils.text(SODIUM_BLENDING_REGISTRY.name()).withStyle(x -> x
                    .withColor(ChatFormatting.BLUE)
                    .withUnderlined(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SODIUM_BLENDING_REGISTRY.url()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextUtils.translate("compat." + DragonLib.MODID + ".click_to_open_modpage")))
                )
            ).withStyle(ChatFormatting.YELLOW))
        ;
		player.displayClientMessage(text, false);
	}
}
