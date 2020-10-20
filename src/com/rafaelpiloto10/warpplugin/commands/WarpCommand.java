package com.rafaelpiloto10.warpplugin.commands;

import com.google.common.collect.ObjectArrays;
import com.rafaelpiloto10.warpplugin.Main;
import com.rafaelpiloto10.warpplugin.managers.WarpManager;
import com.rafaelpiloto10.warpplugin.utils.Utils;
import com.rafaelpiloto10.warpplugin.utils.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private final WarpManager warpManager;
    private final Main plugin;

    public WarpCommand(Main plugin) {
        this.plugin = plugin;
        this.plugin.getCommand("warp").setExecutor(this);
        this.plugin.getCommand("warp").setTabCompleter(this);
        warpManager = new WarpManager(this.plugin);
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Utils.chat("&cOnly players can run this command!"));
            return false;
        }
        /**
         *
         * Available Commands:
         *  /warp location_name
         *  /warp ((set/set:world) : (remove/rm) ) location_name
         *  /warp list
         *  /warp help
         *
         */
        Player player = (Player) commandSender;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

        if (!player.hasPermission("warpplugin.use")) return false;

        if (args.length == 1) {

            // /warp list
            if (args[0].equalsIgnoreCase("list")) {
                String[] world_warps = warpManager.getWarps(plugin.getConfig().getString("world_name"));
                String[] player_warps = warpManager.getWarps(offlinePlayer);

                if (world_warps != null && player_warps != null && (world_warps.length > 0 || player_warps.length > 0)) {
                    String[] total_warps = ObjectArrays.concat(world_warps, player_warps, String.class);
                    String listed_warps = "";
                    for (String warp : total_warps) {
                        listed_warps += warp + ", ";
                    }
                    player.sendMessage(Utils.chat("&7Warps: &f" + listed_warps.trim().substring(0, listed_warps.length() - 2)));
                } else {
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("no_warps")));
                }
                return false;
            }

            // /warp help
            if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage(Utils.chat(plugin.getConfig().getString("help")));
                return false;
            }
        }

        // Teleport
        if (args.length == 1) {

            // /warp location_name
            Warp warpLocation = warpManager.getWarp(plugin.getConfig().getString("world_name"), args[0]) != null ?
                    warpManager.getWarp(plugin.getConfig().getString("world_name"), args[0]) : warpManager.getWarp(offlinePlayer, args[0]);

            if (warpLocation == null) {
                player.sendMessage(Utils.chat(plugin.getConfig().getString("warp_not_exist").replace("<warp>", args[0])));
                return false;
            }

            if (player.getWorld().getName().equals(warpLocation.warpToLocation().getWorld().getName()) || plugin.getConfig().getBoolean("allowed_interdimensional_travel")) {
                if (player.getLevel() >= plugin.getConfig().getInt("warp_xp_level_cost")) {
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("warp_success").replace("<warp>", args[0])));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DelayTeleport(player, warpLocation.warpToLocation()), warpLocation.delay);
                    player.setLevel(player.getLevel() - plugin.getConfig().getInt("warp_xp_level_cost"));
                } else {
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("warp_error_xp").replace("<xp_cost>", Integer.toString(plugin.getConfig().getInt("warp_xp_level_cost")))));
                }
                return false;
            } else {
                player.sendMessage(Utils.chat(plugin.getConfig().getString("warp_error_interdimension")));
                return false;
            }

        // Set and remove
        } else if (args.length == 2 || args.length == 3) {

            // Set
            if (args[0].equalsIgnoreCase("set")) {
                // /warp set location_name [delay]

                for (String world_warps : warpManager.getWarps(plugin.getConfig().getString("world_name"))) {
                    if (world_warps.equalsIgnoreCase(args[1])) {
                        player.sendMessage(Utils.chat(plugin.getConfig().getString("warp_exists").replace("<warp>", args[1])));
                        return false;
                    }
                }
                for (String cmd : plugin.getConfig().getString("illegal_names").split(",")) {
                    if (cmd.equalsIgnoreCase(args[1])) {
                        player.sendMessage(Utils.chat(plugin.getConfig().getString("illegal_name").replace("<warp>", args[1])));
                        return false;
                    }
                }
                if (warpManager.getWarps(offlinePlayer).length >= plugin.getConfig().getInt("player_warp_limit")) {
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("warp_limit_error").replace("<amount>", Integer.toString(plugin.getConfig().getInt("player_warp_limit")))));
                    return false;
                }

                if (args.length == 3) {
                    long delay = Long.parseLong(args[2]);
                    warpManager.setWarp(offlinePlayer, args[1], player.getLocation(), delay);
                } else {
                    warpManager.setWarp(offlinePlayer, args[1], player.getLocation());
                }

                player.sendMessage(Utils.chat(plugin.getConfig().getString("set_new_warp").replace("<warp>", args[1])));

            // set:world
            } else if (args[0].equalsIgnoreCase("set:world")) {
                // /warp set:world location_name
                if (player.hasPermission("warpplugin.set_world")) {
                    warpManager.setWarp(plugin.getConfig().getString("world_name"), args[1], player.getLocation(), 0);
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("set_new_warp").replace("<warp>", args[1])));
                    Bukkit.broadcastMessage(Utils.chat(plugin.getConfig().getString("world_warp_announcement").replace("<warp>", args[1])));
                } else
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("no_perms")));
                } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) {
                if (warpManager.removeWarp(offlinePlayer, args[1])) {
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("remove_success").replace("<warp>", args[1])));
                    return false;
                } else if (player.hasPermission("warpplugin.set_world") && warpManager.removeWarp(plugin.getConfig().getString("world_name"), args[1])) {
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("remove_success").replace("<warp>", args[1])));
                    return false;
                } else {
                    player.sendMessage(Utils.chat(plugin.getConfig().getString("remove_error").replace("<warp>", args[1])));
                    return false;
                }
            } else {
                player.sendMessage(Utils.chat(plugin.getConfig().getString("no_parse")));
            }
        } else {
            // Wrong command syntax
            player.sendMessage(Utils.chat(plugin.getConfig().getString("no_parse")));
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(p.getUniqueId());
        if (strings.length == 1) {
            String[] world_warps = warpManager.getWarps(plugin.getConfig().getString("world_name"));
            String[] player_warps = warpManager.getWarps(offlinePlayer);

            if (world_warps != null && player_warps != null && (world_warps.length > 0 || player_warps.length > 0)) {
                String[] total_warps = ObjectArrays.concat(world_warps, player_warps, String.class);
                String[] help_warps = new String[]{"help", "list", "set", "remove"};
                return Arrays.asList(ObjectArrays.concat(total_warps, help_warps, String.class));
            } else {
                return Arrays.asList("help", "list", "set", "remove");
            }
        } else if (strings.length == 2) {
            if (p.hasPermission("warpplugin.set_world")) {
                String[] world_warps = warpManager.getWarps(plugin.getConfig().getString("world_name"));
                String[] player_warps = warpManager.getWarps(offlinePlayer);

                if (world_warps != null && player_warps != null && (world_warps.length > 0 || player_warps.length > 0)) {
                    return Arrays.asList(ObjectArrays.concat(world_warps, player_warps, String.class));
                }
            } else {
                String[] player_warps = warpManager.getWarps(offlinePlayer);
                if (player_warps != null && player_warps.length > 0) {
                    return Arrays.asList(player_warps);
                }
            }
        }
        return null;
    }
}
