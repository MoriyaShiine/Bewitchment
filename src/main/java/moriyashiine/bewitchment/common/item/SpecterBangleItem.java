package moriyashiine.bewitchment.common.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.common.entity.interfaces.TrueInvisibleAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;

public class SpecterBangleItem extends TrinketItem {
	public SpecterBangleItem(Settings settings) {
		super(settings);
	}
	
	@Override
	public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
		if (entity instanceof PlayerEntity player && player.isSneaking() && BewitchmentAPI.drainMagic(player, 1, true)) {
			if (!player.world.isClient) {
				if (player.getRandom().nextFloat() < 1 / 40f) {
					BewitchmentAPI.drainMagic(player, 1, false);
				}
				if (!((TrueInvisibleAccessor) player).getTrueInvisible()) {
					((TrueInvisibleAccessor) player).setTrueInvisible(true);
				}
			}
			else if (player.getRandom().nextFloat() < 1 / 6f) {
				player.world.addParticle(ParticleTypes.SMOKE, player.getParticleX(1), player.getY(), player.getParticleZ(1), 0, 0, 0);
			}
		}
	}
}
