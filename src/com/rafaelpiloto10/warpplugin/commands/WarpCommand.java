package com.rafaelpiloto10.warpplugin.commands;

import com.rafaelpiloto10.warpplugin.Main;
import com.rafaelpiloto10.warpplugin.managers.WarpManager;
import com.rafaelpiloto10.warpplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    private WarpManager warpManager;

    public WarpCommand(Main plugin) {
        plugin.getCommand("warp").setExecutor(this);
        warpManager = new WarpManager(plugin);
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Utils.chat("&cOnly players can run this command!"));
        } else {
            /**
             *
             * Available Commands:
             *  /warp location_name
             *  /warp (set : (remove/rm) ) location_name
             *  /warp list
             *  /warp help
             *
             */
            Player player = (Player) commandSender;
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

            if (player.hasPermission("warpplugin.use")) {
                if (strings.length == 1) {
                    if (strings[0].equalsIgnoreCase("list")) {
                        // /warp list
                        String[] world_warps = warpManager.getWarps("world");
                        String[] player_warps = warpManager.getWarps(offlinePlayer);
                        String listed_warps = "";
                        for (String warp : world_warps) {
                            listed_warps += warp + ", ";
                        }
                        for (String warp : player_warps) {
                            listed_warps += warp + ", ";
                        }
                        player.sendMessage(Utils.chat("&7Warps: &f" + listed_warps.trim().substring(0, listed_warps.length() - 1)));

                    } else if (strings[0].equalsIgnoreCase("help")) {
                        // /warp help
                        String help = Utils.chat("&6Warp Command Help\n\n");
                        String help_warp = Utils.chat("/warp <name> &e- Warp to a location\n");
                        String help_warp_set = Utils.chat("&6/warp set <name> &e- Set current location as a warp with custom name\n");
                        String help_warp_remove = Utils.chat("&6/warp <remove:rm> <name> &e- Remove the warp location\n");
                        String help_warp_list = Utils.chat("&6/warp list &e- List the allowed warps\n");
                        String help_warp_help = Utils.chat("&6/warp help &e- Get help with the warp commands");

                        player.sendMessage(help + help_warp + help_warp_set + help_warp_remove + help_warp_list + help_warp_help);

                    } else {
                        // /warp location_name
                        Location warpLocation = warpManager.getWarpLocation("world", strings[0]) != null ?
                                warpManager.getWarpLocation("world", strings[0]) : warpManager.getWarpLocation(offlinePlayer, strings[0]);
                        if (warpLocation != null) {
                            if (warpLocation.getWorld() == player.getWorld()) {
                                player.sendMessage(Utils.chat("&aTeleporting to " + strings[0]));
                                player.teleport(warpLocation);
                            } else {
                                player.sendMessage(Utils.chat("&cInterdimensional travel is not allowed!"));
                            }
                        } else {
                            player.sendMessage(Utils.chat("&cThe " + strings[0] + " warp does not exist!"));
                        }
                    }
                } else if (strings.length == 2) {
                    if (strings[0].equalsIgnoreCase("set")) {
                        // /warp set location_name
                        // TODO: Make sure player cannot set warp to already taken commands
                        for (String world_warps : warpManager.getWarps("world")) {
                            if (world_warps.equalsIgnoreCase(strings[1])) {
                                player.sendMessage(Utils.chat("&cWarp name already exists"));
                                return false;
                            }
                            warpManager.setWarp(offlinePlayer, strings[1], player.getLocation());
                            player.sendMessage("&aSuccessfully set " + strings[1] + " warp!");
                        }
                    } else if (strings[0].equalsIgnoreCase("set:world")) {
                        if (player.hasPermission("warpplugin.set_world")) {
                            warpManager.setWarp("world", strings[1], player.getLocation());
                            player.sendMessage(Utils.chat("&aSuccessfully set " + strings[1] + " world warp!"));
                        } else
                            player.sendMessage(Utils.chat("&cYou do not have permissions to run this command!"));

                    } else if (strings[0].equalsIgnoreCase("remove") || strings[0].equalsIgnoreCase("rm")) {
                        if(warpManager.removeWarp(offlinePlayer, strings[1])){
                            player.sendMessage(Utils.chat("&aSuccessfuly removed warp " + strings[1]));
                            return false;
                        } else if(player.hasPermission("warpplugin.set_world") && warpManager.removeWarp("world", strings[0])){
                            player.sendMessage(Utils.chat("&aSuccessfuly removed warp " + strings[1]));
                            return false;
                        } else {
                            player.sendMessage(Utils.chat("&cCould not remove warp " + strings[1]));
                            return false;
                        }
                    }
                } else {
                    // Not correct command syntax
                    player.sendMessage(Utils.chat("&cCould not parse warp - do &6/warp help"));
                }
            }
        }
        return false;
    }
}