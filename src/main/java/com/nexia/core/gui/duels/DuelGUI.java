package com.nexia.core.gui.duels;

import com.natamus.collective_fabric.functions.HeadFunctions;
import com.nexia.minigames.Main;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.gamemodes.GamemodeHandler;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class DuelGUI extends SimpleGui {
    static final TextComponent title = new TextComponent("Duel Menu");

    static ServerPlayer other = null;

    static String kit = "";
    public DuelGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 36; i++){
            this.setSlot(i, itemStack);
        }
    }

    private void setMapLayout(){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);
        int slot = 10;
        int airSlots = 10;
        for(int air = 0; air < 14; air++){
            if(airSlots == 17) {
                airSlots = 19;
            }
            this.setSlot(airSlots, new ItemStack(Items.AIR));
            airSlots++;
        }
        int i1 = 0;
        for(String map : Main.config.duelsMaps){
            this.setSlot(slot, DuelGameMode.duelsMaps.get(i1).setHoverName(new TextComponent(map.toLowerCase())));
            i1++;
            slot++;
        }
    }

    private void setMainLayout(ServerPlayer otherp){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        other = otherp;

        fillEmptySlots(emptySlot);
        int slot = 10;
        int airSlots = 10;
        for(int air = 0; air < 14; air++){
            if(airSlots == 17) {
                airSlots = 19;
            }
            this.setSlot(airSlots, new ItemStack(Items.AIR));
            airSlots++;
        }
        this.setSlot(4, HeadFunctions.getPlayerHead(otherp.getScoreboardName(), 1));
        int i1 = 0;
        for(String duel : DuelGameMode.duels){
            if(slot == 17) {
                slot = 19;
            }

            this.setSlot(slot, DuelGameMode.duelsItems.get(i1).setHoverName(new TextComponent(duel.toUpperCase().replaceAll("_", " "))));
            slot++;
            i1++;
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){
                if(Arrays.stream(DuelGameMode.duels).toList().contains(name.getString().replaceAll(" ", "_"))){
                    kit = name.getString().replaceAll(" ", "_");
                    setMapLayout();
                } else {
                    GamemodeHandler.challengePlayer(this.player, other, kit, name.getString());
                    this.close();
                }

            }
        }
        return super.click(index, clickType, action);
    }
    public static void openDuelGui(ServerPlayer player, ServerPlayer other) {
        DuelGUI shop = new DuelGUI(MenuType.GENERIC_9x4, player, false);
        shop.setTitle(title);
        shop.setMainLayout(other);
        shop.open();
    }
}