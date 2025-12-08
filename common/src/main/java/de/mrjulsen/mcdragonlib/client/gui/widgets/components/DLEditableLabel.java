package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

@SupportsEvents({
    DLEditableLabel.TextColorChangedEvent.class,
    DLEditableLabel.TextEditedEvent.class,
    DLEditableLabel.EditModeChangedEvent.class
})
public class DLEditableLabel extends DLGuiComponent {
    
    public record TextColorChangedEvent(DLColor color) implements IEvent {}
    public record TextEditedEvent(String text) implements IEvent {}
    public record EditModeChangedEvent(boolean isEditing) implements IEvent {}


    public final Property<String> text = new Property<>("");
    public final ColorProperty textColor = new ColorProperty(DLColor.UNDEFINED, DLColor.WHITE)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLEditableLabel.TextColorChangedEvent(a), true));
    public final BooleanProperty drawFontShadow = new BooleanProperty(false);
    public final Property<Padding> padding = new Property<Padding>(new Padding(1));

    public final BooleanProperty editable = new BooleanProperty(true);


    protected final DLRichTextEditBox editBox;
    protected boolean isEditing = false;

    public DLEditableLabel(int x, int y, int w, int h) {
        super(x, y, w, h);

        editBox = new DLRichTextEditBox(0, 0, w, h);
        editBox.anchor.set(EAlign.values());
        editBox.visible.set(false);
        addComponent(editBox);

        editBox.addEventListener(DLGuiStandardEvents.FocusChangedEvent.class, (s, e) -> {
            if (isEditing && !e.focus()) {
                this.isEditing = false;
                this.editBox.visible.set(false);
                if (editable.get()) {
                    this.text.set(this.editBox.text.get().getPlainText());
                    invokeEvent(this, new EditModeChangedEvent(false));
                    invokeEvent(this, new TextEditedEvent(this.text.get()));
                }
            }
            return false;
        });

        addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            if (editable.get() && !isEditing) {
                this.isEditing = true;
                this.editBox.visible.set(true);
                this.editBox.text.get().set(text.get());
                this.editBox.contentPadding.set(new Padding(0, 3, 0, 3));
                getWindowManager().focusComponent(this.editBox);
                invokeEvent(this, new EditModeChangedEvent(true));
            }
            return false;
        });
    }

    public boolean isEditing() {
        return isEditing;
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (editable.get() && isSelected()) {
            GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x70FFFFFF));
        }
        GuiUtils.drawString(graphics, graphics.defaultFont(), padding.get().left(), height() / 2 - graphics.defaultFont().lineHeight / 2, TextUtils.truncateWithEllipsis(graphics.defaultFont(), text.get(), width() - padding.get().left() - padding.get().right()), textColor.get(), ETextAlignment.LEFT, drawFontShadow.get());
    }
    
}
