package net.lldv.llamarewards.components.provider;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.data.Reward;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YamlProvider extends Provider {

    private Config data;

    @Override
    public void connect(LlamaRewards instance) {
        instance.saveResource("/data/reward_data.yml");
        this.data = new Config(instance.getDataFolder() + "/data/reward_data.yml", Config.YAML);
        instance.getLogger().info("[Configuration] Ready.");
    }

    @Override
    public boolean playerExists(Player player) {
        return this.data.exists("players." + player.getName());
    }

    @Override
    public void createPlayer(Player player) {
        List<String> list = new ArrayList<>();
        this.data.set("players." + player.getName() + ".rewards", list);
        this.data.save();
        this.data.reload();
    }

    @Override
    public void redeemReward(Player player, Reward reward) {
        long intervalSet = (reward.getInterval() * 3600000L) + System.currentTimeMillis();
        List<String> list = this.data.getStringList("players." + player.getName() + ".rewards");

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String data = iterator.next();
            String[] dataString = data.split("##");
            if (dataString[0].equals(reward.getName())) {
                iterator.remove();
            }
        }
        list.add(reward.getName() + "##" + intervalSet);
        this.data.set("players." + player.getName() + ".rewards", list);
        this.data.save();
        this.data.reload();
        reward.getRewards().forEach(command -> Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), command.replace("%p", player.getName())));
        player.sendMessage(reward.getMessage());
    }

    @Override
    public boolean canRedeem(Player player, Reward reward) {
        List<String> list = this.data.getStringList("players." + player.getName() + ".rewards");
        if (list.size() == 0) return true;

        boolean value = true;
        for (String data : list) {
            String[] dataString = data.split("##");
            if (dataString[0].equals(reward.getName())) {
                value = System.currentTimeMillis() >= Long.parseLong(dataString[1]);
            }
        }
        return value;
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }

}
