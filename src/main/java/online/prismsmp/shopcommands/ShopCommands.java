package online.prismsmp.shopcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopCommands extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("commonshop").setExecutor(this);
        getCommand("uncommonshop").setExecutor(this);
        getCommand("rareshop").setExecutor(this);
        getCommand("epicshop").setExecutor(this);
        getCommand("legendaryshop").setExecutor(this);
        getCommand("keyshopnpc").setExecutor(this);
        getLogger().info("ShopCommands enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        String inventory = switch (command.getName().toLowerCase()) {
            case "commonshop" -> "common_shop";
            case "uncommonshop" -> "uncommon_shop";
            case "rareshop" -> "rare_shop";
            case "epicshop" -> "epic_shop";
            case "legendaryshop" -> "legendary_shop";
            case "keyshopnpc" -> "keyshop_npc";
            default -> null;
        };

        if (inventory == null) return true;

        getServer().dispatchCommand(
            getServer().getConsoleSender(),
            "zm open " + inventory + " " + player.getName()
        );

        return true;
    }
}
