package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext.action;

public class CommandClickAction implements ClickAction {
    private final String commandToExecute;

    public CommandClickAction(String command) {
        this.commandToExecute = command;
    }

    @Override
    public void onClick() {
        System.out.println("Executing: " + commandToExecute);
    }
}
