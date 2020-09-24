package net.lldv.llamarewards.components.provider;

import cn.nukkit.Player;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.data.Reward;

public class Provider {

    public void connect(LlamaRewards instance) {

    }

    public void disconnect(LlamaRewards instance) {

    }

    public boolean playerExists(Player player) {
        return false;
    }

    public void createPlayer(Player player) {

    }

    public void redeemReward(Player player, Reward reward) {

    }

    public boolean canRedeem(Player player, Reward reward) {
        return false;
    }

    public String getProvider() {
        return null;
    }

}
