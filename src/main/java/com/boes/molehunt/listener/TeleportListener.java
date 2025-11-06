package com.boes.molehunt.listener;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public record TeleportListener(Molehunt plugin) implements Listener {

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        MoleHuntGame game = plugin.getGame();

        if (!game.isRunning() || game.getGameWorld() == null) return;

        if (player.getWorld().equals(game.getGameWorld())) {
            boolean isActivePlayer = game.getGamePlayers().containsKey(player.getUniqueId());
            boolean isSpectator = game.getSpectatorPlayers().contains(player.getUniqueId());

            if ((isActivePlayer || isSpectator) && !event.getTo().getWorld().equals(game.getGameWorld())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot teleport outside the game world while in the game!");
                return;
            }

            if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
                if (!event.getTo().getWorld().equals(game.getGameWorld())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot spectate players outside the game world!");
                }
            }
        }
    }
}