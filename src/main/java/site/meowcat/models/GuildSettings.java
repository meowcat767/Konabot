package site.meowcat.models;

import java.util.HashMap;
import java.util.Map;

public class GuildSettings {
    private String levelUpChannelId;
    private Map<String, String> commandPermissions = new HashMap<>();

    public GuildSettings() {}

    public String getLevelUpChannelId() {
        return levelUpChannelId;
    }

    public void setLevelUpChannelId(String levelUpChannelId) {
        this.levelUpChannelId = levelUpChannelId;
    }

    public Map<String, String> getCommandPermissions() {
        return commandPermissions;
    }

    public void setCommandPermissions(Map<String, String> commandPermissions) {
        this.commandPermissions = commandPermissions;
    }
}
