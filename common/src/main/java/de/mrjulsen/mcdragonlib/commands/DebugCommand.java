package de.mrjulsen.mcdragonlib.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.accessor.AbstractDataAccessorPacket;
import de.mrjulsen.mcdragonlib.util.accessor.DataAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;

public class DebugCommand {

    private static final String CMD_NAME = DragonLib.MODID;
    
    private static final String SUB_DEBUG = "debug";
    private static final String SUB_NETWORK = "network";
    private static final String SUB_NETWORK_ACTIVE_CALLBACKS = "active_callbacks";
    private static final String SUB_NETWORK_ACTIVE_TASKS = "active_tasks";
    
    @SuppressWarnings("all")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandSelection selection) {        
        
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(CMD_NAME)
            .then(Commands.literal(SUB_DEBUG)
                .requires(x -> x.hasPermission(4))
                .then(Commands.literal(SUB_NETWORK)
                    .then(Commands.literal(SUB_NETWORK_ACTIVE_CALLBACKS)
                        .executes(x -> networkingActiveCallbacks(x.getSource()))
                    )
                    .then(Commands.literal(SUB_NETWORK_ACTIVE_TASKS)
                        .executes(x -> networkingActiveTasks(x.getSource()))
                    )
                )
            )
        ;

        dispatcher.register(builder);
    }

    private static int networkingActiveCallbacks(CommandSourceStack cmd) throws CommandSyntaxException {
        cmd.sendSuccess(() -> TextUtils.text("The following network callbacks are waiting:\n" + DataAccessor.debug_activeCallbacks()), false);
        return 1;
    }

    private static int networkingActiveTasks(CommandSourceStack cmd) throws CommandSyntaxException {
        cmd.sendSuccess(() -> TextUtils.text("The following network tasks are running:\n" + AbstractDataAccessorPacket.debug_activeTasks()), false);
        return 1;
    }
}