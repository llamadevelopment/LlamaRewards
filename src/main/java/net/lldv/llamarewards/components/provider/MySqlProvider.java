package net.lldv.llamarewards.components.provider;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.data.Reward;

import java.sql.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySqlProvider extends Provider {

    private Connection connection;

    @Override
    public void connect(LlamaRewards instance) {
        CompletableFuture.runAsync(() -> {
            try {
                Config config = instance.getConfig();
                Class.forName("com.mysql.jdbc.Driver");
                this.connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("MySql.Host") + ":" + config.getString("MySql.Port") + "/" + config.getString("MySql.Database") + "?autoReconnect=true&useGmtMillisForDatetimes=true&serverTimezone=GMT", config.getString("MySql.User"), config.getString("MySql.Password"));
                this.update("CREATE TABLE IF NOT EXISTS reward_data(player VARCHAR(50), rewards LONGTEXT, PRIMARY KEY (player));");
                instance.getLogger().info("[MySqlClient] Connection opened.");
            } catch (Exception e) {
                e.printStackTrace();
                instance.getLogger().info("[MySqlClient] Failed to connect to database.");
            }
        });
    }

    public void update(String query) {
        CompletableFuture.runAsync(() -> {
            if (this.connection != null) {
                try {
                    PreparedStatement preparedStatement = this.connection.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void disconnect(LlamaRewards instance) {
        if (this.connection != null) {
            try {
                this.connection.close();
                instance.getLogger().info("[MySqlClient] Connection closed.");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                instance.getLogger().info("[MySqlClient] Failed to close connection.");
            }
        }
    }

    @Override
    public boolean playerExists(Player player) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM reward_data WHERE PLAYER = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString("PLAYER") != null;
            resultSet.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void createPlayer(Player player) {
        this.update("INSERT INTO reward_data (PLAYER, REWARDS) VALUES ('" + player.getName() + "', '');");
    }

    @Override
    public void redeemReward(Player player, Reward reward) {
        long intervalSet = (reward.getInterval() * 3600000) + System.currentTimeMillis();
        String rawList = "";
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM reward_data WHERE PLAYER = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) rawList = resultSet.getString("REWARDS");
            resultSet.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> list = Arrays.asList(rawList.split(";-;"));
        String[] listSet = rawList.split(";-;");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String data = iterator.next();
            String[] dataString = data.split("##");
            if (dataString[0].equals(reward.getName())) {
                iterator.remove();
            }
        }
        StringBuilder set = new StringBuilder();
        for (String s : listSet) {
            set.append(s).append(";-;");
        }
        this.update("UPDATE reward_data SET REWARDS= '" + set + reward.getName() + "##" + intervalSet + "' WHERE PLAYER= '" + player.getName() + "';");
        reward.getRewards().forEach(command -> Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), command.replace("%p", player.getName())));
        player.sendMessage(reward.getMessage());
    }

    @Override
    public boolean canRedeem(Player player, Reward reward) {
        String rawList = "";
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM reward_data WHERE PLAYER = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) rawList = resultSet.getString("REWARDS");
            resultSet.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public String getProvider() {
        return "MySql";
    }

}
