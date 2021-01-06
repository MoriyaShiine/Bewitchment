package moriyashiine.bewitchment.api;

import moriyashiine.bewitchment.api.registry.AltarMapEntry;
import moriyashiine.bewitchment.client.network.packet.SpawnPortalParticlesPacket;
import moriyashiine.bewitchment.common.entity.projectile.SilverArrowEntity;
import moriyashiine.bewitchment.common.item.TaglockItem;
import moriyashiine.bewitchment.common.registry.BWObjects;
import moriyashiine.bewitchment.common.registry.BWSoundEvents;
import moriyashiine.bewitchment.common.registry.BWTags;
import moriyashiine.bewitchment.common.world.BWUniversalWorldState;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
public class BewitchmentAPI {
	public static Set<AltarMapEntry> ALTAR_MAP_ENTRIES = new HashSet<>();
	
	@SuppressWarnings("InstantiationOfUtilityClass")
	public static EntityGroup DEMON = new EntityGroup();
	
	public static LivingEntity getTaglockOwner(World world, ItemStack taglock) {
		if (world instanceof ServerWorld && taglock.getItem() instanceof TaglockItem && taglock.hasTag() && taglock.getOrCreateTag().contains("OwnerUUID")) {
			UUID ownerUUID = taglock.getOrCreateTag().getUuid("OwnerUUID");
			for (ServerWorld serverWorld : world.getServer().getWorlds()) {
				Entity entity = serverWorld.getEntity(ownerUUID);
				if (entity instanceof LivingEntity) {
					return (LivingEntity) entity;
				}
			}
		}
		return null;
	}
	
	public static boolean isSourceFromSilver(DamageSource source) {
		Entity attacker = source.getAttacker();
		return !(source instanceof EntityDamageSource && ((EntityDamageSource) source).isThorns()) && (attacker instanceof LivingEntity && isHoldingSilver((LivingEntity) attacker, Hand.MAIN_HAND)) || attacker instanceof SilverArrowEntity;
	}
	
	public static boolean isHoldingSilver(LivingEntity livingEntity, Hand hand) {
		return BWTags.SILVER_TOOLS.contains(livingEntity.getStackInHand(hand).getItem());
	}
	
	public static boolean isWeakToSilver(LivingEntity livingEntity) {
		return BWTags.WEAK_TO_SILVER.contains(livingEntity.getType());
	}
	
	public static boolean hasPledge(World world, UUID entity) {
		BWUniversalWorldState worldState = BWUniversalWorldState.get(world);
		for (Pair<UUID, List<UUID>> pair : worldState.pledges) {
			for (UUID player : pair.getRight()) {
				if (player.equals(entity)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isPledged(World world, UUID pledgeableUUID, UUID entity) {
		BWUniversalWorldState worldState = BWUniversalWorldState.get(world);
		for (Pair<UUID, List<UUID>> pair : worldState.pledges) {
			if (pair.getLeft().equals(pledgeableUUID)) {
				for (UUID livingUUID : pair.getRight()) {
					if (livingUUID.equals(entity)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static void pledge(World world, UUID pledgeable, UUID entity) {
		BWUniversalWorldState worldState = BWUniversalWorldState.get(world);
		List<UUID> currentPlayers = new ArrayList<>();
		for (Pair<UUID, List<UUID>> pair : worldState.pledges) {
			if (pair.getLeft().equals(pledgeable)) {
				currentPlayers = pair.getRight();
				break;
			}
		}
		currentPlayers.add(entity);
		boolean found = false;
		for (int i = 0; i < worldState.pledges.size(); i++) {
			if (worldState.pledges.get(i).getLeft().equals(pledgeable)) {
				worldState.pledges.set(i, new Pair<>(pledgeable, currentPlayers));
				found = true;
				break;
			}
		}
		if (!found) {
			worldState.pledges.add(new Pair<>(pledgeable, currentPlayers));
		}
		worldState.markDirty();
	}
	
	public static void unpledge(World world, UUID pledgeable, UUID entity) {
		BWUniversalWorldState worldState = BWUniversalWorldState.get(world);
		for (int i = worldState.pledges.size() - 1; i >= 0; i--) {
			if (worldState.pledges.get(i).getLeft().equals(pledgeable)) {
				for (int j = worldState.pledges.get(i).getRight().size() - 1; j >= 0; j--) {
					if (worldState.pledges.get(i).getRight().get(j).equals(entity)) {
						worldState.pledges.get(i).getRight().remove(j);
					}
				}
				if (worldState.pledges.get(i).getRight().isEmpty()) {
					worldState.pledges.remove(i);
				}
			}
		}
		worldState.markDirty();
	}
	
	public static int getArmorPieces(LivingEntity livingEntity, Predicate<ItemStack> predicate) {
		int amount = 0;
		for (ItemStack stack : livingEntity.getArmorItems()) {
			if (predicate.test(stack)) {
				amount++;
			}
		}
		return amount;
	}
	
	public static void addItemToInventoryAndConsume(LivingEntity entity, Hand hand, ItemStack toAdd) {
		ItemStack stack = entity.getStackInHand(hand);
		stack.decrement(entity instanceof PlayerEntity && !((PlayerEntity) entity).isCreative() ? 1 : 0);
		if (stack.isEmpty()) {
			entity.setStackInHand(hand, toAdd);
		}
		else if (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).inventory.insertStack(toAdd)) {
			entity.dropStack(toAdd);
		}
	}
	
	public static void attemptTeleport(Entity entity, BlockPos origin, int distance) {
		for (int i = 0; i < 32; i++) {
			BlockPos.Mutable mutable = new BlockPos.Mutable(origin.getX() + 0.5 + MathHelper.nextDouble(entity.world.random, -distance, distance), origin.getY() + 0.5 + MathHelper.nextDouble(entity.world.random, -distance / 2f, distance / 2f), origin.getZ() + 0.5 + MathHelper.nextDouble(entity.world.random, -distance, distance));
			if (!entity.world.getBlockState(mutable).getMaterial().isSolid()) {
				while (mutable.getY() > 0 && !entity.world.getBlockState(mutable).getMaterial().isSolid()) {
					mutable.move(Direction.DOWN);
				}
				if (entity.world.getBlockState(mutable).getMaterial().blocksMovement()) {
					teleport(entity, mutable);
					break;
				}
			}
		}
	}
	
	public static void teleport(Entity entity, BlockPos target) {
		if (!entity.isSilent()) {
			entity.world.playSound(null, entity.getBlockPos(), BWSoundEvents.ENTITY_GENERIC_TELEPORT, SoundCategory.NEUTRAL, 1, 1);
		}
		PlayerLookup.tracking(entity).forEach(playerEntity -> SpawnPortalParticlesPacket.send(playerEntity, entity));
		if (entity instanceof PlayerEntity) {
			SpawnPortalParticlesPacket.send((PlayerEntity) entity, entity);
		}
		entity.teleport(target.getX(), target.getY() + 1, target.getZ());
		if (!entity.isSilent()) {
			entity.world.playSound(null, entity.getBlockPos(), BWSoundEvents.ENTITY_GENERIC_TELEPORT, SoundCategory.NEUTRAL, 1, 1);
		}
		PlayerLookup.tracking(entity).forEach(playerEntity -> SpawnPortalParticlesPacket.send(playerEntity, entity));
		if (entity instanceof PlayerEntity) {
			SpawnPortalParticlesPacket.send((PlayerEntity) entity, entity);
		}
	}
	
	public static void registerAltarMapEntries(Block[]... altarArray) {
		for (int i = 0; i < DyeColor.values().length; i++) {
			Item carpet = null;
			switch (DyeColor.byId(i)) {
				case WHITE:
					carpet = Items.WHITE_CARPET;
					break;
				case ORANGE:
					carpet = Items.ORANGE_CARPET;
					break;
				case MAGENTA:
					carpet = Items.MAGENTA_CARPET;
					break;
				case LIGHT_BLUE:
					carpet = Items.LIGHT_BLUE_CARPET;
					break;
				case YELLOW:
					carpet = Items.YELLOW_CARPET;
					break;
				case LIME:
					carpet = Items.LIME_CARPET;
					break;
				case PINK:
					carpet = Items.PINK_CARPET;
					break;
				case GRAY:
					carpet = Items.GRAY_CARPET;
					break;
				case LIGHT_GRAY:
					carpet = Items.LIGHT_GRAY_CARPET;
					break;
				case CYAN:
					carpet = Items.CYAN_CARPET;
					break;
				case PURPLE:
					carpet = Items.PURPLE_CARPET;
					break;
				case BLUE:
					carpet = Items.BLUE_CARPET;
					break;
				case BROWN:
					carpet = Items.BROWN_CARPET;
					break;
				case GREEN:
					carpet = Items.GREEN_CARPET;
					break;
				case RED:
					carpet = Items.RED_CARPET;
					break;
				case BLACK:
					carpet = Items.BLACK_CARPET;
			}
			for (Block[] altars : altarArray) {
				ALTAR_MAP_ENTRIES.add(new AltarMapEntry(altars[0], altars[i + 1], carpet));
			}
		}
		for (Block[] altars : altarArray) {
			ALTAR_MAP_ENTRIES.add(new AltarMapEntry(altars[0], altars[17], BWObjects.HEDGEWITCH_CARPET.asItem()));
			ALTAR_MAP_ENTRIES.add(new AltarMapEntry(altars[0], altars[18], BWObjects.ALCHEMIST_CARPET.asItem()));
			ALTAR_MAP_ENTRIES.add(new AltarMapEntry(altars[0], altars[19], BWObjects.BESMIRCHED_CARPET.asItem()));
		}
	}
}
