package com.zhstudio.jerrymod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zhstudio.jerrymod.network.JerryNetwork;
import com.zhstudio.jerrymod.network.JerrySyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.LogicalSide;

@Mod.EventBusSubscriber(modid = JerryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    private static final String CHEST_TAG = "jerrymod_got_bone_chest";

    private CommonEvents() {
    }

    // ---------------- Commands ----------------

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        for (String name : new String[]{"consoleJerry", "consolejerry"}) {
            d.register(Commands.literal(name)
                    .then(Commands.argument("player", EntityArgument.player())
                            .executes(CommonEvents::runConsoleJerry)));
        }
        for (String name : new String[]{"unJerry", "unjerry"}) {
            d.register(Commands.literal(name).executes(CommonEvents::runUnJerry));
        }
    }

    private static int runConsoleJerry(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        ServerPlayer caller = source.getPlayer();
        if (caller != null && caller != target && !source.hasPermission(2)) {
            source.sendFailure(Component.literal("You can only turn yourself into Jerry. Type /consoleJerry " + caller.getGameProfile().getName()));
            return 0;
        }
        Component error = JerryLogic.makeJerry(target);
        if (error != null) {
            source.sendFailure(error);
            return 0;
        }
        return 1;
    }

    private static int runUnJerry(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer caller = source.getPlayer();
        if (caller != null && !JerryState.isJerry(caller) && !source.hasPermission(2)) {
            source.sendFailure(Component.literal("Only Jerry (or an admin) can do that."));
            return 0;
        }
        Component error = JerryLogic.clearJerry(source.getServer());
        if (error != null) {
            source.sendFailure(error);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Jerry is gone... for now."), true);
        return 1;
    }

    /** Also accept "consoleJerry NAME" or "[consoleJerry NAME]" typed in normal chat. */
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        String text = event.getRawText().trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        if (!text.toLowerCase().startsWith("consolejerry")) {
            return;
        }
        event.setCanceled(true);
        ServerPlayer caller = event.getPlayer();
        String[] parts = text.split("\\s+");
        String name = parts.length > 1 ? parts[1] : caller.getGameProfile().getName();
        MinecraftServer server = caller.getServer();
        if (server == null) {
            return;
        }
        ServerPlayer target = server.getPlayerList().getPlayerByName(name);
        if (target == null) {
            caller.sendSystemMessage(Component.literal("No player called " + name + " is online.").withStyle(ChatFormatting.RED));
            return;
        }
        if (target != caller && !caller.hasPermissions(2)) {
            caller.sendSystemMessage(Component.literal("You can only turn yourself into Jerry.").withStyle(ChatFormatting.RED));
            return;
        }
        Component error = JerryLogic.makeJerry(target);
        if (error != null) {
            caller.sendSystemMessage(error.copy().withStyle(ChatFormatting.RED));
        }
    }

    // ---------------- Joining: sync + bone chest ----------------

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        JerryData data = JerryData.get(server);
        JerryNetwork.sendTo(player, new JerrySyncPacket(data.getJerry(), data.getForm()));
        player.refreshDimensions();

        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.getBoolean(CHEST_TAG)) {
            if (placeBoneChest(player)) {
                persisted.putBoolean(CHEST_TAG, true);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            }
        }
    }

    private static boolean placeBoneChest(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos base = player.blockPosition();
        int[][] offsets = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}, {2, 2}, {-2, -2}, {2, -2}, {-2, 2}, {3, 0}, {0, 3}};
        for (int[] o : offsets) {
            for (int dy = 1; dy >= -2; dy--) {
                BlockPos pos = base.offset(o[0], dy, o[1]);
                BlockPos below = pos.below();
                if (level.isEmptyBlock(pos) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ChestBlockEntity chest) {
                        chest.setItem(0, new ItemStack(Items.BONE, 32));
                        chest.setItem(1, new ItemStack(Items.BONE, 32));
                        ItemStack tag = new ItemStack(Items.NAME_TAG);
                        tag.setHoverName(Component.literal("Jerry"));
                        chest.setItem(2, tag);
                        chest.setItem(3, new ItemStack(Items.COOKED_BEEF, 16));
                        chest.setItem(4, new ItemStack(Items.LEAD, 2));
                        chest.setCustomName(Component.literal("Bones for your dog"));
                        chest.setChanged();
                    }
                    spawnDogsNear(level, pos);
                    player.sendSystemMessage(Component.literal("A chest with bones appeared next to you. Find a dog and tame it...")
                            .withStyle(ChatFormatting.GOLD));
                    return true;
                }
            }
        }
        return false;
    }

    /** Make sure there are normal dogs (wolves) around to tame. */
    private static void spawnDogsNear(ServerLevel level, BlockPos pos) {
        boolean dogsNearby = !level.getEntitiesOfClass(Wolf.class, new net.minecraft.world.phys.AABB(pos).inflate(32)).isEmpty();
        if (dogsNearby) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            Wolf wolf = EntityType.WOLF.create(level);
            if (wolf == null) {
                continue;
            }
            int x = pos.getX() + (i == 0 ? 4 : -4);
            int z = pos.getZ() + (i == 0 ? 3 : -3);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            wolf.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360f, 0f);
            level.addFreshEntity(wolf);
        }
    }

    // ---------------- Size of Jerry ----------------

    @SubscribeEvent
    public static void onSize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        JerryForm form;
        try {
            form = JerryState.getForm(player);
        } catch (Exception e) {
            return; // player not fully created yet
        }
        if (form == JerryForm.NONE) {
            return;
        }
        event.setNewSize(form.dimensions());
        event.setNewEyeHeight(form.eyeHeight);
    }

    // ---------------- Powers ----------------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        JerryForm form = JerryState.getForm(player);
        if (form != JerryForm.NONE) {
            JerryLogic.applyEffects(player, form);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        JerryForm form = JerryState.getForm(attacker);
        if (form == JerryForm.DEMON) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        } else if (form == JerryForm.SKINWALKER) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
        }
    }
}
