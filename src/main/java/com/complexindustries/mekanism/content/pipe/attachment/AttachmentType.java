package com.complexindustries.mekanism.content.pipe.attachment;

public enum AttachmentType {
    INPUT("input"),
    OUTPUT("output"),
    POWER("power");

    private final String name;

    AttachmentType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
