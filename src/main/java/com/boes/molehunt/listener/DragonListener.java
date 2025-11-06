package com.boes.molehunt.listener;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public record DragonListener(Molehunt plugin) implements Listener {

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        MoleHuntGame game = plugin.getGame();
        if (!game.isRunning()) return;
        if (game.getGameWorld() == null || !event.getEntity().getWorld().equals(game.getGameWorld())) return;

        Bukkit.broadcastMessage(ChatColor.GREEN + "Innocents win! The Ender Dragon has been slain! Ending in 60 seconds...");
        new DeathListener(plugin).endGameCountdown(game);
    }
}
