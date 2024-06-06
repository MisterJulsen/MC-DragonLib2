package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.world.phys.Vec2;

public class DLContextMenu extends WidgetContainer {

    private final Supplier<DLContextMenuItem.Builder> openAction;
    private final Supplier<GuiAreaDefinition> area;

    private DLContextMenu parent;
    
    public DLContextMenu(Supplier<GuiAreaDefinition> area, Supplier<DLContextMenuItem.Builder> openAction) {
        super(0, 0, 100, 100);
        this.area = area;
        this.openAction = openAction;
        visible = false;
    }

    public void setParentMenu(DLContextMenu parent) {
        this.parent = parent;
    }

    public DLContextMenu getParent() {
        return parent;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {}

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        setHovered(mouseX, mouseY);
        
        GuiUtils.drawBox(graphics, new GuiAreaDefinition(x, y, width, height), 0xFF000000, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
        GuiUtils.fill(graphics, x + 1, y + height, width, 1, 0xF8202020);
        GuiUtils.fill(graphics, x + width, y + 1, 1, height, 0xF8202020);

        Iterator<Renderable> w = renderables.iterator();
        while (w.hasNext()) {
            if (w.next() instanceof DLContextMenuItem item) {
                item.render(graphics.graphics(), mouseX, mouseY, partialTicks);
                item.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
            }
        }
    }

    public int getAreaX() {
        return getX() + 1;
    }

    public int getAreaWidth() {
        return getWidth() - 2;
    }

    public int getAreaY() {
        return getY() + 3;
    }

    public int getAreaHeight() {
        return getHeight() - 6;
    }

    public void setSize(int areaW, int areaH) {
        setWidth(areaW + 2);
        setHeight(areaH + 6);
    }

    protected void addItem(DLContextMenuItem item) {
        addRenderableWidget(item);
    }

    public boolean open(int mouseX, int mouseY) {
        return open(mouseX, mouseY, mouseX, mouseY);
    }

    public boolean open(int mouseX, int mouseY, int x, int y) {
        return open(mouseX, mouseY, (menu) -> new Vec2(x, y));
    }

    @Override
    public boolean isHovered() {
        return super.isHovered() || children().stream().anyMatch(x -> x instanceof DLContextMenuItem item && item.getContextMenu() != null && item.getContextMenu().isHovered());
    }

    public boolean isOpen() {
        return isVisible();
    }

    @SuppressWarnings("resource")
    public boolean open(int mouseX, int mouseY, Function<DLContextMenu, Vec2> pos) {
        
        if ((area.get() != null && !area.get().isInBounds(mouseX, mouseY)) || Minecraft.getInstance().screen == null) {
            return false;
        }

        openAction.get().applySize(this);
        Vec2 p = pos.apply(this);

        setX(Math.min((int)p.x, Minecraft.getInstance().screen.width - getWidth()));
        setY(Math.min((int)p.y, Minecraft.getInstance().screen.height - getHeight()));

        clearWidgets();
        openAction.get().build(this).forEach(a -> addItem(a));

        setVisible(true);
        return true;
    }

    public void close() {
        clearWidgets();
        setVisible(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean b = super.mouseClicked(mouseX, mouseY, button);

        for (GuiEventListener l : children()) {
            if (l instanceof IDragonLibWidget widget) {
                DLContextMenu menu = widget.getContextMenu();            
                if (menu != null) {
                    if (!b && widget.contextMenuMouseClickHandler((int)mouseX, (int)mouseY, button)) {
                        children().stream().filter(x -> x instanceof IDragonLibWidget w && x != widget && w.getContextMenu() != null).forEach(x -> {
                            DLContextMenu men = ((IDragonLibWidget)x).getContextMenu();
                            men.close();
                        });

                        if (parent != null) {
                            parent.close();
                        }
                        return true;
                    }

                    if (!menu.isHovered()) {
                        menu.close(); 
                                              
                    }
                }
            }
        }
        
        if (b) {
            close();
            if (getParent() != null) {
                getParent().close();
            } 
        }
        return b;
    }
    
    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}