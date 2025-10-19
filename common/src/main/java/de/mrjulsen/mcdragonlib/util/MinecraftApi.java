package de.mrjulsen.mcdragonlib.util;

import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.mrjulsen.mcdragonlib.DragonLib;

public final class MinecraftApi {
    private MinecraftApi() {}    

    public static UUID getPlayerUUID(String playername) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playername);
            Scanner scan = new Scanner(url.openStream());
            String str = "";
            while (scan.hasNext())
                str += scan.nextLine();
            scan.close();
            JsonObject player = new Gson().fromJson(str, JsonObject.class);
            return UUID.fromString(player.get("id").getAsString());
        } catch (Exception e) {
            DragonLib.LOGGER.warn("Could not get UUID for player with username " + playername, e);
            return new UUID(0, 0);
        }
    }

    public static String getPlayerName(UUID uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            Scanner scan = new Scanner(url.openStream());
            String str = "";
            while (scan.hasNext())
                str += scan.nextLine();
            scan.close();
            JsonObject player = new Gson().fromJson(str, JsonObject.class);
            String username = player.get("name").getAsString();
            return username;
        } catch (Exception e) {
            DragonLib.LOGGER.warn("Could not get username for player with UUID " + uuid, e);
            return "Unknown User";
        }
    }
}
