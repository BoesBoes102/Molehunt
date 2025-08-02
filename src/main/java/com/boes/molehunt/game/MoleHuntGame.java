package com.boes.molehunt.game;

import com.boes.molehunt.MoleHuntPlugin;
import com.boes.molehunt.player.GamePlayer;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MoleHuntGame {

    private final MoleHuntPlugin plugin;
    private boolean running;
    private final Map<UUID, GamePlayer> gamePlayers;
    private BossBar bossBar;
    private BukkitRunnable timerTask;
    private int timeLeft;
    private boolean gracePeriodActive;
    private int graceTimeLeft;
    private BossBar graceBossBar;

    public boolean isGracePeriodActive() {
        return gracePeriodActive;
    }

    public MoleHuntGame(MoleHuntPlugin plugin) {
        this.plugin = plugin;
        this.running = false;
        this.gamePlayers = new HashMap<>();
    }

    public boolean isRunning() {
        return running;
    }

    public Collection<GamePlayer> getPlayers() {
        return gamePlayers.values();
    }

    public GamePlayer getGamePlayer(Player player) {
        return gamePlayers.get(player.getUniqueId());
    }

    public void startGame(int moleCount) {
        if (running) return;
        running = true;
        gamePlayers.clear();

        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        Collections.shuffle(players);

        World world = plugin.getServer().getWorlds().getFirst();
        if (world != null) {

            world.setTime(1000);


            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            double startSize = plugin.getConfig().getDouble("world-border.start-size", 2500);
            border.setSize(startSize);
        }


        Set<UUID> moles = new HashSet<>();
        for (int i = 0; i < moleCount && i < players.size(); i++) {
            moles.add(players.get(i).getUniqueId());
        }

        for (Player player : players) {
            boolean isMole = moles.contains(player.getUniqueId());
            GamePlayer gp = new GamePlayer(isMole);
            gamePlayers.put(player.getUniqueId(), gp);


            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.COOKED_BEEF, 64));
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE, 0, false, false, false
            ));

            player.teleport(getSpawnLocation(player.getWorld()));
            startGracePeriod();


            if (isMole) {
                player.sendMessage(ChatColor.DARK_RED + "You are the MOLE! Your goal is to eliminate all innocents and prevent the death of the Ender Dragon.");
            } else {
                player.sendMessage(ChatColor.GREEN + "You are NOT THE MOLE. Find and eliminate the mole, or slay the Ender Dragon!");
            }
        }
    }



    public void startTimer(int seconds) {
        timeLeft = seconds;

        if (bossBar != null) {
            bossBar.removeAll();
        }

        bossBar = Bukkit.createBossBar("Mole Hunt Time Left", BarColor.GREEN, BarStyle.SOLID);

        int totalTime = plugin.getConfig().getInt("game-time", 1200);
        int shrinkThreshold = plugin.getConfig().getInt("world-border.shrink-time-threshold", 900);
        double startSize = plugin.getConfig().getDouble("world-border.start-size", 2500.0);
        double endSize = plugin.getConfig().getDouble("world-border.end-size", 250.0);
        int shrinkTime = Math.max(0, totalTime - shrinkThreshold);

        World world = plugin.getServer().getWorlds().getFirst();
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(startSize);


        if (shrinkTime > 0) {
            border.setSize(endSize, shrinkTime);
        }

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!running) {
                    cancel();
                    if (bossBar != null) {
                        bossBar.removeAll();
                        bossBar = null;
                    }
                    return;
                }

                if (timeLeft <= 0) {
                    endGame();
                    cancel();
                    return;
                }

                double progress = (double) timeLeft / (double) totalTime;
                bossBar.setProgress(progress);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    bossBar.addPlayer(player);
                }

                timeLeft--;
            }
        };

        timerTask.runTaskTimer(plugin, 0, 20);
    }

    public void startGracePeriod() {
        gracePeriodActive = true;
        graceTimeLeft = plugin.getConfig().getInt("grace-period", 60);

        if (graceBossBar != null) {
            graceBossBar.removeAll();
        }
        graceBossBar = Bukkit.createBossBar(ChatColor.YELLOW + "Grace Period", BarColor.YELLOW, BarStyle.SEGMENTED_10);

        for (Player player : Bukkit.getOnlinePlayers()) {
            graceBossBar.addPlayer(player);
            player.sendMessage(ChatColor.YELLOW + "Grace period has started. PVP is disabled!");
        }

        BukkitRunnable graceTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!running) {
                    cancel();
                    return;
                }

                if (graceTimeLeft <= 0) {
                    gracePeriodActive = false;
                    graceBossBar.removeAll();
                    graceBossBar = null;
                    cancel();

                    Bukkit.broadcastMessage(ChatColor.GREEN + "Grace period has ended! PVP is now enabled.");
                    return;
                }

                double progress = (double) graceTimeLeft / plugin.getConfig().getInt("grace-period", 60);
                graceBossBar.setProgress(progress);
                graceTimeLeft--;
            }
        };
        graceTask.runTaskTimer(plugin, 0L, 20L);
    }


    public void onPlayerDeath(Player player) {
        GamePlayer gp = gamePlayers.remove(player.getUniqueId());
        if (gp == null) return;

        checkWinCondition();
    }

    private void checkWinCondition() {
        int moleCount = 0;
        int innocentCount = 0;

        for (GamePlayer gp : gamePlayers.values()) {
            if (gp.isMole()) moleCount++;
            else innocentCount++;
        }

        if (moleCount == 0) {
            plugin.getServer().broadcastMessage("Innocents win! All moles are eliminated!");
            endGame();
        } else if (innocentCount == 0) {
            plugin.getServer().broadcastMessage("Moles win! All innocents are eliminated!");
            endGame();
        }
    }

    public void endGame() {
        if (!running) return;

        running = false;

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        World world = plugin.getServer().getWorlds().getFirst();
        if (world == null) return;

        WorldBorder border = world.getWorldBorder();
        border.setSize(20);

        Location spawn = getSpawnLocation(world);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawn);
            player.setGameMode(GameMode.ADVENTURE);
        }

        gamePlayers.clear();
    }

    private Location getSpawnLocation(World world) {
        int x = 0;
        int z = 0;
        int y = world.getMaxHeight();
        while (y > 0) {
            if (world.getBlockAt(x, y, z).getType().isSolid()) break;
            y--;
        }
        return new Location(world, x + 0.5, y + 1.0, z + 0.5);
    }
}
