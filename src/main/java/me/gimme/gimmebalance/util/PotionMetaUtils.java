package me.gimme.gimmebalance.util;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PotionMetaUtils {

    /**
     * Returns the names of all potion effect types from the specified potion meta and if they are extended and upgraded
     * or not. If the potion does not have any custom effects this will only contain 1 or 0 effects.
     *
     * @param potionMeta the potion meta to get the effects from
     * @return the potion effects from the specified potion meta
     */
    public static List<PotionEffectResult> getPotionEffectTypeNames(PotionMeta potionMeta) {
        List<PotionEffectResult> result = new ArrayList<>();

        if (potionMeta.getBasePotionData().getType().getEffectType() != null)
            result.add(new PotionEffectResult(potionMeta.getBasePotionData()));

        if (potionMeta.hasCustomEffects()) {
            for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                result.add(new PotionEffectResult(potionEffect.getType().getName(), potionEffect.getType(), false, false));
            }
        }
        return result;
    }

    public static PotionEffectResult getBasePotionEffectTypeName(PotionMeta potionMeta) {
        return new PotionEffectResult(potionMeta.getBasePotionData());
    }

    public enum ExtendedOrUpgraded {
        EXTENDED,
        UPGRADED
    }

    public static class PotionEffectResult {
        private String name = null;
        private PotionEffectType type = null;
        private ExtendedOrUpgraded extendedOrUpgraded = null;

        public PotionEffectResult(@NotNull String name,  @NotNull PotionEffectType type, boolean extended, boolean upgraded) {
            this.name = name;
            this.type = type;
            if (extended) extendedOrUpgraded = ExtendedOrUpgraded.EXTENDED;
            else if (upgraded) extendedOrUpgraded = ExtendedOrUpgraded.UPGRADED;
        }

        public PotionEffectResult(@NotNull PotionData potionData) {
            PotionEffectType type = potionData.getType().getEffectType();
            if (type != null) {
                this.name = type.getName();
                this.type = type;
            }
            if (potionData.isExtended()) extendedOrUpgraded = ExtendedOrUpgraded.EXTENDED;
            else if (potionData.isUpgraded()) extendedOrUpgraded = ExtendedOrUpgraded.UPGRADED;
        }

        @Nullable
        public String getName() {
            return name;
        }

        @Nullable
        public PotionEffectType getType() {
            return type;
        }

        @Nullable
        public ExtendedOrUpgraded getExtendedOrUpgraded() {
            return extendedOrUpgraded;
        }
    }

}
