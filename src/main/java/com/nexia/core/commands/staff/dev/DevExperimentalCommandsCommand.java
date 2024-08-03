package com.nexia.core.commands.staff.dev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nexia.core.utilities.chat.LegacyChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.uhc.utilities.UhcFfaAreas;
import com.nexia.minigames.games.skywars.SkywarsGame;
import com.nexia.minigames.games.skywars.SkywarsMap;
import io.github.blumbo.inventorymerger.saving.SavableInventory;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.FileWriter;

public class DevExperimentalCommandsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("devexperimentalcmds")
                        .requires(commandSourceStack -> {
                            try {
                                return Permissions.check(commandSourceStack, "nexia.dev.experimentalcmds");
                            } catch (Exception ignored) {
                                return false;
                            }
                        })
                        .then(Commands.argument("argument", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"cffa", "rluhc", "swmap", "saveinventory"}), builder)))
                                .executes(DevExperimentalCommandsCommand::run))
                )
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String argument = StringArgumentType.getString(context, "argument");
        String name = argument;
        if(name.contains("-")) name = name.split("-")[1];

        if(argument.contains("cffa")){
            ServerLevel level = ServerTime.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("ffa", name)).location(), FfaUtil.ffaWorldConfig).asWorld();
            player.teleportTo(level, 0, 80, 0, 0, 0);
        } else if(argument.equalsIgnoreCase("rluhc")) {
            UhcFfaAreas.resetMap(true);
            player.sendMessage(LegacyChatFormat.format("Reloaded UHC Map."), Util.NIL_UUID);
        } else if(argument.contains("swmap")) {
            SkywarsMap map = SkywarsMap.identifyMap(name);
            if(map != null) {
                SkywarsGame.map = map;
                player.sendMessage(LegacyChatFormat.format("Map set to " + map.id), Util.NIL_UUID);
            } else {
                player.sendMessage(LegacyChatFormat.format("Invalid map!"), Util.NIL_UUID);
            }

        } else if(argument.equalsIgnoreCase("saveinventory")) {

            SavableInventory savableInventory = new SavableInventory(player.inventory);
            String stringInventory = savableInventory.toSave();

            try {
                String fileName = player.getScoreboardName() + "_devinventory-" + RandomUtil.randomInt(0, 100);
                String directory = FabricLoader.getInstance().getConfigDir().toString() + "/nexia";
                FileWriter fileWriter = new FileWriter(directory + "/" + fileName + ".json");
                fileWriter.write(stringInventory);
                fileWriter.close();
                player.sendMessage(LegacyChatFormat.format("Saved Inventory in /config/nexia/{}.json", fileName), Util.NIL_UUID);
            } catch (Exception var6) {
                player.sendMessage(LegacyChatFormat.format("Failed to save inventory!"), Util.NIL_UUID);
                player.sendMessage(new TextComponent(var6.getMessage()), Util.NIL_UUID);
            }


        }

        return 1;
    }
}