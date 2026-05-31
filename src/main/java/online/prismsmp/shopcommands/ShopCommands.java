package online.prismsmp.shopcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShopCommands extends JavaPlugin implements Listener {

    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Set<UUID> fakeLeftPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Crate shops
        registerZMenuCommand("commonshop", "common_shop");
        registerZMenuCommand("uncommonshop", "uncommon_shop");
        registerZMenuCommand("rareshop", "rare_shop");
        registerZMenuCommand("epicshop", "epic_shop");
        registerZMenuCommand("legendaryshop", "legendary_shop");
        registerZMenuCommand("keyshopnpc", "keyshop_npc");
        registerZMenuCommand("keyshop", "keyshop_npc");

        // NPC GUIs
        registerZMenuCommand("rules", "rules");
        registerZMenuCommand("homenpc", "home_npc");
        registerZMenuCommand("discordnpc", "discord_npc");
        registerZMenuCommand("lbnpc", "leaderboard_npc");
        registerZMenuCommand("commands", "commands_npc");
        registerZMenuCommand("cmds", "commands_npc");
        registerZMenuCommand("mystats", "leaderboard_npc");

        // Spawn teleport via Multiverse
        var spawnCmd = getCommand("spawn");
        if (spawnCmd != null) {
            spawnCmd.setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof Player player) {
                    getServer().dispatchCommand(
                        getServer().getConsoleSender(),
                        "mv tp " + player.getName() + " spawnworld"
                    );
                }
                return true;
            });
        }

        getLogger().info("ShopCommands loaded - all commands registered.");
    }

    @Override
    public void onDisable() {
        // Unvanish everyone on shutdown
        for (UUID uuid : vanishedPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                unvanish(p, false);
            }
        }
        vanishedPlayers.clear();
        fakeLeftPlayers.clear();
    }

    private void registerZMenuCommand(String cmdName, String menuName) {
        var cmd = getCommand(cmdName);
        if (cmd != null) {
            cmd.setExecutor((sender, command, label, args) -> {
                if (sender instanceof Player player) {
                    getServer().dispatchCommand(
                        getServer().getConsoleSender(),
                        "zm open " + menuName + " " + player.getName()
                    );
                }
                return true;
            });
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vanish") || command.getName().equalsIgnoreCase("v")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("prismsmp.vanish")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            // /vanish leave — fake leave then vanish
            if (args.length > 0 && args[0].equalsIgnoreCase("leave")) {
                if (vanishedPlayers.contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "You are already vanished. Use " + ChatColor.WHITE + "/vanish" + ChatColor.YELLOW + " to unvanish.");
                    return true;
                }
                vanish(player);
                fakeLeftPlayers.add(player.getUniqueId());

                // Broadcast fake leave message
                String leaveMsg = ChatColor.YELLOW + player.getName() + " left the game";
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.hasPermission("prismsmp.vanish.see")) {
                        online.sendMessage(leaveMsg);
                    } else {
                        online.sendMessage(ChatColor.GRAY + "[Vanish] " + ChatColor.YELLOW + player.getName() + " fake-left the game.");
                    }
                }
                player.sendMessage(ChatColor.GREEN + "You have vanished. Other players think you left the server.");
                return true;
            }

            // /vanish — toggle vanish
            if (vanishedPlayers.contains(player.getUniqueId())) {
                // Unvanish
                unvanish(player, true);

                // If they fake-left, broadcast a fake join
                if (fakeLeftPlayers.remove(player.getUniqueId())) {
                    String joinMsg = ChatColor.YELLOW + player.getName() + " joined the game";
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.hasPermission("prismsmp.vanish.see")) {
                            online.sendMessage(joinMsg);
                        } else {
                            online.sendMessage(ChatColor.GRAY + "[Vanish] " + ChatColor.YELLOW + player.getName() + " fake-joined the game.");
                        }
                    }
                }

                player.sendMessage(ChatColor.GREEN + "You are now visible.");
            } else {
                // Vanish
                vanish(player);
                player.sendMessage(ChatColor.GREEN + "You are now vanished.");
            }

            return true;
        }

        return false;
    }

    private void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        player.setMetadata("vanished", new FixedMetadataValue(this, true));

        // Hide from all non-staff
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission("prismsmp.vanish.see") && !online.equals(player)) {
                online.hidePlayer(this, player);
            }
        }

        // Give invisibility with no particles
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.INVISIBILITY,
            Integer.MAX_VALUE,
            0,
            false,  // ambient = false
            false,  // particles = false
            false   // icon = false (no icon in HUD)
        ));
    }

    private void unvanish(Player player, boolean broadcast) {
        vanishedPlayers.remove(player.getUniqueId());
        fakeLeftPlayers.remove(player.getUniqueId());
        player.removeMetadata("vanished", this);

        // Show to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(this, player);
        }

        // Remove invisibility
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    // Hide vanished players from newly joining players
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();

        for (UUID vanishedUUID : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(vanishedUUID);
            if (vanished != null && vanished.isOnline()) {
                if (!joiner.hasPermission("prismsmp.vanish.see")) {
                    joiner.hidePlayer(this, vanished);
                }
            }
        }

        // If the joining player was vanished (reconnect), re-vanish them
        // (they'd need to re-vanish manually since state is lost on relog)
    }

    // Clean up on quit
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // If they were vanished with fake-leave, suppress the real quit message
        if (fakeLeftPlayers.contains(uuid)) {
            event.setQuitMessage(null);
        }

        vanishedPlayers.remove(uuid);
        fakeLeftPlayers.remove(uuid);
    }
}
