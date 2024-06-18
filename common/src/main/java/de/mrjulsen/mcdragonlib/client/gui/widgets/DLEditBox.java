package de.mrjulsen.mcdragonlib.client.gui.widgets;

import java.util.function.BiConsumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ITickable;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class DLEditBox extends EditBox implements ITickable, IDragonLibWidget {

    protected BiConsumer<DLEditBox, Boolean> onFocusChanged = null;
	protected Component hint;
    protected final Font font;

    private boolean mouseSelected;

    @SuppressWarnings("resource")
    protected DLContextMenu menu = new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
        DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.cut"), Sprite.empty(), canConsumeInput() && !getHighlighted().isEmpty(), (b) -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (this.canConsumeInput()) {
                this.insertText("");
            }
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.copy"), Sprite.empty(), canConsumeInput() && !getHighlighted().isEmpty(), (b) -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.paste"), Sprite.empty(), canConsumeInput(), (b) -> {
            if (this.canConsumeInput()) {
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }
        }, null));
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.delete"), Sprite.empty(), !getValue().isEmpty(), (b) -> {
            setValue("");
        }, null));
        builder.addSeparator();
        builder.add(new ContextMenuItemData(TextUtils.translate("gui.dragonlib.menu.select_all"), Sprite.empty(), true, (b) -> {
            this.moveCursorToEnd();
            this.setHighlightPos(0);
        }, null));
        return builder;
    });

    public DLEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
        this.font = pFont;
    }

    public DLEditBox withOnFocusChanged(BiConsumer<DLEditBox, Boolean> onFocusChanged) {
        this.onFocusChanged = onFocusChanged;
        return this;
    }

    @Override
    protected void setFocused(boolean pFocused) {
        super.setFocused(pFocused);
        if (onFocusChanged != null) {
            onFocusChanged.accept(this, pFocused);
        }
    }

    @Override
    public void onFocusedChanged(boolean pFocused) {
        super.onFocusedChanged(pFocused);
        if (onFocusChanged != null) {
            onFocusChanged.accept(this, pFocused);
        }
    }

	public DLEditBox withHint(Component hint) {
		this.hint = hint;
        return this;
	}

	@Override
	public void renderButton(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(ms, mouseX, mouseY, partialTicks);
        renderMainLayer(new Graphics(ms), mouseX, mouseY, partialTicks);
	}

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (hint == null || hint.getString().isBlank())
			return;

		if (!getValue().isEmpty())
			return;

        GuiUtils.drawString(graphics, font, x + 5, this.y + (this.height - 8) / 2, hint, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.LEFT, false);        
    }

	@Override
	public boolean mouseClicked(double x, double y, int button) {		
		return super.mouseClicked(x, y, button);
	}

	@SuppressWarnings("resource")
    @Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (Minecraft.getInstance().options.keyInventory.isDown()) {
			return true;
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}
    
    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public DLContextMenu getContextMenu() {
        return menu;
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        this.menu = menu;
    }

    @Override
    public void onFocusChangeEvent(boolean focus) {
        setFocus(focus);        
    }

    @Override
    public boolean isMouseSelected() {
        return mouseSelected;
    }

    @Override
    public void setMouseSelected(boolean selected) {
        this.mouseSelected = selected;
    }  

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }    

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setWidth(int w) {
        this.width = w;
    }

    @Override
    public void setHeight(int h) {
        this.height = h;
    }

    @Override
    public void setVisible(boolean b) {
        this.visible = b;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setActive(boolean b) {
        this.active = b;
    }
}
