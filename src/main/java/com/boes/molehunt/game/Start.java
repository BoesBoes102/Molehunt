package com.boes.molehunt.game;

import com.boes.molehunt.roles.roles;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public record Start(MoleHuntGame game) {

    public void startGame(int moleCount) {
        if (game.isRunning()) return;

        World gameWorld = game.consumePreparedWorld();
        if (gameWorld == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "Failed to create game world!");
            return;
        }
        game.setRunning(true);
        game.setGameWorld(gameWorld);

        for (Player player : Bukkit.getOnlinePlayers()) {
            game.getSavedInventories().put(player.getUniqueId(), player.getInventory().getContents());
            game.getSavedLocations().put(player.getUniqueId(), player.getLocation());
        }

        gameWorld.setTime(1000);
        WorldBorder border = gameWorld.getWorldBorder();
        border.setCenter(0, 0);
        double startSize = game.getPlugin().getConfig().getDouble("world-border.start-size", 2500);
        double endSize = game.getPlugin().getConfig().getDouble("world-border.end-size", 250);
        int shrinkThresholdMinutes = game.getPlugin().getConfig().getInt("world-border.shrink-time-threshold-minutes", 15);
        int shrinkThreshold = shrinkThresholdMinutes * 60;
        border.setSize(startSize);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team hiddenTeam = scoreboard.getTeam("hidden_names");
        if (hiddenTeam == null) {
            hiddenTeam = scoreboard.registerNewTeam("hidden_names");
            hiddenTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            hiddenTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        Set<UUID> moles = new HashSet<>();
        for (int i = 0; i < moleCount && i < players.size(); i++) {
            moles.add(players.get(i).getUniqueId());
        }

        for (Player player : players) {
            boolean isMole = moles.contains(player.getUniqueId());
            game.getGamePlayers().put(player.getUniqueId(), new roles(isMole));

            player.getInventory().clear();
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
            player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));

            Location spawn = getSpawnLocation(gameWorld);
            player.teleport(spawn);

            if (!hiddenTeam.hasEntry(player.getName())) {
                hiddenTeam.addEntry(player.getName());
            }
            player.setPlayerListName(" ");
            player.setScoreboard(scoreboard);

            if (isMole) {
                player.sendMessage(ChatColor.DARK_RED + "You are the MOLE! Eliminate innocents or prevent the dragon's death!");
            } else {
                player.sendMessage(ChatColor.GREEN + "You are NOT the mole. Eliminate the mole or slay the dragon!");
            }
        }

        startGracePeriod(gameWorld, startSize, endSize, shrinkThreshold);
    }


    private Location getSpawnLocation(World world) {
        int y = world.getHighestBlockYAt(0, 0);
        return new Location(world, 0.5, y + 1.0, 0.5);
    }

    private void startGracePeriod(World world, double startSize, double endSize, int shrinkThreshold) {
        game.setGracePeriodActive(true);
        int gracePeriodMinutes = game.getPlugin().getConfig().getInt("grace-period-minutes", 15);
        int graceSeconds = gracePeriodMinutes * 60;

        BossBar graceBar = Bukkit.createBossBar(ChatColor.YELLOW + "Grace Period", BarColor.YELLOW, BarStyle.SEGMENTED_10);
        game.setGraceBossBar(graceBar);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(world)) {
                graceBar.addPlayer(player);
                player.sendMessage(ChatColor.YELLOW + "Grace period has started. PVP is disabled!");
            }
        }

        BukkitRunnable graceTask = new BukkitRunnable() {
            int timeLeft = graceSeconds;
            @Override
            public void run() {
                if (!game.isRunning()) {
                    cleanupGrace();
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    cleanupGrace();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Grace period has ended! PVP is now enabled.");
                    startWorldBorderShrink(world, startSize, endSize, shrinkThreshold);
                    cancel();
                    return;
                }

                graceBar.setProgress((double) timeLeft / graceSeconds);
                timeLeft--;
            }

            private void cleanupGrace() {
                game.setGracePeriodActive(false);
                if (game.getGraceBossBar() != null) {
                    game.getGraceBossBar().removeAll();
                    game.setGraceBossBar(null);
                }
                game.setGraceTask(null);
            }
        };
        graceTask.runTaskTimer(game.getPlugin(), 0L, 20L);
        game.setGraceTask(graceTask);
    }

    private void startWorldBorderShrink(World world, double startSize, double endSize, int shrinkThreshold) {
        WorldBorder border = world.getWorldBorder();
        int gameTimeMinutes = game.getPlugin().getConfig().getInt("game-time-minutes", 90);
        int totalTime = gameTimeMinutes * 60;
        int shrinkTime = Math.max(0, totalTime - shrinkThreshold);

        if (shrinkTime > 0) {
            border.setSize(endSize, shrinkTime);
        }
    }
}
