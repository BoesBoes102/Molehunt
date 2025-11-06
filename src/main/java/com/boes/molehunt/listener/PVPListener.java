package com.boes.molehunt.listener;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public record PVPListener(Molehunt plugin) implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged) || !(event.getDamager() instanceof Player damager)) return;

        MoleHuntGame game = plugin.getGame();

        if (game.getGameWorld() == null) return;
        if (!damaged.getWorld().equals(game.getGameWorld())) return;
        if (!damager.getWorld().equals(game.getGameWorld())) return;

        if (game.isGracePeriodActive()) {
            event.setCancelled(true);
            damager.sendMessage(ChatColor.RED + "PVP is disabled during the grace period!");
        }
    }
}
