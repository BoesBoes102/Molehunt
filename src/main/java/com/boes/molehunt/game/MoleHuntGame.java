package com.boes.molehunt.game;

import com.boes.molehunt.Molehunt;
import com.boes.molehunt.roles.roles;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class MoleHuntGame {

    private final Molehunt plugin;
    private boolean running;
    private final Map<UUID, roles> gamePlayers = new HashMap<>();
    private final Map<UUID, org.bukkit.inventory.ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, org.bukkit.Location> savedLocations = new HashMap<>();
    private final Set<UUID> spectatorPlayers = new HashSet<>();
    private final Map<UUID, org.bukkit.inventory.ItemStack[]> spectatorInventories = new HashMap<>();
    private final Map<UUID, org.bukkit.Location> spectatorLocations = new HashMap<>();
    private final Map<UUID, GameMode> spectatorGameModes = new HashMap<>();
    private final Map<UUID, Scoreboard> spectatorScoreboards = new HashMap<>();
    private final Map<UUID, String> spectatorListNames = new HashMap<>();
    private BossBar bossBar;
    private BukkitRunnable timerTask;
    private boolean gracePeriodActive;
    private World gameWorld;
    private World preparedWorld;
    private BukkitRunnable graceTask;
    private BossBar graceBossBar;

    public MoleHuntGame(Molehunt plugin) {
        this.plugin = plugin;
    }

    public Molehunt getPlugin() {
        return plugin;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isGracePeriodActive() {
        return gracePeriodActive;
    }

    public void setGracePeriodActive(boolean gracePeriodActive) {
        this.gracePeriodActive = gracePeriodActive;
    }

    public Map<UUID, roles> getGamePlayers() {
        return gamePlayers;
    }

    public Map<UUID, org.bukkit.inventory.ItemStack[]> getSavedInventories() {
        return savedInventories;
    }

    public Map<UUID, org.bukkit.Location> getSavedLocations() {
        return savedLocations;
    }

    public Set<UUID> getSpectatorPlayers() {
        return spectatorPlayers;
    }

    public Map<UUID, org.bukkit.inventory.ItemStack[]> getSpectatorInventories() {
        return spectatorInventories;
    }

    public Map<UUID, org.bukkit.Location> getSpectatorLocations() {
        return spectatorLocations;
    }

    public Map<UUID, GameMode> getSpectatorGameModes() {
        return spectatorGameModes;
    }

    public Map<UUID, Scoreboard> getSpectatorScoreboards() {
        return spectatorScoreboards;
    }

    public Map<UUID, String> getSpectatorListNames() {
        return spectatorListNames;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public BukkitRunnable getTimerTask() {
        return timerTask;
    }

    public void setTimerTask(BukkitRunnable timerTask) {
        this.timerTask = timerTask;
    }

    public World getGameWorld() {
        return gameWorld;
    }

    public void setGameWorld(World gameWorld) {
        this.gameWorld = gameWorld;
    }

    public BukkitRunnable getGraceTask() {
        return graceTask;
    }

    public void setGraceTask(BukkitRunnable graceTask) {
        this.graceTask = graceTask;
    }

    public BossBar getGraceBossBar() {
        return graceBossBar;
    }

    public void setGraceBossBar(BossBar graceBossBar) {
        this.graceBossBar = graceBossBar;
    }

    public World getPreparedWorld() {
        return preparedWorld;
    }

    public void prepareGameWorld() {
        if (running) return;
        if (gameWorld != null) return;
        if (preparedWorld != null) {
            if (Bukkit.getWorld(preparedWorld.getName()) != null) return;
            preparedWorld = null;
        }
        String worldName = "molehunt_gameworld";
        deleteWorldIfExists(worldName);
        WorldCreator creator = new WorldCreator(worldName).environment(World.Environment.NORMAL).type(WorldType.NORMAL);
        preparedWorld = creator.createWorld();
    }

    public World consumePreparedWorld() {
        if (preparedWorld == null) {
            prepareGameWorld();
        }
        World world = preparedWorld;
        preparedWorld = null;
        return world;
    }

    private void deleteWorldIfExists(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }
    }
}
