package com.boes.molehunt.listener;

import com.boes.molehunt.MoleHuntPlugin;
import com.boes.molehunt.game.MoleHuntGame;
import com.boes.molehunt.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class BigFlyingThingListener implements Listener {

    private final MoleHuntPlugin plugin;

    public BigFlyingThingListener(MoleHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        MoleHuntGame game = plugin.getGame();
        if (!game.isRunning()) return;

        boolean moleAlive = game.getPlayers().stream().anyMatch(GamePlayer::isMole);
        if (moleAlive) {
            plugin.getServer().broadcastMessage(ChatColor.GREEN + "Innocents win! The Ender Dragon has been slain!");
            game.endGame();
        }
    }
}
