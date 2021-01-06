package moriyashiine.bewitchment.mixin;

import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.api.interfaces.MasterAccessor;
import moriyashiine.bewitchment.api.interfaces.Pledgeable;
import moriyashiine.bewitchment.api.interfaces.WetAccessor;
import moriyashiine.bewitchment.common.world.BWUniversalWorldState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin implements WetAccessor {
	private int wetTimer = 0;
	
	@Shadow
	public abstract UUID getUuid();
	
	@Shadow
	public World world;
	
	@Override
	public int getWetTimer() {
		return wetTimer;
	}
	
	@Override
	public void setWetTimer(int wetTimer) {
		this.wetTimer = wetTimer;
	}
	
	@Inject(method = "isWet", at = @At("HEAD"), cancellable = true)
	private void isWet(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (getWetTimer() > 0) {
			callbackInfo.setReturnValue(true);
		}
	}
	
	@Inject(method = "isTouchingWaterOrRain", at = @At("HEAD"), cancellable = true)
	private void isTouchingWaterOrRain(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (getWetTimer() > 0) {
			callbackInfo.setReturnValue(true);
		}
	}
	
	@Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
	private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> callbackInfo) {
		Entity attacker = source.getAttacker();
		if (attacker instanceof LivingEntity) {
			Entity entity = (Entity) (Object) this;
			if (entity instanceof MasterAccessor) {
				MasterAccessor masterAccessor = (MasterAccessor) entity;
				UUID masterUUID = masterAccessor.getMasterUUID();
				if (masterUUID != null && masterUUID.equals(attacker.getUuid())) {
					callbackInfo.setReturnValue(true);
				}
			}
			if (attacker instanceof MasterAccessor) {
				MasterAccessor masterAccessor = (MasterAccessor) attacker;
				UUID masterUUID = masterAccessor.getMasterUUID();
				if (masterUUID != null && masterUUID.equals(getUuid())) {
					callbackInfo.setReturnValue(true);
				}
			}
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo callbackInfo) {
		if (getWetTimer() > 0) {
			setWetTimer(getWetTimer() - 1);
		}
	}
	
	@Inject(method = "remove", at = @At("HEAD"))
	private void remove(CallbackInfo callbackInfo) {
		if (!world.isClient && this instanceof Pledgeable) {
			BWUniversalWorldState worldState = BWUniversalWorldState.get(world);
			for (int i = worldState.specificPledges.size() - 1; i >= 0; i--) {
				Pair<UUID, UUID> pair = worldState.specificPledges.get(i);
				if (pair.getLeft().equals(getUuid())) {
					BewitchmentAPI.unpledge(world, ((Pledgeable) this).getPledgeUUID(), pair.getLeft());
					worldState.specificPledges.remove(i);
					worldState.markDirty();
				}
			}
		}
	}
	
	@Inject(method = "fromTag", at = @At("TAIL"))
	private void readCustomDataFromTag(CompoundTag tag, CallbackInfo callbackInfo) {
		setWetTimer(tag.getInt("WetTimer"));
	}
	
	@Inject(method = "toTag", at = @At("TAIL"))
	private void writeCustomDataToTag(CompoundTag tag, CallbackInfoReturnable<Tag> callbackInfo) {
		tag.putInt("WetTimer", getWetTimer());
	}
}
