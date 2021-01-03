package net.lldv.llamarewards.components.provider;

import cn.nukkit.Player;
import cn.nukkit.Server;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.data.Reward;
import net.lldv.llamarewards.components.simplesqlclient.MySqlClient;
import net.lldv.llamarewards.components.simplesqlclient.objects.SqlColumn;
import net.lldv.llamarewards.components.simplesqlclient.objects.SqlDocument;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySqlProvider extends Provider {

    private MySqlClient client;

    @Override
    public void connect(LlamaRewards instance) {
        CompletableFuture.runAsync(() -> {
            try {
                this.client = new MySqlClient(
                        instance.getConfig().getString("MySql.Host"),
                        instance.getConfig().getString("MySql.Port"),
                        instance.getConfig().getString("MySql.User"),
                        instance.getConfig().getString("MySql.Password"),
                        instance.getConfig().getString("MySql.Database")
                );

                this.client.createTable("reward_data", "player",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 50)
                                .append("rewards", SqlColumn.Type.LONGTEXT));

                instance.getLogger().info("[MySqlClient] Connection opened.");
            } catch (Exception e) {
                e.printStackTrace();
                instance.getLogger().info("[MySqlClient] Failed to connect to database.");
            }
        });
    }

    @Override
    public void disconnect(LlamaRewards instance) {
        instance.getLogger().info("[MySqlClient] Connection closed.");
    }

    @Override
    public boolean playerExists(Player player) {
        SqlDocument document = this.client.find("reward_data", "player", player.getName()).first();
        return document != null;
    }

    @Override
    public void createPlayer(Player player) {
        CompletableFuture.runAsync(() -> this.client.insert("reward_data", new SqlDocument("player", player.getName()).append("rewards", "")));
    }

    @Override
    public void redeemReward(Player player, Reward reward) {
        long intervalSet = (reward.getInterval() * 3600000L) + System.currentTimeMillis();

        SqlDocument document = this.client.find("reward_data", "player", player.getName()).first();
        String rawList = document.getString("rewards");
        List<String> list = Arrays.asList(rawList.split(";-;"));

        if (reward.equals("") || rawList.isEmpty()) {
            this.client.update("reward_data", "player", player.getName(), new SqlDocument("rewards", reward.getName() + "##" + intervalSet + ";-;"));
            reward.getRewards().forEach(command -> Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), command.replace("%p", player.getName())));
            player.sendMessage(reward.getMessage());
            return;
        }

        String[] s = document.getString("rewards").split(";-;");
        String ignore = "";

        for (String e : s) {
            if (e.startsWith(reward.getName())) {
                ignore = reward.getName() + "##" + e.split("##")[1];
            }
        }

        StringBuilder set = new StringBuilder();
        for (String e : list) {
            if (!e.equals(ignore)) {
                set.append(e).append(";-;");
            }
        }

        this.client.update("reward_data", "player", player.getName(), new SqlDocument("rewards", set.toString() + reward.getName() + "##" + intervalSet + ";-;"));
        reward.getRewards().forEach(command -> Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), command.replace("%p", player.getName())));
        player.sendMessage(reward.getMessage());
    }

    @Override
    public boolean canRedeem(Player player, Reward reward) {
        SqlDocument document = this.client.find("reward_data", "player", player.getName()).first();
        if (document != null) {
            String rawList = document.getString("rewards");
            List<String> list = Arrays.asList(rawList.split(";-;"));
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
        return false;
    }

    @Override
    public String getProvider() {
        return "MySql";
    }

}
