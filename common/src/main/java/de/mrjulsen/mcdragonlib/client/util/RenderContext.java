package de.mrjulsen.mcdragonlib.client.util;

@FunctionalInterface
public interface RenderContext {
    void render(Graphics graphics, double mouseX, double mouseY);
}
