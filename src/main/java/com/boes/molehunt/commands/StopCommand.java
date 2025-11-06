package com.boes.molehunt.commands;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.game.Stop;
import org.bukkit.command.CommandSender;

public record StopCommand(Molehunt plugin) {

    public void execute(CommandSender sender, String[] args) {
        MoleHuntGame game = plugin.getGame();
        new Stop(game).endGame();
        sender.sendMessage("Game stopped.");
    }
}
