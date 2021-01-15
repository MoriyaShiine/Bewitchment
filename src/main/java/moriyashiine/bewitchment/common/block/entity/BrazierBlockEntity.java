package moriyashiine.bewitchment.common.block.entity;

import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.api.interfaces.CurseAccessor;
import moriyashiine.bewitchment.api.interfaces.UsesAltarPower;
import moriyashiine.bewitchment.api.registry.Curse;
import moriyashiine.bewitchment.client.network.packet.SpawnBrazierParticlesPacket;
import moriyashiine.bewitchment.client.network.packet.SyncBrazierBlockEntity;
import moriyashiine.bewitchment.client.network.packet.SyncClientSerializableBlockEntity;
import moriyashiine.bewitchment.common.Bewitchment;
import moriyashiine.bewitchment.common.item.TaglockItem;
import moriyashiine.bewitchment.common.recipe.CurseRecipe;
import moriyashiine.bewitchment.common.recipe.IncenseRecipe;
import moriyashiine.bewitchment.common.registry.*;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class BrazierBlockEntity extends BlockEntity implements BlockEntityClientSerializable, Tickable, Inventory, UsesAltarPower {
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
	
	private BlockPos altarPos = null;
	
	public IncenseRecipe incenseRecipe = null;
	public CurseRecipe curseRecipe = null;
	private int timer = 0;
	
	private boolean loaded = false;
	
	public BrazierBlockEntity(BlockEntityType<?> type) {
		super(type);
	}
	
	public BrazierBlockEntity() {
		this(BWBlockEntityTypes.BRAZIER);
	}
	
	@Override
	public void fromClientTag(CompoundTag tag) {
		if (tag.contains("AltarPos")) {
			setAltarPos(BlockPos.fromLong(tag.getLong("AltarPos")));
		}
		Inventories.fromTag(tag, inventory);
		timer = tag.getInt("Timer");
	}
	
	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		if (getAltarPos() != null) {
			tag.putLong("AltarPos", getAltarPos().asLong());
		}
		Inventories.toTag(tag, inventory);
		tag.putInt("Timer", timer);
		return tag;
	}
	
	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		fromClientTag(tag);
		super.fromTag(state, tag);
	}
	
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		return super.toTag(toClientTag(tag));
	}
	
	@Override
	public BlockPos getAltarPos() {
		return altarPos;
	}
	
	@Override
	public void setAltarPos(BlockPos pos) {
		this.altarPos = pos;
	}
	
	@Override
	public void tick() {
		if (world != null && !world.isClient) {
			if (!loaded) {
				markDirty();
				incenseRecipe = world.getRecipeManager().listAllOfType(BWRecipeTypes.INCENSE_RECIPE_TYPE).stream().filter(recipe -> recipe.matches(this, world)).findFirst().orElse(null);
				curseRecipe = world.getRecipeManager().listAllOfType(BWRecipeTypes.CURSE_RECIPE_TYPE).stream().filter(recipe -> recipe.matches(this, world)).findFirst().orElse(null);
				syncBrazier();
				loaded = true;
			}
			if (timer < 0) {
				timer++;
				if (world.random.nextBoolean()) {
					PlayerLookup.tracking(this).forEach(playerEntity -> SpawnBrazierParticlesPacket.send(playerEntity, this));
				}
				if (timer == 0) {
					boolean clear = incenseRecipe != null;
					if (curseRecipe != null) {
						if (altarPos != null && ((WitchAltarBlockEntity) world.getBlockEntity(altarPos)).drain(curseRecipe.cost, false)) {
							Entity target = getTarget();
							CurseAccessor curseAccessor = CurseAccessor.of(target).orElse(null);
							if (curseAccessor != null) {
								ItemStack poppet = BewitchmentAPI.getPoppet(world, BWObjects.CURSE_POPPET, target, null);
								if (!poppet.isEmpty() && !poppet.getOrCreateTag().getBoolean("Cursed")) {
									poppet.getOrCreateTag().putString("Curse", BWRegistries.CURSES.getId(curseRecipe.curse).toString());
									poppet.getOrCreateTag().putBoolean("Cursed", true);
									poppet.getOrCreateTag().remove("OwnerUUID");
									poppet.getOrCreateTag().remove("OwnerName");
								}
								else {
									curseAccessor.addCurse(new Curse.Instance(curseRecipe.curse, 168000));
								}
								world.playSound(null, pos, BWSoundEvents.ENTITY_GENERIC_CURSE, SoundCategory.BLOCKS, 1, 1);
								clear = true;
							}
							else {
								PlayerEntity closestPlayer = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, false);
								if (closestPlayer != null) {
									String entityName = "";
									for (int i = 0; i < size(); i++) {
										ItemStack stack = getStack(i);
										if (stack.getItem() instanceof TaglockItem && stack.hasTag() && stack.getOrCreateTag().contains("OwnerUUID")) {
											entityName = stack.getOrCreateTag().getString("OwnerName");
											break;
										}
									}
									world.playSound(null, pos, BWSoundEvents.BLOCK_BRAZIER_FAIL, SoundCategory.BLOCKS, 1, 1);
									closestPlayer.sendMessage(new TranslatableText(Bewitchment.MODID + ".invalid_entity", entityName), true);
								}
							}
						}
						else {
							PlayerEntity closestPlayer = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, false);
							if (closestPlayer != null) {
								world.playSound(null, pos, BWSoundEvents.BLOCK_BRAZIER_FAIL, SoundCategory.BLOCKS, 1, 1);
								closestPlayer.sendMessage(new TranslatableText(Bewitchment.MODID + ".insufficent_altar_power"), true);
							}
						}
					}
					reset(clear);
					syncBrazier();
				}
			}
		}
	}
	
	@Override
	public int size() {
		return inventory.size();
	}
	
	@Override
	public boolean isEmpty() {
		return inventory.isEmpty();
	}
	
	@Override
	public ItemStack getStack(int slot) {
		return inventory.get(slot);
	}
	
	@Override
	public ItemStack removeStack(int slot, int amount) {
		return Inventories.splitStack(inventory, slot, amount);
	}
	
	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(inventory, slot);
	}
	
	@Override
	public void setStack(int slot, ItemStack stack) {
		inventory.set(slot, stack);
	}
	
	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 16;
	}
	
	@Override
	public void clear() {
		inventory.clear();
	}
	
	public void syncBrazier() {
		if (world instanceof ServerWorld) {
			PlayerLookup.tracking(this).forEach(playerEntity -> {
				SyncClientSerializableBlockEntity.send(playerEntity, this);
				SyncBrazierBlockEntity.send(playerEntity, this);
			});
		}
	}
	
	private int getFirstEmptySlot() {
		for (int i = 0; i < size(); i++) {
			if (getStack(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}
	
	public void onUse(World world, BlockPos pos, PlayerEntity player, Hand hand) {
		if (!getCachedState().get(Properties.WATERLOGGED)) {
			ItemStack stack = player.getStackInHand(hand);
			if (getCachedState().get(Properties.LIT)) {
				world.setBlockState(pos, getCachedState().with(Properties.LIT, false));
				world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 2);
				reset(incenseRecipe != null);
				syncBrazier();
			}
			else {
				if (stack.getItem() instanceof FlintAndSteelItem) {
					world.setBlockState(pos, getCachedState().with(Properties.LIT, true));
					world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, 1);
					stack.damage(1, player, user -> user.sendToolBreakStatus(hand));
					IncenseRecipe foundIncenseRecipe = world.getRecipeManager().listAllOfType(BWRecipeTypes.INCENSE_RECIPE_TYPE).stream().filter(recipe -> recipe.matches(this, world)).findFirst().orElse(null);
					if (foundIncenseRecipe != null) {
						incenseRecipe = foundIncenseRecipe;
						timer = -6000;
						syncBrazier();
					}
					else {
						CurseRecipe foundCurseRecipe = world.getRecipeManager().listAllOfType(BWRecipeTypes.CURSE_RECIPE_TYPE).stream().filter(recipe -> recipe.matches(this, world)).findFirst().orElse(null);
						if (foundCurseRecipe != null && getTarget() != null) {
							curseRecipe = foundCurseRecipe;
							timer = -100;
							syncBrazier();
						}
					}
				}
				else if (!stack.isEmpty()) {
					int firstEmpty = getFirstEmptySlot();
					if (firstEmpty != -1) {
						setStack(firstEmpty, stack.split(1));
						syncBrazier();
					}
				}
				else {
					reset(incenseRecipe != null);
					syncBrazier();
				}
			}
		}
	}
	
	private Entity getTarget() {
		if (world != null && !world.isClient) {
			for (int i = 0; i < size(); i++) {
				Entity entity = BewitchmentAPI.getTaglockOwner(world, getStack(i));
				if (entity != null) {
					return entity;
				}
			}
		}
		return null;
	}
	
	private void reset(boolean clear) {
		if (world != null) {
			if (clear) {
				cleanInventory();
			}
			ItemScatterer.spawn(world, pos.up(getCachedState().get(Properties.HANGING) ? 0 : 1), this);
			incenseRecipe = null;
			curseRecipe = null;
			timer = 0;
		}
	}
	
	private void cleanInventory() {
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = getStack(i);
			if (stack.isDamageable()) {
				if (stack.damage(1, world.random, null) && stack.getDamage() == stack.getMaxDamage()) {
					stack.decrement(1);
				}
			}
			else {
				Item item = stack.getItem();
				setStack(i, item.hasRecipeRemainder() ? new ItemStack(item.getRecipeRemainder()) : ItemStack.EMPTY);
			}
		}
	}
}
