package edu.mathquest;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/** Per-player levels and stats, stored in progress.yml. */
final class Progress {

    static final class Data {
        String name = "?";
        final EnumMap<Topic, Integer> level = new EnumMap<>(Topic.class);
        final EnumMap<Topic, Integer> rightInRow = new EnumMap<>(Topic.class);
        final EnumMap<Topic, Integer> wrongInRow = new EnumMap<>(Topic.class);
        final EnumMap<Topic, Integer> correct = new EnumMap<>(Topic.class);
        final EnumMap<Topic, Integer> answered = new EnumMap<>(Topic.class);
        int streak, bestStreak, rewardsToday;
        String rewardDay = "";

        int level(Topic t) { return level.getOrDefault(t, 1); }
        int correct(Topic t) { return correct.getOrDefault(t, 0); }
        int answered(Topic t) { return answered.getOrDefault(t, 0); }
    }

    /** What happened after an answer: level change (-1, 0, +1). */
    record Outcome(int levelChange, int newLevel, int streak) {}

    private final MathQuest plugin;
    private final File file;
    private final Map<UUID, Data> players = new LinkedHashMap<>();

    Progress(MathQuest plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "progress.yml");
        load();
    }

    Data get(UUID id, String name) {
        Data d = players.computeIfAbsent(id, k -> new Data());
        if (name != null) d.name = name;
        return d;
    }

    Map<UUID, Data> all() {
        return players;
    }

    Outcome record(UUID id, String name, Topic t, boolean right) {
        Data d = get(id, name);
        int upAfter = plugin.getConfig().getInt("leveling.up-after", 5);
        int downAfter = plugin.getConfig().getInt("leveling.down-after", 3);
        d.answered.merge(t, 1, Integer::sum);
        int change = 0;
        if (right) {
            d.correct.merge(t, 1, Integer::sum);
            d.streak++;
            d.bestStreak = Math.max(d.bestStreak, d.streak);
            d.wrongInRow.put(t, 0);
            int row = d.rightInRow.merge(t, 1, Integer::sum);
            if (row >= upAfter && d.level(t) < t.maxLevel()) {
                d.level.put(t, d.level(t) + 1);
                d.rightInRow.put(t, 0);
                change = 1;
            }
        } else {
            d.streak = 0;
            d.rightInRow.put(t, 0);
            int row = d.wrongInRow.merge(t, 1, Integer::sum);
            if (row >= downAfter && d.level(t) > 1) {
                d.level.put(t, d.level(t) - 1);
                d.wrongInRow.put(t, 0);
                change = -1;
            }
        }
        save();
        return new Outcome(change, d.level(t), d.streak);
    }

    /** Uses up one of today's rewards; false if the daily limit is reached. */
    boolean takeReward(UUID id) {
        Data d = get(id, null);
        String today = LocalDate.now().toString();
        if (!today.equals(d.rewardDay)) {
            d.rewardDay = today;
            d.rewardsToday = 0;
        }
        int limit = plugin.getConfig().getInt("rewards.daily-limit", 40);
        if (limit > 0 && d.rewardsToday >= limit) return false;
        d.rewardsToday++;
        save();
        return true;
    }

    void setLevel(UUID id, Topic t, int level) {
        Data d = get(id, null);
        d.level.put(t, Math.max(1, Math.min(level, t.maxLevel())));
        d.rightInRow.put(t, 0);
        d.wrongInRow.put(t, 0);
        save();
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = y.getConfigurationSection("players");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            Data d = new Data();
            d.name = s.getString("name", "?");
            d.streak = s.getInt("streak");
            d.bestStreak = s.getInt("best-streak");
            d.rewardDay = s.getString("reward-day", "");
            d.rewardsToday = s.getInt("rewards-today");
            for (Topic t : Topic.values()) {
                String p = "topics." + t.name() + ".";
                d.level.put(t, s.getInt(p + "level", 1));
                d.rightInRow.put(t, s.getInt(p + "right-in-row"));
                d.wrongInRow.put(t, s.getInt(p + "wrong-in-row"));
                d.correct.put(t, s.getInt(p + "correct"));
                d.answered.put(t, s.getInt(p + "answered"));
            }
            try {
                players.put(UUID.fromString(key), d);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (Map.Entry<UUID, Data> e : players.entrySet()) {
            Data d = e.getValue();
            String base = "players." + e.getKey() + ".";
            y.set(base + "name", d.name);
            y.set(base + "streak", d.streak);
            y.set(base + "best-streak", d.bestStreak);
            y.set(base + "reward-day", d.rewardDay);
            y.set(base + "rewards-today", d.rewardsToday);
            for (Topic t : Topic.values()) {
                String p = base + "topics." + t.name() + ".";
                y.set(p + "level", d.level(t));
                y.set(p + "right-in-row", d.rightInRow.getOrDefault(t, 0));
                y.set(p + "wrong-in-row", d.wrongInRow.getOrDefault(t, 0));
                y.set(p + "correct", d.correct(t));
                y.set(p + "answered", d.answered(t));
            }
        }
        try {
            y.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not save progress.yml", ex);
        }
    }
}
