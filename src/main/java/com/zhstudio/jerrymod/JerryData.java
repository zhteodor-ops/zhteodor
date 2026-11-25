package com.zhstudio.jerrymod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.UUID;

/** Saved with the world: who is Jerry and which form he is in. */
public class JerryData extends SavedData {
    private static final String NAME = "jerrymod";

    @Nullable
    private UUID jerry;
    private JerryForm form = JerryForm.DOG;

    public static JerryData get(MinecraftServer server) {
        JerryData data = server.overworld().getDataStorage().computeIfAbsent(JerryData::load, JerryData::new, NAME);
        JerryState.serverJerry = data.jerry;
        JerryState.serverForm = data.jerry == null ? JerryForm.NONE : data.form;
        return data;
    }

    public static JerryData load(CompoundTag tag) {
        JerryData data = new JerryData();
        if (tag.hasUUID("Jerry")) {
            data.jerry = tag.getUUID("Jerry");
        }
        JerryForm form = JerryForm.byId(tag.getInt("Form"));
        data.form = form == JerryForm.NONE ? JerryForm.DOG : form;
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (jerry != null) {
            tag.putUUID("Jerry", jerry);
        }
        tag.putInt("Form", form.ordinal());
        return tag;
    }

    @Nullable
    public UUID getJerry() {
        return jerry;
    }

    public JerryForm getForm() {
        return jerry == null ? JerryForm.NONE : form;
    }

    public void set(@Nullable UUID jerry, JerryForm form) {
        this.jerry = jerry;
        this.form = form == JerryForm.NONE ? JerryForm.DOG : form;
        JerryState.serverJerry = jerry;
        JerryState.serverForm = jerry == null ? JerryForm.NONE : this.form;
        setDirty();
    }
}
