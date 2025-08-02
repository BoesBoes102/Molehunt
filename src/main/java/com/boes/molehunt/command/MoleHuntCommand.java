package com.boes.molehunt.command;

import com.boes.molehunt.MoleHuntPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MoleHuntCommand implements TabExecutor {

    private final MoleHuntPlugin plugin;

    public MoleHuntCommand(MoleHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("molehunt.admin")) {
            sender.sendMessage("No permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("/molehunt <start|stop|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                int moles = plugin.getConfig().getInt("mole-count", 1);
                int gameTime = plugin.getConfig().getInt("game-time", 1200);

                plugin.getGame().startGame(moles);
                plugin.getGame().startTimer(gameTime);

                sender.sendMessage("Game started with " + moles + " mole(s) for " + gameTime + " seconds.");
            }
            case "stop" -> {
                plugin.getGame().endGame();
                sender.sendMessage("Game stopped.");
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage("MoleHunt config reloaded.");
            }
            default -> sender.sendMessage("/molehunt <start|stop|reload>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (!sender.hasPermission("molehunt.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return Stream.of("start", "stop", "reload")
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return new ArrayList<>();
    }
}
