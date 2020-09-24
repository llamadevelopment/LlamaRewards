package net.lldv.llamarewards;

import cn.nukkit.plugin.PluginBase;
import lombok.Getter;
import net.lldv.llamarewards.commands.RewardCommand;
import net.lldv.llamarewards.components.api.LlamaRewardsAPI;
import net.lldv.llamarewards.components.data.Reward;
import net.lldv.llamarewards.components.forms.FormListener;
import net.lldv.llamarewards.components.language.Language;
import net.lldv.llamarewards.components.provider.MongodbProvider;
import net.lldv.llamarewards.components.provider.MySqlProvider;
import net.lldv.llamarewards.components.provider.Provider;
import net.lldv.llamarewards.components.provider.YamlProvider;
import net.lldv.llamarewards.listeners.EventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LlamaRewards extends PluginBase {

    private final Map<String, Provider> providers = new HashMap<>();
    public Provider provider;

    @Getter
    private static LlamaRewards instance;

    @Override
    public void onEnable() {
        try {
            instance = this;
            saveDefaultConfig();
            this.providers.put("MongoDB", new MongodbProvider());
            this.providers.put("MySql", new MySqlProvider());
            this.providers.put("Yaml", new YamlProvider());
            if (!this.providers.containsKey(this.getConfig().getString("Provider"))) {
                this.getLogger().error("§4Please specify a valid provider: Yaml, MySql, MongoDB");
                return;
            }
            this.provider = this.providers.get(getConfig().getString("Provider"));
            this.provider.connect(this);
            this.getLogger().info("§aSuccessfully loaded " + provider.getProvider() + " provider.");
            LlamaRewardsAPI.setProvider(provider);
            Language.init();
            this.loadPlugin();
            this.getLogger().info("§aPlugin successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().error("§4Failed to load LlamaRewards.");
        }
    }

    private void loadPlugin() {
        this.getServer().getCommandMap().register("llamarewards", new RewardCommand(this));
        this.getServer().getPluginManager().registerEvents(new FormListener(), this);
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        this.loadRewards();
    }

    private void loadRewards() {
        for (String s : this.getConfig().getSection("Rewards").getAll().getKeys(false)) {
            String name = this.getConfig().getString("Rewards." + s + ".Name");
            int interval = this.getConfig().getInt("Rewards." + s + ".Interval");
            List<String> rewards = this.getConfig().getStringList("Rewards." + s + ".Rewards");
            String message = this.getConfig().getString("Rewards." + s + ".Message");
            LlamaRewardsAPI.cachedRewards.put(name, new Reward(name, interval, rewards, message));
        }
    }

    @Override
    public void onDisable() {
        this.provider.disconnect(this);
    }

}
