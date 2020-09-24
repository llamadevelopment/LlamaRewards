package net.lldv.llamarewards.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.api.LlamaRewardsAPI;
import net.lldv.llamarewards.components.forms.simple.SimpleForm;
import net.lldv.llamarewards.components.language.Language;

public class RewardCommand extends Command {

    private final LlamaRewards instance;

    public RewardCommand(LlamaRewards instance) {
        super(instance.getConfig().getString("Commands.Reward.Name"), instance.getConfig().getString("Commands.Reward.Description"));
        this.instance = instance;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            SimpleForm.Builder form = new SimpleForm.Builder(Language.getNP("reward-form-title"), Language.getNP("reward-form-content"));
            LlamaRewardsAPI.cachedRewards.keySet().forEach(reward -> {
                String status;
                if (this.instance.provider.canRedeem(player, LlamaRewardsAPI.cachedRewards.get(reward))) status = Language.getNP("can-redeem");
                else status = Language.getNP("cannot-redeem");
                form.addButton(new ElementButton(reward.replace("&", "§") + " \n§r" + status), r -> {
                    if (status.equals(Language.getNP("cannot-redeem"))) {
                        player.sendMessage(Language.get("reward-cannot-redeem"));
                        return;
                    }
                    this.instance.provider.redeemReward(player, LlamaRewardsAPI.cachedRewards.get(reward));
                });
            });
            SimpleForm finalForm = form.build();
            finalForm.send(player);
        }
        return false;
    }
}
