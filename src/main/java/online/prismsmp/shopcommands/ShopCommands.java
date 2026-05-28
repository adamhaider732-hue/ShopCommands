package online.prismsmp.shopcommands;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopCommands extends JavaPlugin {

    @Override
    public void onEnable() {
        // Crate shops
        registerZMenuCommand("commonshop",    "common_shop");
        registerZMenuCommand("uncommonshop",  "uncommon_shop");
        registerZMenuCommand("rareshop",      "rare_shop");
        registerZMenuCommand("epicshop",      "epic_shop");
        registerZMenuCommand("legendaryshop", "legendary_shop");
        registerZMenuCommand("keyshopnpc",    "keyshop_npc");
        registerZMenuCommand("keyshop",       "keyshop_npc");

        // NPC GUIs
        registerZMenuCommand("rules",         "rules");
        registerZMenuCommand("homenpc",       "home_npc");
        registerZMenuCommand("discordnpc",    "discord_npc");
        registerZMenuCommand("lbnpc",         "leaderboard_npc");
        registerZMenuCommand("commands",      "commands_npc");
        registerZMenuCommand("cmds",          "commands_npc");
        registerZMenuCommand("mystats",       "leaderboard_npc");

        getLogger().info("ShopCommands loaded - all commands registered.");
    }

    private void registerZMenuCommand(String commandName, String inventoryName) {
        var cmd = getCommand(commandName);
        if (cmd == null) {
            getLogger().warning("Command not found in plugin.yml: " + commandName);
            return;
        }
        cmd.setExecutor((sender, c, label, args) -> {
            if (sender instanceof Player player) {
                getServer().dispatchCommand(
                    getServer().getConsoleSender(),
                    "zm open " + inventoryName + " " + player.getName()
                );
            }
            return true;
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("ShopCommands disabled.");
    }
}
