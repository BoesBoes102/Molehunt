package com.boes.molehunt.listener;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.roles.roles;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public record JoinListener(Molehunt plugin) implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MoleHuntGame game = plugin.getGame();

        if (game.getGameWorld() != null && player.getWorld().equals(game.getGameWorld())) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.getTeam("hidden_names");
            if (team == null) {
                team = scoreboard.registerNewTeam("hidden_names");
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
            
            if (game.getGamePlayers().containsKey(player.getUniqueId())) {
                if (!team.hasEntry(player.getName())) {
                    team.addEntry(player.getName());
                }
                player.setPlayerListName(" ");
            }
            
            player.setScoreboard(scoreboard);
        }

        if (game.isRunning() && player.getWorld().equals(game.getGameWorld())) {
            roles gp = game.getGamePlayers().get(player.getUniqueId());
            if (gp == null) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("You joined late and were not assigned a role. You're now a spectator.");
            } else {
                player.teleport(getSpawnLocation(game.getGameWorld()));
            }
        }
    }

    private Location getSpawnLocation(org.bukkit.World world) {
        int y = world.getHighestBlockYAt(0, 0);
        return new Location(world, 0.5, y + 1.0, 0.5);
    }
}
