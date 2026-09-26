package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import net.minecraft.resources.ResourceLocation;

public record ImageRef(ResourceLocation texture, int width, int height) {

    public static final int DEFAULT_SIZE = 16;
    public static final int MAX_SIZE = 512;

    public static ImageRef parse(String target) {
        if (target == null) {
            return null;
        }
        String path = target.trim();
        if (path.contains("://")) {
            return null;
        }
        int width = DEFAULT_SIZE;
        int height = DEFAULT_SIZE;

        int space = path.lastIndexOf(' ');
        if (space > 0) {
            int[] size = parseSize(path.substring(space + 1));
            if (size != null) {
                width = size[0];
                height = size[1];
                path = path.substring(0, space).trim();
            }
        }

        ResourceLocation texture = ResourceLocation.tryParse(path);
        return texture == null ? null : new ImageRef(texture, width, height);
    }

    private static int[] parseSize(String text) {
        int x = text.indexOf('x');
        if (x <= 0 || x == text.length() - 1) {
            return null;
        }
        try {
            int width = Integer.parseInt(text.substring(0, x));
            int height = Integer.parseInt(text.substring(x + 1));
            if (width <= 0 || height <= 0) {
                return null;
            }
            return new int[] { Math.min(width, MAX_SIZE), Math.min(height, MAX_SIZE) };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
