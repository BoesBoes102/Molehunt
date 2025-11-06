package com.boes.molehunt.commands;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

public record LeaveCommand(Molehunt plugin) {

    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return;
        }

        MoleHuntGame game = plugin.getGame();
        UUID playerUuid = player.getUniqueId();

        if (!game.getSpectatorPlayers().contains(playerUuid)) {
            player.sendMessage(ChatColor.RED + "You are not spectating!");
            return;
        }

        game.getSpectatorPlayers().remove(playerUuid);

        if (game.getSpectatorLocations().containsKey(playerUuid)) {
            player.teleport(game.getSpectatorLocations().get(playerUuid));
            game.getSpectatorLocations().remove(playerUuid);
        } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        if (game.getSpectatorInventories().containsKey(playerUuid)) {
            player.getInventory().setContents(game.getSpectatorInventories().get(playerUuid));
            game.getSpectatorInventories().remove(playerUuid);
        } else {
            player.getInventory().clear();
        }

        if (game.getSpectatorGameModes().containsKey(playerUuid)) {
            player.setGameMode(game.getSpectatorGameModes().get(playerUuid));
            game.getSpectatorGameModes().remove(playerUuid);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }

        if (game.getSpectatorScoreboards().containsKey(playerUuid)) {
            Scoreboard scoreboard = game.getSpectatorScoreboards().get(playerUuid);
            if (scoreboard != null) {
                player.setScoreboard(scoreboard);
            }
            game.getSpectatorScoreboards().remove(playerUuid);
        }

        if (game.getSpectatorListNames().containsKey(playerUuid)) {
            player.setPlayerListName(game.getSpectatorListNames().get(playerUuid));
            game.getSpectatorListNames().remove(playerUuid);
        }

        player.sendMessage(ChatColor.YELLOW + "You have left the spectator mode!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " left spectating!");
    }
}
