package com.vyronyt.privatesmptools;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PrivateSMPTools extends JavaPlugin {
    private final Map<UUID, Long> lastDupe = new HashMap<>();
    private final Map<UUID, Boolean> vanished = new HashMap<>();

    @Override public void onEnable() {
        saveDefaultConfig();
        getLogger().info("PrivateSMPTools enabled. Admin access is controlled by server permissions and config allowlists.");
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private void msg(CommandSender sender, String key) {
        sender.sendMessage(color(getConfig().getString("messages." + key, "&cUnavailable.")));
    }
    private boolean authorized(Player p, String permission) {
        if (!p.hasPermission(permission)) return false;
        String uuid = p.getUniqueId().toString();
        boolean inUuidList = getConfig().getStringList("authorized-uuids").contains(uuid);
        boolean inNameList = getConfig().getStringList("authorized-names").stream().anyMatch(n -> n.equalsIgnoreCase(p.getName()));
        // Require both a Bukkit permission and an explicit allowlist entry.
        return inUuidList || inNameList;
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dupe")) return dupe(sender, args);
        if (command.getName().equalsIgnoreCase("smpadmin")) return admin(sender, args);
        return false;
    }

    private boolean dupe(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("This command can only be used by a player."); return true; }
        if (!authorized(p, "privatesmptools.dupe")) { msg(p, "no-permission"); return true; }
        long now = System.currentTimeMillis();
        long cooldown = Math.max(0, getConfig().getLong("dupe.cooldown-seconds", 2)) * 1000L;
        long ready = lastDupe.getOrDefault(p.getUniqueId(), 0L) + cooldown;
        if (now < ready) {
            p.sendMessage(color(getConfig().getString("messages.cooldown", "&cCooldown.")
                    .replace("%seconds%", String.valueOf(Math.max(1, (ready - now + 999) / 1000)))));
            return true;
        }
        ItemStack held = p.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR || held.getAmount() < 1) { msg(p, "dupe-empty"); return true; }
        int amount = 1;
        if (args.length > 0) {
            try { amount = Integer.parseInt(args[0]); }
            catch (NumberFormatException ex) { p.sendMessage(color("&cUsage: /dupe [amount]")); return true; }
        }
        int max = Math.max(1, getConfig().getInt("dupe.max-amount", 64));
        if (amount < 1 || amount > max) { p.sendMessage(color("&cAmount must be between 1 and " + max + ".")); return true; }
        ItemStack copy = held.clone();
        copy.setAmount(amount);
        // Check storage first, so a full inventory doesn't silently lose the duplicate.
        if (p.getInventory().firstEmpty() == -1 && !canMerge(p, copy)) { msg(p, "dupe-full"); return true; }
        Map<Integer, ItemStack> leftovers = p.getInventory().addItem(copy);
        if (!leftovers.isEmpty()) {
            // Roll back any partial insertion if Bukkit could not fit the complete copy.
            int inserted = amount - leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
            removeMatching(p, copy, inserted);
            msg(p, "dupe-full");
            return true;
        }
        lastDupe.put(p.getUniqueId(), now);
        p.sendMessage(color(getConfig().getString("messages.dupe-success", "&aDuplicated item.")
                .replace("%amount%", String.valueOf(amount)).replace("%item%", held.getType().name())));
        return true;
    }

    private boolean canMerge(Player p, ItemStack item) {
        for (ItemStack slot : p.getInventory().getStorageContents()) {
            if (slot != null && slot.isSimilar(item) && slot.getAmount() + item.getAmount() <= slot.getMaxStackSize()) return true;
        }
        return false;
    }
    private void removeMatching(Player p, ItemStack template, int amount) {
        int left = amount;
        ItemStack[] contents = p.getInventory().getStorageContents();
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack slot = contents[i];
            if (slot != null && slot.isSimilar(template)) {
                int take = Math.min(left, slot.getAmount()); slot.setAmount(slot.getAmount() - take); left -= take;
                if (slot.getAmount() <= 0) contents[i] = null;
            }
        }
        p.getInventory().setStorageContents(contents);
    }

    private boolean admin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("This command requires a player; use server console for configuration."); return true; }
        if (!authorized(p, "privatesmptools.admin")) { msg(p, "no-permission"); return true; }
        if (args.length != 1) { p.sendMessage(color("&e/smpadmin <creative|survival|vanish|fly|reload>")); return true; }
        switch (args[0].toLowerCase()) {
            case "creative" -> { p.setGameMode(GameMode.CREATIVE); p.sendMessage(color("&aGame mode set to Creative.")); }
            case "survival" -> { p.setGameMode(GameMode.SURVIVAL); p.sendMessage(color("&aGame mode set to Survival.")); }
            case "fly" -> { boolean enabled = !p.getAllowFlight(); p.setAllowFlight(enabled); if (!enabled) p.setFlying(false); p.sendMessage(color("&aFlight " + (enabled ? "enabled" : "disabled") + ".")); }
            case "vanish" -> {
                boolean hide = !vanished.getOrDefault(p.getUniqueId(), false);
                vanished.put(p.getUniqueId(), hide);
                for (Player other : getServer().getOnlinePlayers()) if (!other.equals(p)) {
                    if (hide) other.hidePlayer(this, p); else other.showPlayer(this, p);
                }
                p.sendMessage(color("&aVanish " + (hide ? "enabled" : "disabled") + "."));
            }
            case "reload" -> { reloadConfig(); p.sendMessage(color("&aConfiguration reloaded.")); }
            default -> p.sendMessage(color("&e/smpadmin <creative|survival|vanish|fly|reload>"));
        }
        return true;
    }
}
