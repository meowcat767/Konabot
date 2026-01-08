package site.meowcat.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserData {
    private long xp = 0;
    private int level = 0;
    private long lastMessageTime = 0;

    public UserData() {}

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    @JsonIgnore
    public long getLastMessageTime() { return lastMessageTime; }
    @JsonIgnore
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public void addXp(int amount) {
        this.xp += amount;
    }

    public boolean canGainXp() {
        return System.currentTimeMillis() - lastMessageTime > 60000;
    }

    public int calculateLevel() {
        // Simple formula: level = floor(sqrt(xp / 100))
        // Or more classic: XP needed = 100 * (level ^ 1.5)
        // Let's use: level = (int) (0.1 * sqrt(xp))
        return (int) (0.1 * Math.sqrt(xp));
    }
    
    public long getXpForLevel(int level) {
        return (long) Math.pow(level / 0.1, 2);
    }
}
