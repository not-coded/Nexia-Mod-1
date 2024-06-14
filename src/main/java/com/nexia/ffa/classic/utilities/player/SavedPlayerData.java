package com.nexia.ffa.classic.utilities.player;

public class SavedPlayerData {

    public int kills;
    public int killstreak;
    public int bestKillstreak;
    public int deaths;
    public double rating;
    public double elo;
    public SavedPlayerData() {
        this.kills = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;
        this.deaths = 0;

        this.elo = 0;
        this.rating = 1;
    }
}


