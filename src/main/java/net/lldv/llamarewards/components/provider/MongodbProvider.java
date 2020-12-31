package net.lldv.llamarewards.components.provider;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.data.Reward;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongodbProvider extends Provider {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> rewardDataCollection;

    @Override
    public void connect(LlamaRewards instance) {
        CompletableFuture.runAsync(() -> {
            MongoClientURI uri = new MongoClientURI(instance.getConfig().getString("MongoDB.Uri"));
            this.mongoClient = new MongoClient(uri);
            this.mongoDatabase = this.mongoClient.getDatabase(instance.getConfig().getString("MongoDB.Database"));
            this.rewardDataCollection = this.mongoDatabase.getCollection("reward_data");
            Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.OFF);
            instance.getLogger().info("[MongoClient] Connection opened.");
        });
    }

    @Override
    public void disconnect(LlamaRewards instance) {
        this.mongoClient.close();
        instance.getLogger().info("[MongoClient] Connection closed.");
    }

    @Override
    public boolean playerExists(Player player) {
        return this.rewardDataCollection.find(new Document("player", player.getName())).first() != null;
    }

    @Override
    public void createPlayer(Player player) {
        List<String> list = new ArrayList<>();
        Document document = new Document("player", player.getName())
                .append("rewards", list);
        this.rewardDataCollection.insertOne(document);
    }

    @Override
    public void redeemReward(Player player, Reward reward) {
        long intervalSet = (reward.getInterval() * 3600000L) + System.currentTimeMillis();
        Document document = this.rewardDataCollection.find(new Document("player", player.getName())).first();
        assert document != null;
        List<String> list = document.getList("rewards", String.class);

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String data = iterator.next();
            String[] dataString = data.split("##");
            if (dataString[0].equals(reward.getName())) {
                iterator.remove();
            }
        }
        list.add(reward.getName() + "##" + intervalSet);
        Bson newEntry = new Document("$set", new Document("rewards", list));
        this.rewardDataCollection.updateOne(new Document("player", player.getName()), newEntry);
        reward.getRewards().forEach(command -> Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), command.replace("%p", player.getName())));
        player.sendMessage(reward.getMessage());
    }

    @Override
    public boolean canRedeem(Player player, Reward reward) {
        Document document = this.rewardDataCollection.find(new Document("player", player.getName())).first();
        assert document != null;
        List<String> list = document.getList("rewards", String.class);
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
        return "MongoDB";
    }

}
