package com.nexia.core.utilities.player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SavedPlayerData {

    private String muteEnd;

    private String muteReason;

    public boolean isReportBanned;

    public SavedPlayerData() {
        setMuteEnd(LocalDateTime.MIN);
        this.muteReason = null;
        this.isReportBanned = false;
    }

    public LocalDateTime getMuteEnd() {
        LocalDateTime localDateTime;
        try {
            localDateTime = LocalDateTime.parse(this.muteEnd, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            localDateTime = LocalDateTime.MIN;
        }
        return localDateTime;
    }

    public void setMuteEnd(LocalDateTime muteEnd) {
        this.muteEnd = muteEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getMuteReason() {
        return muteReason;
    }

    public String setMuteReason(String muteReason) {
        return this.muteReason = muteReason;
    }

    public boolean isReportBanned() {
        return isReportBanned;
    }

    public boolean setReportBanned(boolean reportBanned) {
        return isReportBanned = reportBanned;
    }
}
