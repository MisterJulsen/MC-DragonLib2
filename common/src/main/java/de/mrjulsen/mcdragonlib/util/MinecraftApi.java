/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
