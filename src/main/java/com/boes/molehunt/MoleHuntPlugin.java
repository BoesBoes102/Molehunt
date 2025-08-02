package com.boes.molehunt;

import com.boes.molehunt.command.MoleHuntCommand;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.listener.GameListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MoleHuntPlugin extends JavaPlugin {

    private MoleHuntGame game;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        World world = Bukkit.getWorlds().getFirst();
        if (world != null) {
            WorldBorder border = world.getWorldBorder();
            border.setSize(20);
        }

        this.game = new MoleHuntGame(this);

        Objects.requireNonNull(getCommand("molehunt")).setExecutor(new MoleHuntCommand(this));
        Objects.requireNonNull(getCommand("molehunt")).setTabCompleter(new MoleHuntCommand(this));
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
    }

    public MoleHuntGame getGame() {
        return game;
    }
}
