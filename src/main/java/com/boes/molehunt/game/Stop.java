package com.boes.molehunt.game;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public record Stop(MoleHuntGame game) {

    public void endGame() {
        if (!game.isRunning()) return;
        game.setRunning(false);

        if (game.getGameWorld() != null) {
            World world = game.getGameWorld();
            for (Player player : world.getPlayers()) {
                game.getGamePlayers().remove(player.getUniqueId());
            }
        }

        if (game.getTimerTask() != null) {
            game.getTimerTask().cancel();
            game.setTimerTask(null);
        }

        if (game.getGraceTask() != null) {
            game.getGraceTask().cancel();
            game.setGraceTask(null);
        }

        if (game.getBossBar() != null) {
            game.getBossBar().removeAll();
            game.setBossBar(null);
        }
        if (game.getGraceBossBar() != null) {
            game.getGraceBossBar().removeAll();
            game.setGraceBossBar(null);
        }

        game.setGracePeriodActive(false);

        Bukkit.broadcastMessage(ChatColor.RED + "Game ended! Returning to original world in 60 seconds...");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (game.getSavedLocations().containsKey(player.getUniqueId())) {
                        player.teleport(game.getSavedLocations().get(player.getUniqueId()));
                    } else {
                        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    }

                    if (game.getSavedInventories().containsKey(player.getUniqueId())) {
                        player.getInventory().setContents(game.getSavedInventories().get(player.getUniqueId()));
                    } else {
                        player.getInventory().clear();
                    }

                    player.setGameMode(GameMode.ADVENTURE);
                    player.setHealth(20.0);
                    player.setFoodLevel(20);
                    player.setSaturation(20);
                    player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                }

                game.getSavedInventories().clear();
                game.getSavedLocations().clear();
                game.getGamePlayers().clear();
                game.getSpectatorPlayers().clear();
                game.getSpectatorInventories().clear();
                game.getSpectatorLocations().clear();
                game.getSpectatorGameModes().clear();
                game.getSpectatorScoreboards().clear();
                game.getSpectatorListNames().clear();

                if (game.getGameWorld() != null) {
                    String name = game.getGameWorld().getName();
                    Bukkit.unloadWorld(game.getGameWorld(), false);
                    deleteWorld(new File(Bukkit.getWorldContainer(), name));
                    game.setGameWorld(null);
                }

                game.prepareGameWorld();
            }
        }.runTaskLater(game.getPlugin(), 20L * 60);
    }

    private void deleteWorld(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteWorld(f);
                    else f.delete();
                }
            }
        }
        path.delete();
    }
}
