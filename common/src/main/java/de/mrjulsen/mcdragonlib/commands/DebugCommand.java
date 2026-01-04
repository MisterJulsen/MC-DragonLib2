package de.mrjulsen.mcdragonlib.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;

public class DebugCommand {

    private static final String CMD_NAME = DragonLib.MODID;
    
    private static final String SUB_DEBUG = "debug";
    private static final String SUB_NETWORK = "network";
    
    @SuppressWarnings("all")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandSelection selection) {        
        
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(CMD_NAME)
            .then(Commands.literal(SUB_DEBUG)
                .requires(x -> x.hasPermission(4))
                .then(Commands.literal(SUB_NETWORK)
                )
            )
        ;

        dispatcher.register(builder);
    }
}