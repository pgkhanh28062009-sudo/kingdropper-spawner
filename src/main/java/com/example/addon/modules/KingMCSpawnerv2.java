package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class KingMCSpawnerv2 extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // 1. Chỉnh tốc độ (Delay tick)
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("toc-do-delay")
        .description("Thời gian chờ giữa các thao tác (tính bằng tick).")
        .defaultValue(5)
        .min(1)
        .sliderMax(20)
        .build()
    );

    // 2. Danh sách vật phẩm cần ném
    private final Setting<List<Item>> targetItems = sgGeneral.add(new ItemListSetting.Builder()
        .name("vat-pham-nem")
        .description("Chọn các vật phẩm cần phân loại và ném ra ngoài.")
        .build()
    );

    // 3. Vị trí ô Dispenser trong Spawner
    private final Setting<Integer> dispenserSlot = sgGeneral.add(new IntSetting.Builder()
        .name("slot-dispenser")
        .description("Vị trí ô Dispenser trong GUI Spawner (mặc định ô 4).")
        .defaultValue(4)
        .min(0)
        .sliderMax(53)
        .build()
    );

    private int timer = 0;

    public KingMCSpawnerv2(Category category) {
        super(category, "kingmc-spawner-v2", "Tự động mở Spawner KingMC, ấn Dispenser, lọc và ném vật phẩm.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        timer++;
        if (timer < delay.get()) return;
        timer = 0;

        // Trạng thái 1: Chưa mở GUI -> Tìm và mở Spawner xung quanh
        if (mc.currentScreen == null) {
            BlockPos spawnerPos = findNearbySpawner();
            if (spawnerPos != null) {
                mc.interactionManager.interactBlock(
                    mc.player,
                    Hand.MAIN_HAND,
                    new BlockHitResult(
                        Vec3d.ofCenter(spawnerPos),
                        Direction.UP,
                        spawnerPos,
                        false
                    )
                );
            }
        } 
        // Trạng thái 2: Đang mở GUI Spawner -> Click Dispenser & Lọc ném đồ
        else if (mc.currentScreen instanceof GenericContainerScreen container) {
            int syncId = container.getScreenHandler().syncId;

            // Click vào nút Dispenser
            mc.interactionManager.clickSlot(syncId, dispenserSlot.get(), 0, SlotActionType.PICKUP, mc.player);

            boolean hasJunkItem = false;
            int containerSlots = container.getScreenHandler().slots.size() - 36;

            for (int i = 0; i < containerSlots; i++) {
                ItemStack stack = container.getScreenHandler().getSlot(i).getStack();
                if (!stack.isEmpty()) {
                    // Nếu gặp vật phẩm không nằm trong danh sách cần ném -> Đánh dấu là đồ rác/mắc kẹt
                    if (!targetItems.get().contains(stack.getItem())) {
                        hasJunkItem = true;
                        break;
                    } else {
                        // Ném vật phẩm ra ngoài
                        mc.interactionManager.clickSlot(syncId, i, 1, SlotActionType.THROW, mc.player);
                    }
                }
            }

            // Nếu gặp rác không cần thiết hoặc đã xử lý xong -> Thoát GUI để lặp lại
            mc.player.closeHandledScreen();
        }
    }

    // Quét tìm vị trí Spawner trong phạm vi 4 ô
    private BlockPos findNearbySpawner() {
        BlockPos pPos = mc.player.getBlockPos();
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = pPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).isOf(Blocks.SPAWNER)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}

