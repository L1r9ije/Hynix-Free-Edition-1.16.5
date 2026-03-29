package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.component.impl.RotationComponent;
import su.hynix.events.*;
import su.hynix.handlers.impl.Rotation;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.utils.math.AuraUtil;
import su.hynix.utils.player.InventoryUtil;

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class AutoExplosion extends Module {
    private final BooleanSetting test = new BooleanSetting("Test", true);
    private BlockPos targetPos;
    private int targetSlot;
    private int oldSlot;
    private boolean needSync;
    private AxisAlignedBB crystalArea;
    private boolean blocked;

    public AutoExplosion() {
        super("AutoExplosion", "Автоматичeски взрывает кристаллы", Category.Combat);
        addSettings(test);
    }

    @EventTarget
    public void onEvent(EventPlaceBlock e) {
        if (e.getBlock() == Blocks.OBSIDIAN) {
            if (mc.player.getCooldownTracker().hasCooldown(Items.END_CRYSTAL)) {
                return;
            }
            int slotInHotBar = InventoryUtil.getSlot(Items.END_CRYSTAL);
            if (slotInHotBar == -1) {
                return;
            }
            if (slotInHotBar < 9) {
                targetPos = e.getPos();
                targetSlot = slotInHotBar;
            }
        }
        blocked = true;
    }

    @EventTarget
    private void onEvent(EventUpdate e) {
        if (needSync) {
            needSync = false;
            mc.player.inventory.currentItem = oldSlot;
            mc.playerController.syncCurrentPlayItem();
        } else if (targetPos != null) {
            if (mc.world.getBlockState(targetPos).isAir()) {
                targetPos = null;
            } else if (blocked) {
                blocked = false;
            } else {
                Vector3d eyeVec = mc.player.getEyePosition(1.0F);
                Vector3d hitVec = AuraUtil.getClosestVec(eyeVec, new AxisAlignedBB(targetPos));
                Vector3d offset = hitVec.subtract(eyeVec);
                float targetYaw = (float) wrapDegrees(Math.toDegrees(Math.atan2(offset.z, offset.x)) - 90);
                float targetPitch = (float) (-Math.toDegrees(Math.atan2(offset.y, hypot(offset.x, offset.z))));
                RotationComponent.update(new Rotation(targetYaw, targetPitch), 180, 1, 6);
                oldSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = targetSlot;
                mc.playerController.syncCurrentPlayItem();
                offset = offset.inverse();
                mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(hitVec, Direction.getFacingFromVector(offset.x, offset.y, offset.z), targetPos, false));
                mc.player.swingArm(Hand.MAIN_HAND);
                needSync = true;
                targetPos = null;
            }
        }
    }

    @EventTarget
    private void onClick(final EventClick e) {
        if (test.get()) {
            int slotCrystal = InventoryUtil.getItemSlot(Items.END_CRYSTAL);
            if (slotCrystal != -1) {
                if ((mc.player.getHeldItemMainhand().getItem() instanceof BlockItem && !(mc.player.getHeldItemMainhand().getItem() == Items.PLAYER_HEAD)) || mc.player.getHeldItemOffhand().getItem() instanceof BlockItem && !(mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD)) {
                    return;
                }

                if (mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
                    BlockPos targetPos = blockRayTraceResult.getPos();
                    if (mc.world.getBlockState(targetPos).getBlock() == Blocks.OBSIDIAN && mc.world.getBlockState(targetPos.up()).getBlock() == Blocks.AIR) {
                        mc.playerController.windowClick(0, slotCrystal < 9 ? slotCrystal + 36 : slotCrystal, 40, ClickType.SWAP, mc.player);
                        mc.playerController.func_217292_a(mc.player, mc.world, Hand.OFF_HAND, blockRayTraceResult);
                        mc.playerController.windowClick(0, slotCrystal < 9 ? slotCrystal + 36 : slotCrystal, 40, ClickType.SWAP, mc.player);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventTarget
    private void onEvent(EventPostUpdate e) {
        if (crystalArea != null) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof EnderCrystalEntity && crystalArea.contains(entity.getPositionVec())) {
                    if (!entity.getBoundingBox().contains(mc.player.getEyePosition(1.0F))) {
                        Vector3d direction = AuraUtil.getVector(entity);
                        float targetYaw = (float) wrapDegrees(Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
                        float targetPitch = (float) (-Math.toDegrees(Math.atan2(direction.y, hypot(direction.x, direction.z))));
                        RotationComponent.update(new Rotation(targetYaw, targetPitch), 180, 1, 6);
                    }
                    mc.playerController.attackEntity(mc.player, entity);
                    mc.player.swingArm(Hand.MAIN_HAND);
                    crystalArea = null;
                    return;
                }
            }
        }
    }

    @EventTarget
    private void onEvent(EventClickBlockRight e) {
        if (!mc.player.getCooldownTracker().hasCooldown(Items.END_CRYSTAL)) {
            Block block = e.getWorld().getBlockState(e.getResult().getPos()).getBlock();
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
                crystalArea = (new AxisAlignedBB(e.getResult().getPos().up())).grow(0.1);
            }
        }
    }
}
