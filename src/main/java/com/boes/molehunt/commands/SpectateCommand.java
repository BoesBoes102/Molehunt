package com.boes.molehunt.commands;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

public record SpectateCommand(Molehunt plugin) {

    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return;
        }

        MoleHuntGame game = plugin.getGame();

        if (!game.isRunning() || game.getGameWorld() == null) {
            player.sendMessage(ChatColor.RED + "No game is currently running!");
            return;
        }

        if (game.getGamePlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already playing in the game!");
            return;
        }

        if (game.getSpectatorPlayers().contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already spectating!");
            return;
        }

        if (!player.getWorld().equals(game.getGameWorld())) {
            game.getSpectatorLocations().put(player.getUniqueId(), player.getLocation());
            game.getSpectatorInventories().put(player.getUniqueId(), player.getInventory().getContents());
            game.getSpectatorGameModes().put(player.getUniqueId(), player.getGameMode());

            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard != null) {
                game.getSpectatorScoreboards().put(player.getUniqueId(), scoreboard);
            }
            game.getSpectatorListNames().put(player.getUniqueId(), player.getPlayerListName());
        }

        game.getSpectatorPlayers().add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();

        Location spawnLoc = game.getGameWorld().getSpawnLocation();
        player.teleport(spawnLoc);

        player.sendMessage(ChatColor.YELLOW + "You are now spectating the game!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " is now spectating!");
    }
}
