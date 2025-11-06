package com.boes.molehunt.commands;

import com.boes.molehunt.Molehunt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record CommandManager(Molehunt plugin) implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {

        if (args.length == 0) {
            sender.sendMessage("/molehunt <start|stop|reload|spectate|leave>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (!sender.hasPermission("molehunt.admin")) {
                    sender.sendMessage("No permission.");
                    return true;
                }
                new StartCommand(plugin).execute(sender);
            }
            case "stop" -> {
                if (!sender.hasPermission("molehunt.admin")) {
                    sender.sendMessage("No permission.");
                    return true;
                }
                new StopCommand(plugin).execute(sender, args);
            }
            case "reload" -> {
                if (!sender.hasPermission("molehunt.admin")) {
                    sender.sendMessage("No permission.");
                    return true;
                }
                new ReloadCommand(plugin).execute(sender);
            }
            case "spectate" -> new SpectateCommand(plugin).execute(sender);
            case "leave" -> new LeaveCommand(plugin).execute(sender);
            default -> sender.sendMessage("/molehunt <start|stop|reload|spectate|leave>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> allCommands = List.of("start", "stop", "reload", "spectate", "leave");
            return allCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}
