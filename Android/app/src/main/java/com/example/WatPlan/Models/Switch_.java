package com.example.WatPlan.Models;

import android.widget.Switch;

public class Switch_ {
    private Switch switch_;
    private String name;
    private BlockFilter blockFilter;

    public Switch_(Switch switch_, String name, BlockFilter blockFilter) {
        this.switch_ = switch_;
        this.name = name;
        this.blockFilter = blockFilter;
    }

    public Switch getSwitch() {
        return switch_;
    }

    public String getName() {
        return name;
    }

    public BlockFilter getBlockFilter() {
        return blockFilter;
    }
}