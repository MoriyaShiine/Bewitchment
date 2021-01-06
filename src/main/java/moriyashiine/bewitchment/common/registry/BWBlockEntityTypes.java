package moriyashiine.bewitchment.common.registry;

import com.google.common.collect.Sets;
import moriyashiine.bewitchment.common.Bewitchment;
import moriyashiine.bewitchment.common.block.entity.BWChestBlockEntity;
import moriyashiine.bewitchment.common.block.entity.GlyphBlockEntity;
import moriyashiine.bewitchment.common.block.entity.WitchAltarBlockEntity;
import moriyashiine.bewitchment.common.block.entity.WitchCauldronBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BWBlockEntityTypes {
	private static final Map<BlockEntityType<?>, Identifier> BLOCK_ENTITY_TYPES = new LinkedHashMap<>();
	
	public static final BlockEntityType<BWChestBlockEntity> BW_CHEST = create("bw_chest", BlockEntityType.Builder.create(BWChestBlockEntity::new, BWObjects.JUNIPER_CHEST, BWObjects.TRAPPED_JUNIPER_CHEST, BWObjects.CYPRESS_CHEST, BWObjects.TRAPPED_CYPRESS_CHEST, BWObjects.ELDER_CHEST, BWObjects.TRAPPED_ELDER_CHEST, BWObjects.DRAGONS_BLOOD_CHEST, BWObjects.TRAPPED_DRAGONS_BLOOD_CHEST).build(null));
	public static final BlockEntityType<WitchAltarBlockEntity> WITCH_ALTAR = create("witch_altar", BlockEntityType.Builder.create(WitchAltarBlockEntity::new, merge(BWObjects.STONE_WITCH_ALTAR, BWObjects.MOSSY_COBBLESTONE_WITCH_ALTAR, BWObjects.PRISMARINE_WITCH_ALTAR, BWObjects.NETHER_BRICK_WITCH_ALTAR, BWObjects.BLACKSTONE_WITCH_ALTAR, BWObjects.GOLDEN_WITCH_ALTAR, BWObjects.END_STONE_WITCH_ALTAR, BWObjects.OBSIDIAN_WITCH_ALTAR, BWObjects.PURPUR_WITCH_ALTAR)).build(null));
	public static final BlockEntityType<WitchCauldronBlockEntity> WITCH_CAULDRON = create("witch_cauldron", BlockEntityType.Builder.create(WitchCauldronBlockEntity::new, BWObjects.WITCH_CAULDRON).build(null));
	public static final BlockEntityType<GlyphBlockEntity> GLYPH = create("glyph", BlockEntityType.Builder.create(GlyphBlockEntity::new, BWObjects.GOLDEN_GLYPH).build(null));
	
	private static <T extends BlockEntity> BlockEntityType<T> create(String name, BlockEntityType<T> type) {
		BLOCK_ENTITY_TYPES.put(type, new Identifier(Bewitchment.MODID, name));
		return type;
	}
	
	private static Block[] merge(Block[]... blockArrays) {
		Set<Block> merged = new HashSet<>();
		for (Block[] blockArray : blockArrays) {
			merged.addAll(Sets.newHashSet(blockArray));
		}
		return merged.toArray(new Block[0]);
	}
	
	public static void init() {
		BLOCK_ENTITY_TYPES.keySet().forEach(blockEntityType -> Registry.register(Registry.BLOCK_ENTITY_TYPE, BLOCK_ENTITY_TYPES.get(blockEntityType), blockEntityType));
	}
}
