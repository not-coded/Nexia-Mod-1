package com.nexia.core.utilities.misc;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.gui.ffa.SpawnGUI;
import com.nexia.ffa.FfaUtil;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.ffa.classic.utilities.FfaClassicUtil;
import com.nexia.minigames.games.oitc.OitcGame;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUtil {

    public static boolean dropItem(Player player, ItemStack itemStack) {
        return !(player.getTags().contains("ffa") || player.getTags().contains("ffa_classic") || player.getTags().contains("ffa_kits") || player.getTags().contains("ffa_pot")) || !LobbyUtil.isLobbyWorld(player.level) || !OitcGame.isOITCPlayer(player);
    }

    public static void onSignClick(CallbackInfoReturnable<InteractionResult> ci, BlockPos signPos, Level level, ServerPlayer p) {
        List<BlockPos> bp = new ArrayList<>(Arrays.asList(
                new BlockPos(0, 81, -6),
                new BlockPos(0, 81, 6),
                new BlockPos(-6, 81, 0),
                new BlockPos(6, 81, 0)
        ));

        p.swing(InteractionHand.MAIN_HAND);
        if(level.equals(FfaAreas.ffaWorld)) {
            if(bp.contains(signPos)) {
                SpawnGUI.openSpawnGUI(p);
            }
            ci.setReturnValue(InteractionResult.PASS);
        }
    }


}
