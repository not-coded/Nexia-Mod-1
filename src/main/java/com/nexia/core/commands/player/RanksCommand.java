package com.nexia.core.commands.player;

import com.nexia.nexus.api.command.CommandSourceInfo;
import com.nexia.nexus.api.command.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nexia.core.gui.RanksGUI;
import com.nexia.core.utilities.commands.CommandUtil;

public class RanksCommand {

    public static void register(CommandDispatcher<CommandSourceInfo> dispatcher) {
        dispatcher.register(CommandUtils.literal("buy").executes(RanksCommand::run));
        dispatcher.register(CommandUtils.literal("store").executes(RanksCommand::run));
        dispatcher.register(CommandUtils.literal("ranks").executes(RanksCommand::run));
        dispatcher.register(CommandUtils.literal("role").executes(RanksCommand::run));
    }

    public static int run(CommandContext<CommandSourceInfo> context) {
        if(CommandUtil.failIfNoPlayerInCommand(context)) return 0;
        RanksGUI.openMainGUI(CommandUtil.getPlayer(context).unwrap());
        return 1;
    }

}
