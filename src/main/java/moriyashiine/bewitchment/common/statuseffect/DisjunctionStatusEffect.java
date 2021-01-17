package moriyashiine.bewitchment.common.statuseffect;

import moriyashiine.bewitchment.api.BewitchmentAPI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

public class DisjunctionStatusEffect extends StatusEffect {
	public DisjunctionStatusEffect(StatusEffectType type, int color) {
		super(type, color);
	}
	
	@Override
	public boolean isInstant() {
		return true;
	}
	
	@Override
	public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
		if (!entity.world.isClient) {
			BewitchmentAPI.attemptTeleport(entity, entity.getBlockPos(), 8 * (amplifier + 1), true);
		}
	}
}
