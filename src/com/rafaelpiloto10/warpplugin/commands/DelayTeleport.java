package com.rafaelpiloto10.warpplugin.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DelayTeleport implements Runnable {

    private final Player player;
    private final Location location;

    public DelayTeleport(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

    @Override
    public void run() {
        player.teleport(location);
    }
}