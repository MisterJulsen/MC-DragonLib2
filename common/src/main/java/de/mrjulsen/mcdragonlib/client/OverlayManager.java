package de.mrjulsen.mcdragonlib.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.gui.DLOverlayScreen;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class OverlayManager {

    protected static final Map<Long, DLOverlayScreen> instances = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends DLOverlayScreen> Optional<T> get(long id) {
        return has(id) ? Optional.of((T)instances.get(id)) : Optional.empty();
    }

    /**
     * Adds a new overlay to the client's hud.
     * @param overlay
     * @return the id of the overlay. Keep it to access it later or to remove it.
     */
    public static long add(DLOverlayScreen overlay) {
        instances.put(overlay.getId(), overlay);
        return overlay.getId();
    }

    public static boolean has(long id) {
        return instances.containsKey(id);
    }

    public static int count() {
        return instances.size();
    }

    public static void remove(long id) {
        if (!has(id)) {
            return;
        }        
        instances.remove(id).onClose();
    }

    public static void clear() {
        instances.values().forEach(x -> x.onClose());
        instances.clear();
    }

    public static void tickAll() {
        instances.values().forEach(x -> x.tick());
    }

    public static void renderAll(GuiGraphics graphics, float partialTicks) {
        instances.values().forEach(x -> x.render(new Graphics(graphics, graphics.pose()), partialTicks, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight()));
    }

    public static Collection<DLOverlayScreen> getAllOverlays() {
        return instances.values();
    }
}

