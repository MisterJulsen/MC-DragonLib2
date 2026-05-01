package de.mrjulsen.mcdragonlib.client.newgui.events;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.RichTextComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.action.InteractiveElement;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.ITextFormatter;
import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.Color;
import net.minecraft.network.chat.Component;

public final class DLGuiCommonEvents {
    private DLGuiCommonEvents() {}

    public record BackgroundColorChangedEvent(Color color) implements IEvent {}
    public record TextColorChangedEvent(Color color) implements IEvent {}
    public record CaptionChangedEvent(Component text) implements IEvent {}

    public record CheckedChangedEvent(boolean checked) implements IEvent {}

    public record WindowCreatedEvent(DLWindowManager windowManager, int screenWidth, int screenHeight) implements IEvent {}
    public record WindowCloseEvent(DLWindowManager windowManager) implements IEvent {}
    public record WindowFocusEvent(DLWindowManager windowManager, boolean focus) implements IEvent {}
    public record FocusedWindowChangedEvent(DLWindowManager windowManager, boolean focus, DLWindow newWindow) implements IEvent {}

    public record TextChangedEvent(RichTextComponent text) implements IEvent {}
    public record TextMultilinedChanged(boolean multiline) implements IEvent {}
    public record TextLineWrapChangedEvent(boolean lineWrap) implements IEvent {}
    public record TextMaxCharactersChangedEvent(int maxCharacters) implements IEvent {}
    public record TextLineSpacingChangedEvent(int lineSpacing) implements IEvent {}
    public record TextFilterRegexChangedEvent(MutableSingle<String> regex) implements IEvent {}
    public record TextTextValidationEvent(String currentText, String futureText, MutableSingle<String> input) implements IEvent {}
    public record TextInteractiveElementClicked(InteractiveElement.ClickAction action) implements IEvent {}
    public record TextReadOnlyChangedEvent(boolean readOnly) implements IEvent {}
    public record TextAcceptKeyPressedEvent() implements IEvent {}

    public record ValueChangedEvent(double value) implements IEvent {}
    public record ValueRangeChangedEvent(double min, double max) implements IEvent {}
    public record TextFormatChanged<T extends DLGuiComponent>(ITextFormatter<T> format) implements IEvent {}
    public record ScrollValueChangedEvent(double value) implements IEvent {}
    public record ScrollMaxValueChangedEvent(int max) implements IEvent {}
}
