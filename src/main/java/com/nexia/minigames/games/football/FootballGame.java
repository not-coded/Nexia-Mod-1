package com.nexia.minigames.games.football;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.games.util.PlayerGameMode;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.misc.RandomUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.core.utilities.pos.EntityPos;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameHandler;
import com.nexia.minigames.games.football.util.player.PlayerData;
import com.nexia.minigames.games.football.util.player.PlayerDataManager;
import net.fabricmc.loader.impl.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.TickUtil;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Predicate;

public class FootballGame {
    public static ArrayList<AccuratePlayer> players = new ArrayList<>();

    public static ArrayList<AccuratePlayer> spectator = new ArrayList<>();

    public static ServerLevel world = null;

    public static FootballMap map = FootballMap.FIELD;

    public static FootballTeam team1 = new FootballTeam(new ArrayList<>(), FootballGame.map.team1Pos);
    public static FootballTeam team2 = new FootballTeam(new ArrayList<>(), map.team2Pos);


    // Both timers counted in seconds.
    public static int gameTime = 300;

    public static int queueTime = 15;

    public static ArrayList<AccuratePlayer> queue = new ArrayList<>();

    public static boolean isStarted = false;

    public static boolean isEnding = false;

    private static FootballTeam winnerTeam = null;

    private static int endTime = 5;


    public static void leave(ServerPlayer minecraftPlayer) {
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        AccuratePlayer accuratePlayer = AccuratePlayer.create(minecraftPlayer);

        PlayerData data = PlayerDataManager.get(minecraftPlayer);
        FootballGame.spectator.remove(accuratePlayer);
        FootballGame.queue.remove(accuratePlayer);
        FootballGame.players.remove(accuratePlayer);
        data.team = null;

        player.removeTag("in_football_game");

        PlayerUtil.resetHealthStatus(player);
        minecraftPlayer.setGameMode(GameType.ADVENTURE);

        if(FootballGame.team1.players.isEmpty()) FootballGame.endGame(FootballGame.team2);
        if(FootballGame.team2.players.isEmpty()) FootballGame.endGame(FootballGame.team1);

        player.getInventory().clear();
        minecraftPlayer.inventory.setCarried(ItemStack.EMPTY);
        minecraftPlayer.getEnderChestInventory().clearContent();

        if(data.gameMode.equals(FootballGameMode.PLAYING) && winnerTeam != null && !winnerTeam.players.contains(accuratePlayer)) {
            data.savedData.loss++;
        }

        player.removeTag("football");

        data.gameMode = FootballGameMode.LOBBY;
    }

    public static void second() {
        if(FootballGame.isStarted) {
            if(FootballGame.isEnding) {
                int color = 244 * 65536 + 166 * 256 + 71;
                // r * 65536 + g * 256 + b;

                ServerPlayer randomPlayer = winnerTeam.players.get(RandomUtil.randomInt(winnerTeam.players.size())).get();
                if(randomPlayer != null) DuelGameHandler.winnerRockets(randomPlayer, FootballGame.world, color);

                if(FootballGame.endTime <= 0) {
                    for(ServerPlayer player : FootballGame.getViewers()){
                        PlayerUtil.getFactoryPlayer(player).runCommand("/hub", 0, false);
                    }

                    FootballGame.resetAll();
                }

                FootballGame.endTime--;
            } else {
                FootballGame.updateInfo();

                if(FootballGame.gameTime <= 0 && !FootballGame.isEnding){

                    int team1 = FootballGame.team1.goals;
                    int team2 = FootballGame.team2.goals;

                    if(team1 > team2) endGame(FootballGame.team1);
                    else endGame(FootballGame.team2);
                } else if(FootballGame.gameTime > 0 && !FootballGame.isEnding){
                    FootballGame.gameTime--;
                }
            }


        } else {
            if(FootballGame.queue.size() >= 2) {
                for(AccuratePlayer player : FootballGame.queue){
                    Player fPlayer = PlayerUtil.getFactoryPlayer(player.get());

                    if(FootballGame.queueTime <= 5) {
                        Title title = getTitle(FootballGame.queueTime);

                        PlayerUtil.getFactoryPlayer(player.get()).sendTitle(title);
                        PlayerUtil.sendSound(player.get(), new EntityPos(player.get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 1);
                    }

                    fPlayer.sendActionBarMessage(
                            Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                                    .append(Component.text(StringUtil.capitalize(FootballGame.map.id)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                                    .append(Component.text(" (" + FootballGame.queue.size()).color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                                    .append(Component.text(FootballGame.queueTime).color(ChatFormat.brandColor2))
                                    .append(Component.text(" | ").color(ChatFormat.lineColor))
                                    .append(Component.text("Teaming is not allowed!").color(ChatFormat.failColor))
                    );

                    if(FootballGame.queueTime <= 5 || FootballGame.queueTime == 10 || FootballGame.queueTime == 15) {
                        fPlayer.sendMessage(Component.text("The game will start in ").color(ChatFormat.lineColor)
                                .append(Component.text(FootballGame.queueTime).color(ChatFormat.brandColor1))
                                .append(Component.text(" seconds.").color(ChatFormat.lineColor))
                        );
                    }
                }

                FootballGame.queueTime--;
            } else {
                FootballGame.queueTime = 15;
            }
            if(FootballGame.queueTime <= 0) startGame();
        }
    }

    public static void goal(ArmorStand entity, FootballTeam team) {
        if(team.goals >= FootballGame.map.maxGoals) FootballGame.endGame(team);
        team.goals++;
        entity.moveTo(0, 80, 0, 0, 0);

        //PlayerDataManager.get(scorer.get()).savedData.goals++;

        int teamID = 1;
        if(team == FootballGame.team2) teamID = 2;

        for(ServerPlayer player : FootballGame.getViewers()) {
            PlayerUtil.getFactoryPlayer(player).sendTitle(Title.title(Component.text("Team " + teamID).color(ChatFormat.brandColor2), Component.text("has scored a goal!").color(ChatFormat.normalColor)));
        }

        for(AccuratePlayer player : FootballGame.team1.players) {
            FootballGame.team1.spawnPosition.teleportPlayer(FootballGame.world, player.get());
        }

        for(AccuratePlayer player : FootballGame.team2.players) {
            FootballGame.team2.spawnPosition.teleportPlayer(FootballGame.world, player.get());
        }
    }

    @NotNull
    private static Title getTitle(int queueTime) {
        TextColor color = NamedTextColor.GREEN;

        if (queueTime <= 3 && queueTime > 1) {
            color = NamedTextColor.YELLOW;
        } else if (queueTime <= 1) {
            color = NamedTextColor.RED;
        }

        return Title.title(Component.text(queueTime).color(color), Component.text(""), Title.Times.of(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofMillis(0)));
    }

    public static void joinQueue(ServerPlayer player) {
        com.nexia.minigames.games.football.util.player.PlayerData data = PlayerDataManager.get(player);
        data.team = null;
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        accuratePlayer.get().setHealth(accuratePlayer.get().getMaxHealth());
        if(FootballGame.isStarted){
            FootballGame.spectator.add(accuratePlayer);
            PlayerDataManager.get(player).gameMode = FootballGameMode.SPECTATOR;
            accuratePlayer.get().setGameMode(GameType.SPECTATOR);
        } else {
            FootballGame.queue.add(accuratePlayer);
            player.addTag(LobbyUtil.NO_DAMAGE_TAG);
        }

        player.teleportTo(world, 0, 101, 0, 0, 0);
        player.setRespawnPosition(world.dimension(), new BlockPos(0, 100, 0), 0, true, false);
    }

    public static void endGame(FootballTeam winnerTeam) {
        FootballGame.isEnding = true;
        FootballGame.winnerTeam = winnerTeam;

        for(AccuratePlayer accuratePlayer : winnerTeam.players) {
            PlayerUtil.getFactoryPlayer(accuratePlayer.get()).sendTitle(Title.title(Component.text("You won!").color(ChatFormat.greenColor), Component.text("")));
            PlayerDataManager.get(accuratePlayer.get()).savedData.wins++;
        }

        for(ServerPlayer player : FootballGame.getViewers()){
            PlayerUtil.getFactoryPlayer(player).sendTitle(Title.title(Component.text(player.getScoreboardName()).color(ChatFormat.brandColor2), Component.text("has won the game! (" + winnerTeam.goals + " goals)").color(ChatFormat.normalColor)));
        }
    }

    public static void updateInfo() {
        String[] timer = TickUtil.minuteTimeStamp(FootballGame.gameTime * 20);
        for(ServerPlayer player : FootballGame.getViewers()) {
            PlayerUtil.getFactoryPlayer(player).sendActionBarMessage(
                    Component.text("Map » ").color(TextColor.fromHexString("#b3b3b3"))
                            .append(Component.text(StringUtil.capitalize(FootballGame.map.id)).color(ChatFormat.brandColor2).decoration(ChatFormat.bold, true))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Time » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(timer[0] + ":" + timer[1]).color(ChatFormat.brandColor2))
                            .append(Component.text(" | ").color(ChatFormat.lineColor))
                            .append(Component.text("Goals » ").color(TextColor.fromHexString("#b3b3b3")))
                            .append(Component.text(com.nexia.minigames.games.football.util.player.PlayerDataManager.get(player).team.goals + "/" + FootballGame.map.maxGoals).color(ChatFormat.brandColor2))
            );
        }
    }

    private static ArmorStand createArmorStand() {
        ArmorStand armorStand = new ArmorStand(FootballGame.world, 0, 80, 0);
        armorStand.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 5, true, true));

        //armorStand.setSmall(true);
        armorStand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));

        //armorStand.setShowArms(true);
        //armorStand.setRightArmPose(new Rotations(0, 45, 0));
        // show hitler pose for thingie for sneak peek with red cap

        FootballGame.world.addFreshEntity(armorStand);

        return armorStand;
    }

    private static FootballTeam assignPlayer(AccuratePlayer player) {
        int players = FootballGame.players.size();
        int random = RandomUtil.randomInt(1, 2);

        int team1 = FootballGame.team1.players.size();
        int team2 = FootballGame.team2.players.size();

        if(team1 >= players/2) {
            FootballGame.team2.addPlayer(player);
            return FootballGame.team2;
        } else if (team2 >= players/2) {
            FootballGame.team1.addPlayer(player);
            return FootballGame.team1;
        }

        if(random == 1) {
            FootballGame.team1.addPlayer(player);
            return FootballGame.team1;
        } else if (random == 2) {
            FootballGame.team2.addPlayer(player);
            return FootballGame.team2;
        }

        return null;
    }

    public static void startGame() {
        if(FootballGame.queueTime <= 0){
            FootballGame.isStarted = true;
            FootballGame.gameTime = 300;
            FootballGame.players.addAll(FootballGame.queue);

            // leather armor dyed blue/red depending on the team

            ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
            sword.getOrCreateTag().putBoolean("Unbreakable", true);
            sword.enchant(Enchantments.KNOCKBACK, 2);
            sword.hideTooltipPart(ItemStack.TooltipPart.UNBREAKABLE);

            FootballGame.createArmorStand();

            for(AccuratePlayer player : FootballGame.players) {
                ServerPlayer serverPlayer = player.get();
                serverPlayer.inventory.setItem(0, sword);

                PlayerData data = PlayerDataManager.get(serverPlayer);
                data.gameMode = FootballGameMode.PLAYING;

                serverPlayer.addTag("in_football_game");
                serverPlayer.addTag(LobbyUtil.NO_DAMAGE_TAG);

                data.team = FootballGame.assignPlayer(player);
                while(data.team == null) {
                    data.team = FootballGame.assignPlayer(player);
                    // if you're still null then im going to beat the shit out of you
                }
                data.team.spawnPosition.teleportPlayer(FootballGame.world, serverPlayer);

                ItemStack helmet = Items.LEATHER_HELMET.getDefaultInstance();
                helmet.getOrCreateTag().putInt("Unbreakable", 1);

                ItemStack chestplate = Items.LEATHER_CHESTPLATE.getDefaultInstance();
                chestplate.getOrCreateTag().putInt("Unbreakable", 1);

                ItemStack leggings = Items.LEATHER_LEGGINGS.getDefaultInstance();
                leggings.getOrCreateTag().putInt("Unbreakable", 1);

                ItemStack boots = Items.LEATHER_BOOTS.getDefaultInstance();
                boots.getOrCreateTag().putInt("Unbreakable", 1);

                if(data.team.equals(FootballGame.team1)) {
                    // r * 65536 + g * 256 + b
                    int colour = 255 * 65536;

                    DyeableLeatherItem leatherItem = (DyeableLeatherItem) Items.LEATHER_HELMET;

                    leatherItem.setColor(helmet, colour);
                    leatherItem.setColor(chestplate, colour);
                    leatherItem.setColor(leggings, colour);
                    leatherItem.setColor(boots, colour);

                } else if(data.team.equals(FootballGame.team2)) {
                    int colour = 255;

                    DyeableLeatherItem leatherItem = (DyeableLeatherItem) Items.LEATHER_HELMET;

                    leatherItem.setColor(helmet, colour);
                    leatherItem.setColor(chestplate, colour);
                    leatherItem.setColor(leggings, colour);
                    leatherItem.setColor(boots, colour);
                }

                player.get().setItemSlot(EquipmentSlot.HEAD, helmet);
                player.get().setItemSlot(EquipmentSlot.CHEST, chestplate);
                player.get().setItemSlot(EquipmentSlot.LEGS, leggings);
                player.get().setItemSlot(EquipmentSlot.FEET, boots);


                player.get().setGameMode(GameType.SURVIVAL);
                //player.setRespawnPosition(world.dimension(), pos, 0, true, false);
            }

            FootballGame.spectator.clear();
            FootballGame.queue.clear();
        }
    }

    public static boolean isFootballPlayer(net.minecraft.world.entity.player.Player player){
        return com.nexia.core.utilities.player.PlayerDataManager.get(player).gameMode == PlayerGameMode.FOOTBALL || player.getTags().contains("football") || player.getTags().contains("in_football_game");
    }

    public static void resetAll() {
        queue.clear();
        players.clear();
        spectator.clear();

        map = FootballMap.footballMaps.get(RandomUtil.randomInt(FootballMap.footballMaps.size()));
        world = ServerTime.fantasy.getOrOpenPersistentWorld(new ResourceLocation("football", FootballGame.map.id), new RuntimeWorldConfig()).asWorld();

        isStarted = false;
        queueTime = 15;
        gameTime = 300;
        isEnding = false;
        team1 = new FootballTeam(new ArrayList<>(), map.team1Pos);
        team2 = new FootballTeam(new ArrayList<>(), map.team2Pos);
        winnerTeam = null;
        endTime = 5;
    }

    public static void tick() {

        AABB aabb = new AABB(FootballGame.map.corner1, FootballGame.map.corner2);
        Predicate<Entity> predicate = o -> true;

        for (ItemEntity entity : FootballGame.world.getEntities(EntityType.ITEM, aabb, predicate)) {
            // kill @e[type=item,distance=0..]
            entity.remove();
        }

        if(!FootballGame.isStarted) {
            for (ArmorStand entity : FootballGame.world.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
                // kill @e[type=item,distance=0..]
                entity.remove();
            }
        }
        if(!FootballGame.isStarted) return;

        // check if armor stand (football) is in goal, then reset football to middle and give goal

        aabb = new AABB(FootballGame.map.team1goalCorner1, FootballGame.map.team1goalCorner2);
        for (ArmorStand entity : FootballGame.world.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            FootballGame.goal(entity, FootballGame.team2);
        }
        aabb = new AABB(FootballGame.map.team2goalCorner1, FootballGame.map.team2goalCorner2);
        for (ArmorStand entity : FootballGame.world.getEntities(EntityType.ARMOR_STAND, aabb, predicate)) {
            FootballGame.goal(entity, FootballGame.team1);
        }
    }

    public static void firstTick(){
        resetAll();
    }

    public static ArrayList<ServerPlayer> getViewers() {
        ArrayList<ServerPlayer> viewers = new ArrayList<>();
        FootballGame.players.forEach(player -> viewers.add(player.get()));
        FootballGame.spectator.forEach(player -> viewers.add(player.get()));
        return viewers;
    }
}
