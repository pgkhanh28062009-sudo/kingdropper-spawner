package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
        if (mc.player == null || mc.level == null) return;

        timer++;
        if (timer < delay.get()) return;
        timer = 0;

        // Trạng thái 1: Chưa mở GUI -> Tìm và mở Spawner xung quanh
        if (mc.screen == null) {
            BlockPos spawnerPos = findNearbySpawner();
            if (spawnerPos != null && mc.gameMode != null) {
                mc.gameMode.useItemOn(
                    mc.player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(
                        Vec3.atCenterOf(spawnerPos),
                        Direction.UP,
                        spawnerPos,
                        false
                    )
                );
            }
        } 
        // Trạng thái 2: Đang mở GUI Spawner -> Click Dispenser & Lọc ném đồ
        else if (mc.screen instanceof AbstractContainerScreen<?> container) {
            int syncId = container.getMenu().containerId;

            if (mc.gameMode != null) {
                // Click vào nút Dispenser
                mc.gameMode.handleInventoryMouseClick(syncId, dispenserSlot.get(), 0, ClickType.PICKUP, mc.player);

                boolean hasJunkItem = false;
                int containerSlots = container.getMenu().slots.size() - 36;

                for (int i = 0; i < containerSlots; i++) {
                    ItemStack stack = container.getMenu().getSlot(i).getItem();
                    if (!stack.isEmpty()) {
                        // Nếu gặp vật phẩm không nằm trong danh sách cần ném -> Đánh dấu là đồ rác
                        if (!targetItems.get().contains(stack.getItem())) {
                            hasJunkItem = true;
                            break;
                        } else {
                            // Ném vật phẩm ra ngoài
                            mc.gameMode.handleInventoryMouseClick(syncId, i, 1, ClickType.THROW, mc.player);
                        }
                    }
                }

                // Đóng GUI màn hình
                mc.setScreen(null);
            }
        }
    }

    // Quét tìm vị trí Spawner trong phạm vi 4 ô
    private BlockPos findNearbySpawner() {
        BlockPos pPos = mc.player.blockPosition();
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = pPos.offset(x, y, z);
                    if (mc.level.getBlockState(pos).is(Blocks.SPAWNER)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}
