package net.lldv.llamarewards.components.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;

@AllArgsConstructor
@Getter
public class Reward {

    private final String name;
    private final int interval;
    private final List<String> rewards;
    private final String message;

    public static LinkedHashMap<String, Reward> cachedRewards = new LinkedHashMap<>();

}
