package edu.mathquest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Shows a screen (title, text, buttons) as a pop-up form for Bedrock players,
 * or as chat with clickable buttons for Java players.
 */
final class Ui {

    record Button(String label, String image, Consumer<Player> action) {
        Button(String label, Consumer<Player> action) {
            this(label, null, action);
        }
    }

    record Screen(String title, String body, List<Button> buttons) {}

    private record Pending(long token, List<Button> buttons) {}

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final MathQuest plugin;
    private final boolean floodgate;
    private final Map<UUID, Pending> pending = new HashMap<>();
    private long nextToken = 1;

    Ui(MathQuest plugin, boolean floodgate) {
        this.plugin = plugin;
        this.floodgate = floodgate;
    }

    void show(Player p, Screen s) {
        UUID id = p.getUniqueId();
        long token = nextToken++;
        pending.put(id, new Pending(token, s.buttons()));

        if (floodgate && BedrockForms.isBedrock(id)) {
            BedrockForms.show(id, s, index ->
                    Bukkit.getScheduler().runTask(plugin, () -> click(id, token, index)));
            return;
        }

        p.sendMessage(Component.empty());
        p.sendMessage(LEGACY.deserialize("§6§l" + s.title()));
        p.sendMessage(LEGACY.deserialize(s.body()));
        for (int i = 0; i < s.buttons().size(); i++) {
            String label = s.buttons().get(i).label();
            p.sendMessage(Component.text("  [").color(NamedTextColor.DARK_GRAY)
                    .append(LEGACY.deserialize(label).colorIfAbsent(NamedTextColor.AQUA))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY))
                    .clickEvent(ClickEvent.runCommand("/math click " + token + " " + i))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to choose"))));
        }
    }

    /** Runs the chosen button's action if the click belongs to the screen currently shown. */
    void click(UUID id, long token, int index) {
        Pending p = pending.get(id);
        Player player = Bukkit.getPlayer(id);
        if (p == null || p.token() != token || player == null) return;
        if (index < 0 || index >= p.buttons().size()) return;
        pending.remove(id);
        p.buttons().get(index).action().accept(player);
    }

    void forget(UUID id) {
        pending.remove(id);
    }
}
