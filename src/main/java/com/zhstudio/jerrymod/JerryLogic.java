package com.zhstudio.jerrymod;

import com.zhstudio.jerrymod.network.JerryNetwork;
import com.zhstudio.jerrymod.network.JerrySyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** All the server-side rules for becoming / changing Jerry. */
public final class JerryLogic {
    private static final Map<UUID, Long> LAST_CHANGE = new HashMap<>();

    private JerryLogic() {
    }

    /** Make {@code target} Jerry. Returns an error message, or null when it worked. */
    public static Component makeJerry(ServerPlayer target) {
        MinecraftServer server = target.getServer();
        if (server == null) {
            return Component.literal("No server.");
        }
        JerryData data = JerryData.get(server);
        UUID current = data.getJerry();
        if (current != null && !current.equals(target.getUUID())) {
            ServerPlayer other = server.getPlayerList().getPlayer(current);
            String name = other != null ? other.getGameProfile().getName() : "another player";
            return Component.literal("Only one player can be Jerry! " + name + " is already Jerry.");
        }
        if (current != null) {
            return Component.literal(target.getGameProfile().getName() + " is already Jerry.");
        }
        if (!hasRoomFor(target, JerryForm.DOG)) {
            return Component.literal("Not enough space to turn into Jerry here.");
        }
        data.set(target.getUUID(), JerryForm.DOG);
        afterChange(target, JerryForm.DOG);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal(target.getGameProfile().getName() + " has become Jerry...").withStyle(ChatFormatting.DARK_RED), false);
        target.sendSystemMessage(Component.literal("You are Jerry. Press F for demonic Jerry, J for skinwalker Jerry. /unJerry to stop.")
                .withStyle(ChatFormatting.GRAY));
        return null;
    }

    public static Component clearJerry(MinecraftServer server) {
        JerryData data = JerryData.get(server);
        UUID current = data.getJerry();
        if (current == null) {
            return Component.literal("Nobody is Jerry right now.");
        }
        data.set(null, JerryForm.DOG);
        ServerPlayer old = server.getPlayerList().getPlayer(current);
        if (old != null) {
            removeJerryEffects(old);
            afterChange(old, JerryForm.NONE);
            old.sendSystemMessage(Component.literal("You are no longer Jerry.").withStyle(ChatFormatting.GRAY));
        } else {
            sync(server);
        }
        return null;
    }

    /** F / J pressed. Pressing the key of the form you already have turns you back into normal Jerry. */
    public static void toggleForm(ServerPlayer player, JerryForm wanted) {
        if (wanted != JerryForm.DEMON && wanted != JerryForm.SKINWALKER) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        JerryData data = JerryData.get(server);
        if (data.getJerry() == null || !data.getJerry().equals(player.getUUID())) {
            return;
        }
        long now = player.level().getGameTime();
        Long last = LAST_CHANGE.get(player.getUUID());
        if (last != null && now - last < 20) {
            return; // 1 second cooldown
        }
        JerryForm next = data.getForm() == wanted ? JerryForm.DOG : wanted;
        if (!hasRoomFor(player, next)) {
            player.displayClientMessage(Component.literal("Not enough space to transform!").withStyle(ChatFormatting.RED), true);
            return;
        }
        LAST_CHANGE.put(player.getUUID(), now);
        data.set(player.getUUID(), next);
        afterChange(player, next);
    }

    private static boolean hasRoomFor(ServerPlayer player, JerryForm form) {
        AABB box = form.dimensions().makeBoundingBox(player.position());
        return player.level().noCollision(player, box.deflate(1.0E-4));
    }

    private static void afterChange(ServerPlayer player, JerryForm form) {
        player.refreshDimensions();
        sync(player.getServer());
        if (player.level() instanceof ServerLevel level) {
            double x = player.getX();
            double y = player.getY() + 0.8;
            double z = player.getZ();
            SoundEvent sound;
            float pitch;
            switch (form) {
                case DEMON -> {
                    sound = SoundEvents.WARDEN_ROAR;
                    pitch = 0.7f;
                    level.sendParticles(ParticleTypes.FLAME, x, y, z, 40, 0.5, 0.8, 0.5, 0.03);
                }
                case SKINWALKER -> {
                    sound = SoundEvents.WOLF_HOWL;
                    pitch = 0.55f;
                    level.sendParticles(ParticleTypes.SOUL, x, y + 0.6, z, 30, 0.4, 1.2, 0.4, 0.02);
                }
                case DOG -> {
                    sound = SoundEvents.WOLF_AMBIENT;
                    pitch = 1.0f;
                }
                default -> {
                    sound = SoundEvents.WOLF_WHINE;
                    pitch = 1.0f;
                }
            }
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 25, 0.4, 0.6, 0.4, 0.02);
            level.playSound(null, x, y, z, sound, SoundSource.PLAYERS, 1.5f, pitch);
        }
        applyEffects(player, form);
    }

    public static void sync(MinecraftServer server) {
        if (server == null) {
            return;
        }
        JerryData data = JerryData.get(server);
        JerryNetwork.sendToAll(new JerrySyncPacket(data.getJerry(), data.getForm()));
    }

    /** Called every second for Jerry. Effects last a bit longer than a second so they never run out. */
    public static void applyEffects(ServerPlayer player, JerryForm form) {
        switch (form) {
            case DOG -> {
                give(player, MobEffects.MOVEMENT_SPEED, 0);
                give(player, MobEffects.JUMP, 0);
                give(player, MobEffects.NIGHT_VISION, 0);
            }
            case DEMON -> {
                give(player, MobEffects.MOVEMENT_SPEED, 1);
                give(player, MobEffects.DAMAGE_BOOST, 1);
                give(player, MobEffects.DAMAGE_RESISTANCE, 0);
                give(player, MobEffects.FIRE_RESISTANCE, 0);
                give(player, MobEffects.NIGHT_VISION, 0);
            }
            case SKINWALKER -> {
                give(player, MobEffects.MOVEMENT_SPEED, 2);
                give(player, MobEffects.JUMP, 2);
                give(player, MobEffects.NIGHT_VISION, 0);
            }
            default -> {
            }
        }
    }

    private static void give(ServerPlayer player, MobEffect effect, int amplifier) {
        int duration = effect == MobEffects.NIGHT_VISION ? 300 : 40;
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
    }

    private static void removeJerryEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        player.removeEffect(MobEffects.JUMP);
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.DAMAGE_BOOST);
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.removeEffect(MobEffects.FIRE_RESISTANCE);
    }
}
