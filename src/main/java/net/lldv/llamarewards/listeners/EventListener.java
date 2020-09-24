package net.lldv.llamarewards.listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import net.lldv.llamarewards.LlamaRewards;

import java.util.concurrent.CompletableFuture;

public class EventListener implements Listener {

    private final LlamaRewards instance;

    public EventListener(LlamaRewards instance) {
        this.instance = instance;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        CompletableFuture.runAsync(() -> {
            if (!this.instance.provider.playerExists(event.getPlayer())) {
                this.instance.provider.createPlayer(event.getPlayer());
            }
        });
    }

}
