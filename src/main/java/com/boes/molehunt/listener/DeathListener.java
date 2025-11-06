package com.boes.molehunt.listener;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.game.Stop;
import com.boes.molehunt.roles.roles;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public record DeathListener(Molehunt plugin) implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        MoleHuntGame game = plugin.getGame();

        if (game.getGameWorld() == null || !player.getWorld().equals(game.getGameWorld())) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.setGameMode(GameMode.SPECTATOR);

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team hiddenTeam = scoreboard.getTeam("hidden_names");
            if (hiddenTeam != null && hiddenTeam.hasEntry(player.getName())) {
                hiddenTeam.removeEntry(player.getName());
            }
            player.setPlayerListName(player.getName());
            player.setScoreboard(scoreboard);
        });
        game.getGamePlayers().remove(player.getUniqueId());

        checkWinCondition(game);
    }

    private void checkWinCondition(MoleHuntGame game) {
        int moleCount = 0;
        int innocentCount = 0;

        for (roles gp : game.getGamePlayers().values()) {
            if (gp.isMole()) moleCount++;
            else innocentCount++;
        }

        if (moleCount == 0) {
            Bukkit.broadcastMessage("Innocents win! All moles are eliminated! Ending in 60 seconds...");
            endGameCountdown(game);
        } else if (innocentCount == 0) {
            Bukkit.broadcastMessage("Moles win! All innocents are eliminated! Ending in 60 seconds...");
            endGameCountdown(game);
        }
    }

    public void endGameCountdown(MoleHuntGame game) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(game.getGameWorld())) {
                player.setGameMode(GameMode.ADVENTURE);
                player.getInventory().clear();
            }
        }

        new Stop(game).endGame();
    }
}
