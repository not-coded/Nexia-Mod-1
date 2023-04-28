package com.nexia.core.utilities.pos;

import com.google.gson.Gson;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtectionMap {

    public byte[][][] map;
    public ProtectionBlock[] blocksByIds;
    public ProtectionBlock notListedBlock;
    public byte notListedBlockId;
    public String outsideMessage;

    public ProtectionMap(ServerPlayer player, BlockPos corner1, BlockPos corner2, String filePath, ProtectionBlock[] listedBlocks, ProtectionBlock notListedBlock, String outSideMessage) {
        this.blocksByIds = listedBlocks;
        this.notListedBlock = notListedBlock;
        this.notListedBlockId = (byte)blocksByIds.length;
        this.outsideMessage = outSideMessage;
        this.map = new byte
                [corner2.getX() - corner1.getX() + 1]
                [corner2.getY() - corner1.getY() + 1]
                [corner2.getZ() - corner1.getZ() + 1];
        this.createMap(player, corner1);
        this.exportMap(player, filePath);
    }

    private ProtectionMap(byte[][][] map, ProtectionBlock[] listedBlocks, ProtectionBlock notListedBlock, String outsideMessage) {
        this.map = map;
        this.blocksByIds = listedBlocks;
        this.notListedBlock = notListedBlock;
        this.notListedBlockId = (byte)listedBlocks.length;
        this.outsideMessage = outsideMessage;
    }

    private void createMap(ServerPlayer player, BlockPos corner1) {
        ServerLevel world = player.getLevel();
        int blockCount = 0;

        for (int x = 0; x < this.map.length; x++) {
            for (int y = 0; y < this.map[0].length; y++) {
                for (int z = 0; z < this.map[0][0].length; z++) {
                    BlockPos blockPos = corner1.offset(x, y, z);
                    byte i = this.getMapBlockId(world.getBlockState(blockPos).getBlock());
                    this.map[x][y][z] = i;
                    if (!getMappingBlock(i).canBuild) blockCount++;
                }
            }
        }

        player.sendMessage(new TextComponent("\2477Map created successfully with " +
                ChatFormat.brandColor2 + blockCount + " \2477protected blocks"), Util.NIL_UUID);
    }

    private void exportMap(ServerPlayer player, String filePath) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this.map);

            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(json);
            fileWriter.close();

            player.sendMessage(new TextComponent("\2477Successfully exported protection map."), Util.NIL_UUID);

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(new TextComponent("\2477Failed to export protection map."), Util.NIL_UUID);
        }
    }

    public static ProtectionMap importMap(String filePath, ProtectionBlock[] listedBlocks, ProtectionBlock notListedBlock,
                                          String outsideMessage) {
        byte[][][] map;
        try {
            String possibleJson = Files.readString(Path.of(filePath));
            Gson gson = new Gson();
            map = gson.fromJson(possibleJson, byte[][][].class);
        } catch (Exception e) {
            System.out.println(Main.MOD_NAME + ": Failed to import protection map from " + filePath);
            return null;
        }
        return new ProtectionMap(map, listedBlocks, notListedBlock, outsideMessage);
    }

    private byte getMapBlockId(Block block) {
        for (byte i = 0; i < this.blocksByIds.length; i++) {
            if (block == this.blocksByIds[i].block) return i;
        }
        return this.notListedBlockId;
    }

    public ProtectionBlock getMappingBlock(byte id) {
        if (this.blocksByIds.length > id) return blocksByIds[id];
        return notListedBlock;
    }

    public boolean canBuiltAt(BlockPos mapCorner1, BlockPos buildPos) {
        return this.canBuiltAt(mapCorner1, buildPos, null, false);
    }

    public boolean canBuiltAt(BlockPos mapCorner1, BlockPos buildPos, ServerPlayer player, boolean sendMessage) {
        sendMessage = sendMessage && player != null;

        BlockPos mapPos = buildPos.subtract(mapCorner1);

        if (mapPos.getX() < 0 || mapPos.getX() >= map.length ||
                mapPos.getY() < 0 || mapPos.getY() >= map[0].length ||
                mapPos.getZ() < 0 || mapPos.getZ() >= map[0][0].length) {
            if (sendMessage) player.sendMessage(ChatFormat.formatFail(outsideMessage), Util.NIL_UUID);
            return false;
        }

        byte id = this.map[mapPos.getX()][mapPos.getY()][mapPos.getZ()];
        ProtectionBlock protectionBlock = this.getMappingBlock(id);

        if (!protectionBlock.canBuild) {
            if (sendMessage) player.sendMessage(ChatFormat.formatFail(protectionBlock.noBuildMessage), Util.NIL_UUID);
            return false;
        }
        return true;
    }

}