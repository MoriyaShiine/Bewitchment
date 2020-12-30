package moriyashiine.bewitchment.common.statuseffect;

import moriyashiine.bewitchment.api.interfaces.MagicAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerEntity;

public class InvigoratingStatusEffect extends StatusEffect {
	public InvigoratingStatusEffect(StatusEffectType type, int color) {
		super(type, color);
	}
	
	@Override
	public boolean isInstant() {
		return true;
	}
	
	@Override
	public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
		if (entity instanceof PlayerEntity) {
			MagicAccessor.of((PlayerEntity) entity).ifPresent(magicAccessor -> magicAccessor.fillMagic(1000 * (amplifier + 1), false));
		}
	}
}
