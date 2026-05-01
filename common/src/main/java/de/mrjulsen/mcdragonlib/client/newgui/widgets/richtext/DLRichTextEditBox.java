package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.DLTranslate;
import de.mrjulsen.mcdragonlib.DLTranslate.Type;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLContextMenu.ItemEntry;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.render.IStateRenderer;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.render.VanillaTextBoxRenderer;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.Property;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public class DLRichTextEditBox extends DLAbstractRichTextInputField {

    public static enum TextBoxState {
        NORMAL,
        SELECTED,
        FOCUSED,
        DISABLED;
    }

    public final Property<IStateRenderer<TextBoxState>> componentRenderer = new Property<>(VanillaTextBoxRenderer.VANILLA_TEXTBOX);

    public DLRichTextEditBox(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public List<ItemEntry> buildContextMenuContents(int x, int y) {        
        List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
        if (!readOnly.get()) {
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate(DLTranslate.key(Type.GUI, "menu.cut")), Sprite.empty(), hasSelection(), () -> {
                cutSelected();
            }, null));
        }
        entries.add(new DLContextMenu.ItemEntry(TextUtils.translate(DLTranslate.key(Type.GUI, "menu.copy")), Sprite.empty(), hasSelection(), () -> {
            copySelected();
        }, null));        
        if (!readOnly.get()) {
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate(DLTranslate.key(Type.GUI, "menu.paste")), Sprite.empty(), true, () -> {
                paste();
            }, null));
        }
        if (!readOnly.get()) {
            entries.add(new DLContextMenu.ItemEntry(TextUtils.translate(DLTranslate.key(Type.GUI, "menu.delete")), Sprite.empty(), hasSelection(), () -> {
                text.get().clear();
            }, null));
        }
        entries.add(DLContextMenu.ItemEntry.SEPARATOR);
        entries.add(new DLContextMenu.ItemEntry(TextUtils.translate(DLTranslate.key(Type.GUI, "menu.select_all")), Sprite.empty(), true, () -> {
            selectAll();
        }, null));
        return entries;
    }

    @Override
    public void renderMainLayer(Graphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        //GuiUtils.setTint(backgroundTint.get().getAsARGB());
        if (!enabled.get()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, TextBoxState.DISABLED);
        } else if (isFocused()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, TextBoxState.FOCUSED);
        } else if (isSelected()) {
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, TextBoxState.SELECTED);
        } else {            
            componentRenderer.get().renderSprite(graphics, 0, 0, width(), height(), this, TextBoxState.NORMAL);
        }
        //GuiUtils.fill(graphics, 0, 0, width(), height(), isFocused() ? DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE : DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
        //GuiUtils.fill(graphics, decoratedPadding.get().left(), decoratedPadding.get().top(), width() - decoratedPadding.get().left() - decoratedPadding.get().right(), height() - decoratedPadding.get().top() - decoratedPadding.get().bottom(), (readOnly.get() ? pReadOnlyBackgroundColor.get() : backgroundColor.get()).getAsARGB());
        super.renderMainLayer(graphics, mouseX, mouseY, renderBounds);
    }
    
}
