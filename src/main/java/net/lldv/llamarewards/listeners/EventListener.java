package net.lldv.llamarewards.listeners;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import lombok.RequiredArgsConstructor;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.data.Reward;
import net.lldv.llamarewards.components.language.Language;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class EventListener implements Listener {

    private final LlamaRewards instance;

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        CompletableFuture.runAsync(() -> {
            if (!this.instance.provider.playerExists(event.getPlayer())) {
                this.instance.provider.createPlayer(event.getPlayer());
            }
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
                AtomicInteger i = new AtomicInteger();
                Reward.cachedRewards.values().forEach(s -> {
                    if (this.instance.provider.canRedeem(event.getPlayer(), s)) i.set(i.get() + 1);
                });
                if (i.get() != 0) event.getPlayer().sendMessage(Language.get("reward-redeemable", i.get()));
            }, 50);
        });
    }

}
