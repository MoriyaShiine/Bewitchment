package moriyashiine.bewitchment.mixin.integration.requiem;

import moriyashiine.bewitchment.api.registry.Transformation;
import moriyashiine.bewitchment.common.Bewitchment;
import moriyashiine.bewitchment.common.integration.requiem.interfaces.RequiemCompatAccessor;
import moriyashiine.bewitchment.common.registry.BWRegistries;
import moriyashiine.bewitchment.common.registry.BWTransformations;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RequiemCompatAccessor {
	private static final TrackedData<String> CACHED_TRANSFORMATION_FOR_REQUIEM = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);
	
	private boolean weakToSilverFromRequiem = false;
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}
	
	@Override
	public Transformation getCachedTransformationForRequiem() {
		return BWRegistries.TRANSFORMATIONS.get(new Identifier(dataTracker.get(CACHED_TRANSFORMATION_FOR_REQUIEM)));
	}
	
	@Override
	public void setCachedTransformationForRequiem(Transformation transformation) {
		dataTracker.set(CACHED_TRANSFORMATION_FOR_REQUIEM, BWRegistries.TRANSFORMATIONS.getId(transformation).toString());
	}
	
	@Override
	public boolean getWeakToSilverFromRequiem() {
		return weakToSilverFromRequiem;
	}
	
	@Override
	public void setWeakToSilverFromRequiem(boolean weakToSilver) {
		this.weakToSilverFromRequiem = weakToSilver;
	}
	
	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo callbackInfo) {
		if (Bewitchment.isRequiemLoaded) {
			setWeakToSilverFromRequiem(nbt.getBoolean("WeakToSilverFromRequiem"));
			if (nbt.contains("CachedTransformationForRequiem")) {
				setCachedTransformationForRequiem(BWRegistries.TRANSFORMATIONS.get(new Identifier(nbt.getString("CachedTransformationForRequiem"))));
			}
		}
	}
	
	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo callbackInfo) {
		if (Bewitchment.isRequiemLoaded) {
			nbt.putBoolean("WeakToSilverFromRequiem", getWeakToSilverFromRequiem());
			nbt.putString("CachedTransformationForRequiem", BWRegistries.TRANSFORMATIONS.getId(getCachedTransformationForRequiem()).toString());
		}
	}
	
	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void initDataTracker(CallbackInfo callbackInfo) {
		dataTracker.startTracking(CACHED_TRANSFORMATION_FOR_REQUIEM, BWRegistries.TRANSFORMATIONS.getId(BWTransformations.HUMAN).toString());
	}
}
