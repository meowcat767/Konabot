package site.meowcat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.meowcat.models.GuildSettings;
import site.meowcat.models.UserData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LevelManager {
    private static final String DATA_FILE = "levels.json";
    private static final String GUILD_DATA_FILE = "guilds.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Map<String, UserData> userLevels = new HashMap<>();
    private static Map<String, GuildSettings> guildSettings = new HashMap<>();
    private static final Random random = new Random();

    static {
        loadData();
        loadGuildData();
    }

    public static synchronized void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                userLevels = mapper.readValue(file, new TypeReference<Map<String, UserData>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void loadGuildData() {
        File file = new File(GUILD_DATA_FILE);
        if (file.exists()) {
            try {
                guildSettings = mapper.readValue(file, new TypeReference<Map<String, GuildSettings>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void saveData() {
        try {
            mapper.writeValue(new File(DATA_FILE), userLevels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveGuildData() {
        try {
            mapper.writeValue(new File(GUILD_DATA_FILE), guildSettings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized UserData getUserData(String userId) {
        return userLevels.computeIfAbsent(userId, k -> new UserData());
    }

    public static synchronized GuildSettings getGuildSettings(String guildId) {
        return guildSettings.computeIfAbsent(guildId, k -> new GuildSettings());
    }

    public static synchronized boolean addXp(String userId) {
        UserData data = getUserData(userId);
        if (data.canGainXp()) {
            int xpToAdd = 15 + random.nextInt(11); // 15-25 XP
            data.addXp(xpToAdd);
            data.setLastMessageTime(System.currentTimeMillis());
            
            int oldLevel = data.getLevel();
            int newLevel = data.calculateLevel();
            
            if (newLevel > oldLevel) {
                data.setLevel(newLevel);
                saveData();
                return true; // Leveled up
            }
            saveData();
        }
        return false;
    }
    
    public static Map<String, UserData> getAllUserData() {
        return userLevels;
    }
}
