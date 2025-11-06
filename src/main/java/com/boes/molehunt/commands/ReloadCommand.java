package com.boes.molehunt.commands;

import com.boes.molehunt.Molehunt;
import org.bukkit.command.CommandSender;

public record ReloadCommand(Molehunt plugin) {

    public void execute(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage("MoleHunt config reloaded.");
    }
}
