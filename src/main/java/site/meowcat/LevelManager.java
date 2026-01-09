package site.meowcat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.meowcat.models.UserData;
import site.meowcat.models.GuildSettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LevelManager {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random random = new Random();

    // ========= USER XP =========
    private static final String USER_FILE = "levels.json";
    private static Map<String, Map<String, UserData>> userLevels = new HashMap<>();

    // ========= GUILD SETTINGS =========
    private static final String GUILD_FILE = "guilds.json";
    private static Map<String, GuildSettings> guildSettings = new HashMap<>();

    // ========= INIT =========
    static {
        loadUserData();
        loadGuildData();
    }

    // =========================
    // USER DATA
    // =========================
    public static synchronized void loadUserData() {
        File file = new File(USER_FILE);
        if (file.exists()) {
            try {
                userLevels = mapper.readValue(
                        file,
                        new TypeReference<Map<String, Map<String, UserData>>>() {}
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void saveUserData() {
        try {
            mapper.writeValue(new File(USER_FILE), userLevels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized UserData getUserData(String guildId, String userId) {
        return userLevels
                .computeIfAbsent(guildId, g -> new HashMap<>())
                .computeIfAbsent(userId, u -> new UserData());
    }

    public static synchronized boolean addXp(String guildId, String userId) {
        UserData data = getUserData(guildId, userId);

        if (!data.canGainXp()) return false;

        int xpToAdd = 15 + random.nextInt(11);
        int oldLevel = data.getLevel();

        data.addXp(xpToAdd);
        data.setLastMessageTime(System.currentTimeMillis());

        int newLevel = data.calculateLevel();
        if (newLevel > oldLevel) {
            data.setLevel(newLevel);
            saveUserData();
            return true;
        }

        saveUserData();
        return false;
    }

    public static Map<String, Map<String, UserData>> getAllUserData() {
        return userLevels;
    }

    // =========================
    // GUILD SETTINGS
    // =========================
    public static synchronized void loadGuildData() {
        File file = new File(GUILD_FILE);
        if (file.exists()) {
            try {
                guildSettings = mapper.readValue(
                        file,
                        new TypeReference<Map<String, GuildSettings>>() {}
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void saveGuildData() {
        try {
            mapper.writeValue(new File(GUILD_FILE), guildSettings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized GuildSettings getGuildSettings(String guildId) {
        return guildSettings.computeIfAbsent(guildId, id -> new GuildSettings());
    }
}
