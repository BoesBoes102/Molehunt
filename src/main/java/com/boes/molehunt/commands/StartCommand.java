package com.boes.molehunt.commands;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.game.Start;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public record StartCommand(Molehunt plugin) {

    public void execute(CommandSender sender) {
        MoleHuntGame game = plugin.getGame();
        
        if (game.isRunning()) {
            sender.sendMessage(ChatColor.RED + "A game is already running!");
            return;
        }
        
        int moles = plugin.getConfig().getInt("mole-count", 1);

        new Start(game).startGame(moles);
        sender.sendMessage(ChatColor.GREEN + "Game started with " + moles + " mole(s).");
    }
}
