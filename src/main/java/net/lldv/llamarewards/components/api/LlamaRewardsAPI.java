package net.lldv.llamarewards.components.api;

import lombok.Getter;
import lombok.Setter;
import net.lldv.llamarewards.components.data.Reward;
import net.lldv.llamarewards.components.provider.Provider;

import java.util.LinkedHashMap;

public class LlamaRewardsAPI {

    public static LinkedHashMap<String, Reward> cachedRewards = new LinkedHashMap<>();

    @Setter
    @Getter
    private static Provider provider;

}
