package net.lldv.llamarewards.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.form.element.ElementButton;
import net.lldv.llamarewards.LlamaRewards;
import net.lldv.llamarewards.components.data.Reward;
import net.lldv.llamarewards.components.forms.simple.SimpleForm;
import net.lldv.llamarewards.components.language.Language;

public class RewardCommand extends PluginCommand<LlamaRewards> {

    public RewardCommand(LlamaRewards owner) {
        super(owner.getConfig().getString("Commands.Reward.Name"), owner);
        this.setDescription(owner.getConfig().getString("Commands.Xpbank.Description"));
        this.setAliases(owner.getConfig().getStringList("Commands.Xpbank.Aliases").toArray(new String[]{}));
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            SimpleForm.Builder form = new SimpleForm.Builder(Language.getNP("reward-form-title"), Language.getNP("reward-form-content"));
            Reward.cachedRewards.keySet().forEach(reward -> {
                String status;
                if (this.getPlugin().provider.canRedeem(player, Reward.cachedRewards.get(reward))) status = Language.getNP("can-redeem");
                else status = Language.getNP("cannot-redeem");
                form.addButton(new ElementButton(reward.replace("&", "§") + " \n§r" + status), r -> {
                    if (status.equals(Language.getNP("cannot-redeem"))) {
                        player.sendMessage(Language.get("reward-cannot-redeem"));
                        return;
                    }
                    this.getPlugin().provider.redeemReward(player, Reward.cachedRewards.get(reward));
                });
            });
            form.build().send(player);
        }
        return false;
    }
}
