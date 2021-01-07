package moriyashiine.bewitchment.mixin;

import moriyashiine.bewitchment.api.interfaces.ContractAccessor;
import moriyashiine.bewitchment.api.interfaces.FortuneAccessor;
import moriyashiine.bewitchment.api.interfaces.MagicAccessor;
import moriyashiine.bewitchment.api.interfaces.PolymorphAccessor;
import moriyashiine.bewitchment.api.registry.Fortune;
import moriyashiine.bewitchment.common.registry.BWContracts;
import moriyashiine.bewitchment.common.registry.BWRegistries;
import moriyashiine.bewitchment.common.registry.BWStatusEffects;
import moriyashiine.bewitchment.common.registry.BWTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements MagicAccessor, PolymorphAccessor, FortuneAccessor {
	private static final TrackedData<Integer> MAGIC = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> MAGIC_TIMER = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	private static final TrackedData<Optional<UUID>> POLYMORPH_UUID = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	private static final TrackedData<String> POLYMORPH_NAME = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);
	
	private Fortune.Instance fortune = null;
	
	@Shadow
	public abstract HungerManager getHungerManager();
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}
	
	@Override
	public int getMagic() {
		return dataTracker.get(MAGIC);
	}
	
	@Override
	public void setMagic(int magic) {
		dataTracker.set(MAGIC, magic);
	}
	
	@Override
	public int getMagicTimer() {
		return dataTracker.get(MAGIC_TIMER);
	}
	
	@Override
	public void setMagicTimer(int magicTimer) {
		dataTracker.set(MAGIC_TIMER, magicTimer);
	}
	
	@Override
	public Fortune.Instance getFortune() {
		return fortune;
	}
	
	@Override
	public void setFortune(Fortune.Instance fortune) {
		this.fortune = fortune;
	}
	
	@Override
	public Optional<UUID> getPolymorphUUID() {
		return dataTracker.get(POLYMORPH_UUID);
	}
	
	@Override
	public void setPolymorphUUID(Optional<UUID> uuid) {
		dataTracker.set(POLYMORPH_UUID, uuid);
	}
	
	@Override
	public String getPolymorphName() {
		return dataTracker.get(POLYMORPH_NAME);
	}
	
	@Override
	public void setPolymorphName(String name) {
		dataTracker.set(POLYMORPH_NAME, name);
	}
	
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo callbackInfo) {
		if (getMagicTimer() > 0) {
			setMagicTimer(getMagicTimer() - 1);
		}
		if (!world.isClient && getFortune() != null) {
			if (getFortune().fortune.tick((ServerWorld) world, (PlayerEntity) (Object) this)) {
				getFortune().duration = 0;
			}
			else {
				getFortune().duration--;
			}
			if (getFortune().duration <= 0) {
				if (getFortune().fortune.finish((ServerWorld) world, (PlayerEntity) (Object) this)) {
					setFortune(null);
				}
				else {
					getFortune().duration = world.random.nextInt(120000);
				}
			}
		}
	}
	
	@Inject(method = "eatFood", at = @At("HEAD"))
	private void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> callbackInfo) {
		if (hasStatusEffect(BWStatusEffects.NOURISHING)) {
			getHungerManager().add(getStatusEffect(BWStatusEffects.NOURISHING).getAmplifier() + 2, 0.5f);
		}
		FoodComponent foodComponent = stack.getItem().getFoodComponent();
		if (foodComponent != null) {
			if (BWTags.WITCHBERRY_FOODS.contains(stack.getItem())) {
				MagicAccessor.of(this).ifPresent(magicAccessor -> magicAccessor.fillMagic(foodComponent.getHunger() * 100, false));
			}
			ContractAccessor.of(this).ifPresent(contractAccessor -> {
				if (contractAccessor.hasContract(BWContracts.GLUTTONY)) {
					if (contractAccessor.hasNegativeEffects() && random.nextFloat() < 1 / 10f) {
						getHungerManager().add(-foodComponent.getHunger(), foodComponent.getSaturationModifier());
					}
					else {
						getHungerManager().add(foodComponent.getHunger(), 0);
					}
				}
			});
		}
	}
	
	@Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
	private void readCustomDataFromTag(CompoundTag tag, CallbackInfo callbackInfo) {
		setMagic(tag.getInt("Magic"));
		if (tag.contains("Fortune")) {
			setFortune(new Fortune.Instance(BWRegistries.FORTUNES.get(new Identifier(tag.getString("Fortune"))), tag.getInt("FortuneDuration")));
		}
		setPolymorphUUID(tag.getString("PolymorphUUID").isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(tag.getString("PolymorphUUID"))));
		setPolymorphName(tag.getString("PolymorphName"));
	}
	
	@Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
	private void writeCustomDataToTag(CompoundTag tag, CallbackInfo callbackInfo) {
		tag.putInt("Magic", getMagic());
		if (getFortune() != null) {
			tag.putString("Fortune", BWRegistries.FORTUNES.getId(getFortune().fortune).toString());
			tag.putInt("FortuneDuration", getFortune().duration);
		}
		tag.putString("PolymorphUUID", getPolymorphUUID().isPresent() ? getPolymorphUUID().get().toString() : "");
		tag.putString("PolymorphName", getPolymorphName());
	}
	
	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void initDataTracker(CallbackInfo callbackInfo) {
		dataTracker.startTracking(MAGIC, 0);
		dataTracker.startTracking(MAGIC_TIMER, 60);
		dataTracker.startTracking(POLYMORPH_UUID, Optional.empty());
		dataTracker.startTracking(POLYMORPH_NAME, "");
	}
}
