package su.hynix.utils.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import su.hynix.hynix;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;

import static su.hynix.utils.Wrapper.mc;

public class TargetUtil {

    public static boolean isPlayerTarget(Entity entity, MultiBooleanSetting settings, boolean considerNaked) {
        if (!(entity instanceof PlayerEntity player) || entity == mc.player) return false;
        boolean isFriend = hynix.getInstance().getFriendManager().isFriend(player.getGameProfile().getName());
        boolean isNaked = player.getTotalArmorValue() == 0;
        if (isFriend) {
            return settings.is("Друзей");
        }
        if (considerNaked && isNaked) {
            return settings.is("Голых");
        }
        return settings.is("Игроков");
    }

    public static boolean isPlayerTarget(Entity entity, MultiBooleanSetting settings) {
        return isPlayerTarget(entity, settings, true);
    }

    public static boolean isVillagerTarget(LivingEntity entity, MultiBooleanSetting settings) {
        return entity instanceof VillagerEntity && settings.is("Жителей");
    }

    public static boolean isAnimalTarget(LivingEntity entity, MultiBooleanSetting settings) {
        return entity instanceof AnimalEntity && settings.is("Животных");
    }

    public static boolean isMobTarget(LivingEntity entity, MultiBooleanSetting settings) {
        return entity instanceof MobEntity && settings.is("Мобов");
    }

    public static boolean isMonsterTarget(LivingEntity entity, MultiBooleanSetting settings) {
        return entity instanceof MobEntity && !(entity instanceof AnimalEntity) && !(entity instanceof VillagerEntity) && settings.is("Монстров");
    }

    public static boolean isSelfTarget(Entity entity, MultiBooleanSetting settings) {
        return entity == mc.player && settings.is("Себя") && !mc.gameSettings.getPointOfView().firstPerson();
    }

    public static boolean isItemTarget(Entity entity, MultiBooleanSetting settings) {
        return entity instanceof ItemEntity && settings.is("Предметы");
    }


    public static boolean isEntityTarget(Entity entity, MultiBooleanSetting settings) {
        if (entity instanceof LivingEntity livingEntity) {
            return isSelfTarget(entity, settings) ||
                    isPlayerTarget(entity, settings, true) ||
                    isVillagerTarget(livingEntity, settings) ||
                    isAnimalTarget(livingEntity, settings) ||
                    isMonsterTarget(livingEntity, settings);
        }
        return isItemTarget(entity, settings);
    }
}