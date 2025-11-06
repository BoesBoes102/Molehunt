package com.boes.molehunt;

import com.boes.molehunt.commands.CommandManager;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class Molehunt extends JavaPlugin {

    private MoleHuntGame game;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.game = new MoleHuntGame(this);
        this.game.prepareGameWorld();

        getCommand("molehunt").setExecutor(new CommandManager(this));

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PVPListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new DragonListener(this),this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("hidden_names");
        if (team == null) {
            team = scoreboard.registerNewTeam("hidden_names");
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);


    }

    public MoleHuntGame getGame() {
        return game;
    }
}
