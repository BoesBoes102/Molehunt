package com.boes.molehunt.listener;

import com.boes.molehunt.MoleHuntPlugin;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.player.GamePlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GameListener implements Listener {

    private final MoleHuntPlugin plugin;

    public GameListener(MoleHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        MoleHuntGame game = plugin.getGame();

        if (!game.isRunning()) return;

        Bukkit.getScheduler().runTask(plugin, () -> player.setGameMode(GameMode.SPECTATOR));

        game.onPlayerDeath(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MoleHuntGame game = plugin.getGame();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("hidden_names");
        if (team != null && !team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
        player.setPlayerListName(" ");
        player.setScoreboard(scoreboard);

        Location spawnLoc = getHighestLocation(player.getWorld());
        if (spawnLoc != null) {
            player.teleport(spawnLoc);
        }

        if (game.isRunning()) {
            GamePlayer gp = game.getGamePlayer(player);
            if (gp == null) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("You joined late and were not assigned a role. You're now a spectator.");
            }
        }
    }


    private Location getHighestLocation(World world) {
        int y = world.getMaxHeight();
        while (y > 0) {
            Block block = world.getBlockAt(0, y, 0);
            if (block.getType() != Material.AIR && block.getType().isSolid()) {
                return new Location(world, 0 + 0.5, y + 1.0, 0 + 0.5);
            }
            y--;
        }
        return new Location(world, 0 + 0.5, world.getMaxHeight(), 0 + 0.5);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        MoleHuntGame game = plugin.getGame();
        if (!game.isRunning() || game.isGracePeriodActive()) {
            event.setCancelled(true);
            event.getDamager().sendMessage(ChatColor.RED + "PVP is disabled during the grace period!");
        }
    }

}
