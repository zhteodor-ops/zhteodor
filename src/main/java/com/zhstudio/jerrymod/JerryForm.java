package com.zhstudio.jerrymod;

import net.minecraft.world.entity.EntityDimensions;

public enum JerryForm {
    NONE(0f, 0f, 0f),
    DOG(0.6f, 0.85f, 0.68f),
    DEMON(1.0f, 1.45f, 1.15f),
    SKINWALKER(0.8f, 2.6f, 2.3f);

    public final float width;
    public final float height;
    public final float eyeHeight;

    JerryForm(float width, float height, float eyeHeight) {
        this.width = width;
        this.height = height;
        this.eyeHeight = eyeHeight;
    }

    public EntityDimensions dimensions() {
        return EntityDimensions.scalable(width, height);
    }

    public static JerryForm byId(int id) {
        JerryForm[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}
