package com.example.WatPlan.Models;

import java.util.HashMap;
import java.util.Map;

public interface BlockFilter {
    String NO_FILTER = "---";

    Map<String, BlockFilter> filterMap = new HashMap<String, BlockFilter>() {{
        put(Preferences.LECTURE, block -> !block.getClassType().equals("w"));
        put(Preferences.EXERCISE, block -> !block.getClassType().equals("ć"));
        put(Preferences.LABORATORY, block -> !block.getClassType().equals("L"));
    }};

    boolean filter(Block block);


    static Map<String, BlockFilter> getFiltermap() {
        return filterMap;
    }

}
