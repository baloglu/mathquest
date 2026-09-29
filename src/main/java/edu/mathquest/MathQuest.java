package edu.mathquest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class MathQuest extends JavaPlugin implements TabExecutor, Listener {

    private final Random random = new Random();
    private Progress progress;
    private Ui ui;
    private NamespacedKey bookKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bookKey = new NamespacedKey(this, "book");
        progress = new Progress(this);
        boolean floodgate = getServer().getPluginManager().getPlugin("floodgate") != null;
        ui = new Ui(this, floodgate);
        PluginCommand cmd = getCommand("math");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Math Quest ready" + (floodgate ? " (Bedrock forms enabled)" : ""));
    }

    @Override
    public void onDisable() {
        if (progress != null) progress.save();
    }

    // ---------- screens ----------

    void menu(Player p) {
        Progress.Data d = progress.get(p.getUniqueId(), p.getName());
        StringBuilder body = new StringBuilder("Hi §e" + displayName(p) + "§r! Learn something new, or practice to earn rewards.\n\n");
        for (Topic t : Topic.values()) {
            body.append("§6").append(t.display).append("§r: Level ").append(d.level(t)).append("/").append(t.maxLevel())
                    .append(" §7(").append(t.levelName(d.level(t))).append(")§r\n");
        }
        if (d.streak > 0) body.append("\nStreak: §a").append(d.streak).append(" in a row!");
        List<Ui.Button> buttons = new ArrayList<>();
        for (Topic t : Topic.values()) {
            buttons.add(new Ui.Button("Practice: " + t.display, t.icon, pl -> practice(pl, t)));
        }
        buttons.add(new Ui.Button("Mixed Challenge", "textures/items/nether_star", pl -> practice(pl, null)));
        for (Topic t : Topic.values()) {
            buttons.add(new Ui.Button("Learn: " + t.display, "textures/items/book_normal", pl -> lesson(pl, t, 0)));
        }
        buttons.add(new Ui.Button("My Progress", "textures/items/map_filled", this::showProgress));
        ui.show(p, new Ui.Screen("Math Quest", body.toString(), buttons));
    }

    void lesson(Player p, Topic t, int page) {
        List<String[]> pages = Lessons.pages(t);
        String[] pg = pages.get(page);
        List<Ui.Button> buttons = new ArrayList<>();
        if (page < pages.size() - 1) buttons.add(new Ui.Button("Next page »", pl -> lesson(pl, t, page + 1)));
        else buttons.add(new Ui.Button("§2Practice now »", t.icon, pl -> practice(pl, t)));
        if (page > 0) buttons.add(new Ui.Button("« Back", pl -> lesson(pl, t, page - 1)));
        buttons.add(new Ui.Button("Menu", this::menu));
        String title = t.display + " (" + (page + 1) + "/" + pages.size() + ")";
        ui.show(p, new Ui.Screen(title, "§l" + pg[0] + "§r\n\n" + pg[1], buttons));
    }

    /** Asks a question. mode == null means mixed topics. */
    void practice(Player p, Topic mode) {
        Topic t = mode != null ? mode : Topic.values()[random.nextInt(Topic.values().length)];
        Progress.Data d = progress.get(p.getUniqueId(), p.getName());
        int level = d.level(t);
        Question q = Gen.next(t, level, random);

        List<Ui.Button> buttons = new ArrayList<>();
        for (int i = 0; i < q.choices().size(); i++) {
            int choice = i;
            buttons.add(new Ui.Button(q.choices().get(i), pl -> answer(pl, mode, t, q, choice)));
        }
        buttons.add(new Ui.Button("§8Back to menu", this::menu));
        String body = q.prompt() + "\n\n§7Streak: " + d.streak + "   Level " + level + ": " + t.levelName(level);
        String title = (mode == null ? "Mixed: " : "") + t.display;
        ui.show(p, new Ui.Screen(title, body, buttons));
    }

    void answer(Player p, Topic mode, Topic t, Question q, int choice) {
        boolean right = choice == q.correct();
        Progress.Outcome out = progress.record(p.getUniqueId(), p.getName(), t, right);
        StringBuilder body = new StringBuilder();
        if (right) {
            body.append("§a§lCorrect!§r  ").append(q.answer()).append("\n");
            body.append(giveReward(p, out.streak()));
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        } else {
            body.append("§c§lNot quite.§r You picked ").append(q.choices().get(choice))
                    .append(".\nThe answer is §a").append(q.answer()).append("§r.\n");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
        }
        body.append("\n").append(q.explanation());
        if (out.levelChange() > 0) {
            body.append("\n\n§6§lLEVEL UP!§r ").append(t.display).append(" level ").append(out.newLevel())
                    .append(": §e").append(t.levelName(out.newLevel()));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        } else if (out.levelChange() < 0) {
            body.append("\n\n§eLet's practice level ").append(out.newLevel()).append(" a bit more: ")
                    .append(t.levelName(out.newLevel())).append("§r");
        }
        List<Ui.Button> buttons = List.of(
                new Ui.Button(right ? "§2Next question »" : "Try another »", pl -> practice(pl, mode)),
                new Ui.Button("Learn: " + t.display, "textures/items/book_normal", pl -> lesson(pl, t, 0)),
                new Ui.Button("Menu", this::menu));
        ui.show(p, new Ui.Screen(right ? "Correct!" : "Keep going!", body.toString(), buttons));
    }

    private String giveReward(Player p, int streak) {
        if (!progress.takeReward(p.getUniqueId())) {
            return "§7(You've earned all your rewards for today. Great work — keep practicing!)§r\n";
        }
        StringBuilder msg = new StringBuilder();
        int xp = getConfig().getInt("rewards.xp-per-correct", 5);
        if (xp > 0) {
            p.giveExp(xp);
            msg.append("§b+").append(xp).append(" XP§r  ");
        }
        msg.append(give(p, "rewards.correct-item", "rewards.correct-amount"));
        int every = getConfig().getInt("rewards.bonus-every", 5);
        if (every > 0 && streak > 0 && streak % every == 0) {
            msg.append("\n§d§l").append(streak).append(" in a row! Bonus: §r").append(give(p, "rewards.bonus-item", "rewards.bonus-amount"));
        }
        return msg.append("\n").toString();
    }

    private String give(Player p, String itemKey, String amountKey) {
        Material m = Material.matchMaterial(getConfig().getString(itemKey, ""));
        int amount = getConfig().getInt(amountKey, 1);
        if (m == null || amount <= 0 || !m.isItem()) return "";
        p.getInventory().addItem(new ItemStack(m, amount)).values()
                .forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
        return "§a+" + amount + " " + pretty(m) + "§r";
    }

    void showProgress(Player p) {
        Progress.Data d = progress.get(p.getUniqueId(), p.getName());
        ui.show(p, new Ui.Screen("My Progress", progressText(d), List.of(new Ui.Button("Menu", this::menu))));
    }

    private String progressText(Progress.Data d) {
        StringBuilder sb = new StringBuilder();
        for (Topic t : Topic.values()) {
            int c = d.correct(t), a = d.answered(t);
            sb.append("§6").append(t.display).append("§r\n  Level ").append(d.level(t)).append("/").append(t.maxLevel())
                    .append(": ").append(t.levelName(d.level(t)))
                    .append("\n  Correct: ").append(c).append("/").append(a);
            if (a > 0) sb.append(" (").append(Math.round(100.0 * c / a)).append("%)");
            sb.append("\n\n");
        }
        sb.append("Best streak: §a").append(d.bestStreak).append("§r");
        return sb.toString();
    }

    // ---------- commands ----------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (sub) {
            case "report" -> {
                if (!sender.hasPermission("mathquest.admin")) return noPerm(sender);
                if (progress.all().isEmpty()) sender.sendMessage("No one has played Math Quest yet.");
                for (Progress.Data d : progress.all().values()) {
                    if (args.length > 1 && !d.name.equalsIgnoreCase(args[1])) continue;
                    sender.sendMessage("§e§l" + d.name + "§r");
                    for (String line : progressText(d).split("\n")) if (!line.isEmpty()) sender.sendMessage(line);
                }
                return true;
            }
            case "setlevel" -> {
                if (!sender.hasPermission("mathquest.admin")) return noPerm(sender);
                Topic t = args.length == 4 ? Topic.parse(args[2]) : null;
                UUID id = args.length == 4 ? findPlayer(args[1]) : null;
                if (t == null || id == null) {
                    sender.sendMessage("Usage: /math setlevel <player> <fractions|decimals|multiplication> <level>");
                    return true;
                }
                try {
                    progress.setLevel(id, t, Integer.parseInt(args[3]));
                    sender.sendMessage("Set " + args[1] + "'s " + t.display + " level to " + progress.get(id, null).level(t));
                } catch (NumberFormatException e) {
                    sender.sendMessage("Level must be a number.");
                }
                return true;
            }
            case "open", "givebook" -> {
                if (!sender.hasPermission("mathquest.admin")) return noPerm(sender);
                Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : null;
                if (target == null) {
                    sender.sendMessage("Usage: /math " + sub + " <online player>");
                } else if (sub.equals("open")) {
                    menu(target);
                    sender.sendMessage("Opened Math Quest for " + target.getName());
                } else {
                    giveBook(target);
                    sender.sendMessage("Gave a Math Quest book to " + target.getName());
                }
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("mathquest.admin")) return noPerm(sender);
                reloadConfig();
                sender.sendMessage("Math Quest config reloaded.");
                return true;
            }
            default -> {
            }
        }

        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only. Console can use: /math report [player], open <player>, givebook <player>, setlevel, reload");
            return true;
        }
        switch (sub) {
            case "click" -> {
                if (args.length == 3) {
                    try {
                        ui.click(p.getUniqueId(), Long.parseLong(args[1]), Integer.parseInt(args[2]));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            case "learn", "practice" -> {
                Topic t = args.length > 1 ? Topic.parse(args[1]) : null;
                boolean mixed = args.length > 1 && args[1].equalsIgnoreCase("mixed");
                if (sub.equals("learn") && t != null) lesson(p, t, 0);
                else if (sub.equals("practice") && (t != null || mixed)) practice(p, t);
                else menu(p);
            }
            case "progress" -> showProgress(p);
            case "book" -> giveBook(p);
            default -> menu(p);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("learn", "practice", "progress", "book"));
            if (sender.hasPermission("mathquest.admin")) options.addAll(List.of("report", "open", "givebook", "setlevel", "reload"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("learn") || args[0].equalsIgnoreCase("practice"))) {
            options.addAll(List.of("fractions", "decimals", "multiplication"));
            if (args[0].equalsIgnoreCase("practice")) options.add("mixed");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setlevel")) {
            options.addAll(List.of("fractions", "decimals", "multiplication"));
        } else {
            return List.of();
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        options.removeIf(o -> !o.startsWith(prefix));
        return options;
    }

    private boolean noPerm(CommandSender sender) {
        sender.sendMessage("§cYou don't have permission to do that.");
        return true;
    }

    private UUID findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online.getUniqueId();
        for (Map.Entry<UUID, Progress.Data> e : progress.all().entrySet()) {
            if (e.getValue().name.equalsIgnoreCase(name)) return e.getKey();
        }
        return null;
    }

    // ---------- events ----------

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        progress.get(p.getUniqueId(), p.getName());
        if (getConfig().getBoolean("give-book", true)) giveBook(p);
        if (!getConfig().getBoolean("join-message", true)) return;
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (p.isOnline()) p.sendMessage("§6[Math Quest]§r Tap with the §6Math Quest§r book (or type §e/math§r) to play!");
        }, 100L);
    }

    @EventHandler
    public void onUseBook(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND || !isMathBook(e.getItem())) return;
        e.setCancelled(true);
        menu(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        ui.forget(e.getPlayer().getUniqueId());
    }

    // ---------- helpers ----------

    private boolean isMathBook(ItemStack item) {
        return item != null && item.getType() == Material.BOOK && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(bookKey, PersistentDataType.BYTE);
    }

    /** Gives the player a Math Quest book unless they already carry one. */
    private void giveBook(Player p) {
        for (ItemStack item : p.getInventory().getContents()) if (isMathBook(item)) return;
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.displayName(Component.text("Math Quest", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Hold it and tap to play!", NamedTextColor.YELLOW)));
        meta.getPersistentDataContainer().set(bookKey, PersistentDataType.BYTE, (byte) 1);
        book.setItemMeta(meta);
        p.getInventory().addItem(book).values()
                .forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
    }

    private static String displayName(Player p) {
        String n = p.getName();
        return n.startsWith(".") ? n.substring(1) : n; // Floodgate prefixes Bedrock names with "."
    }

    private static String pretty(Material m) {
        String s = m.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
