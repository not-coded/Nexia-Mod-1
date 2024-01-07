package com.nexia.world;

import com.combatreforged.factory.api.util.Identifier;
import com.combatreforged.factory.api.world.World;
import com.nexia.core.Main;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.skywars.SkywarsMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldUtil {
    public static World getWorld(@NotNull Level level) {
        return ServerTime.factoryServer.getWorld(WorldUtil.getWorldName(WorldUtil.getWorldName(level)));
    }

    private static String getWorldName(@NotNull Level level) {
        return level.dimension().toString().replaceAll("dimension / ", "").replaceAll("]", "").replaceAll("ResourceKey\\[minecraft:", "");
    }

    private static Identifier getWorldName(String name) {
        String[] splitName = name.split(":");
        return new Identifier(splitName[0], splitName[1]);
    }

    public static void deleteWorld(Identifier identifier) {
        RuntimeWorldHandle worldHandle;
        try {
            worldHandle = ServerTime.fantasy.getOrOpenPersistentWorld(
                    ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(identifier.getNamespace(), identifier.getId())).location(),
                    new RuntimeWorldConfig());
            FileUtils.forceDeleteOnExit(new File("/world/dimensions/" + identifier.getNamespace(), identifier.getId()));
            ServerTime.factoryServer.unloadWorld(identifier.getNamespace() + ":" + identifier.getId(), false);
        } catch (Exception e) {
            Main.logger.error("Error occurred while deleting world: " + identifier.getNamespace() + ":" + identifier.getId());
            if(Main.config.debugMode) e.printStackTrace();
            return;
        }
        worldHandle.delete();
    }

    public static void deleteTempWorlds() {
        if(Main.config.debugMode) Main.logger.info("[DEBUG]: Deleting Temporary Worlds");
        List<String> skywarsDelete = new ArrayList<>();
        List<String> duelsDelete = new ArrayList<>();

        for (ServerLevel level : ServerTime.minecraftServer.getAllLevels()) {
            Identifier split = WorldUtil.getWorldName(WorldUtil.getWorldName(level));
            if (split.getNamespace().toLowerCase().contains("duels")) {
                duelsDelete.add(split.getId());
            }
            if (split.getNamespace().toLowerCase().contains("skywars")) {
                skywarsDelete.add(split.getId());
            }
        }

        for(String deletion : skywarsDelete) {
            SkywarsMap.deleteWorld(deletion);
        }

        for(String deletion : duelsDelete) {
            DuelGameHandler.deleteWorld(deletion);
        }
    }
}