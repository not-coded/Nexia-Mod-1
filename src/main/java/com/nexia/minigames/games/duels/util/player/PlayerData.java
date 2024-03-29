package com.nexia.minigames.games.duels.util.player;

import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.map.DuelsMap;
import com.nexia.minigames.games.duels.util.DuelOptions;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files

    // Global
    public DuelGameMode gameMode;
    public boolean inDuel;

    // Kit Editor
    public String editingKit;

    // Kit Layout
    public String editingLayout;

    // Duels
    public DuelOptions duelOptions;
    public DuelOptions.GameOptions gameOptions;
    public DuelOptions.InviteOptions inviteOptions;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.gameMode = DuelGameMode.LOBBY;
        this.inDuel = false;

        this.duelOptions = new DuelOptions(null, null);
        this.inviteOptions = new DuelOptions.InviteOptions(null, false, DuelsMap.CITY, "null", false);
        this.gameOptions = null;

        this.editingKit = "";
        this.editingLayout = "";

        /*
        this.invitingPlayer = null;
        this.isDead = false;
        this.duelsTeam = null;
        this.duelPlayer = null;
        this.teamDuelsGame = null;
        this.spectatingPlayer = null;
        this.inviteMap = DuelsMap.CITY;
        this.inviteKit = "";
        this.duelsGame = null;

         */
    }

}
