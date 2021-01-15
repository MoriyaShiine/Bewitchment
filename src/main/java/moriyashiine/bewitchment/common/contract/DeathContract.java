package moriyashiine.bewitchment.common.contract;

import moriyashiine.bewitchment.api.interfaces.ContractAccessor;
import moriyashiine.bewitchment.api.interfaces.CurseAccessor;
import moriyashiine.bewitchment.api.registry.Contract;
import moriyashiine.bewitchment.api.registry.Curse;
import moriyashiine.bewitchment.common.registry.BWRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class DeathContract extends Contract {
	public DeathContract(boolean canBeGiven) {
		super(canBeGiven);
	}
	
	public void tick(LivingEntity target, boolean includeNegative) {
		if (target.damage(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE) && target.isDead()) {
			ContractAccessor.of(target).ifPresent(contractAccessor -> contractAccessor.removeContract(this));
		}
	}
	
	@Override
	public void finishUsing(LivingEntity user, boolean includeNegative) {
		if (includeNegative) {
			CurseAccessor.of(user).ifPresent(curseAccessor -> {
				Curse curse = null;
				while (curse == null || curse.type != Curse.Type.LESSER) {
					curse = BWRegistries.CURSES.get(user.getRandom().nextInt(BWRegistries.CURSES.getEntries().size()));
				}
				curseAccessor.addCurse(new Curse.Instance(curse, 24000));
			});
		}
	}
}
