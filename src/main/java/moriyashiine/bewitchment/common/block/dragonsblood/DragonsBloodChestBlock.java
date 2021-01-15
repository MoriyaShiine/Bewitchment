package moriyashiine.bewitchment.common.block.dragonsblood;

import moriyashiine.bewitchment.api.interfaces.HasSigil;
import moriyashiine.bewitchment.common.block.BWChestBlock;
import moriyashiine.bewitchment.common.block.entity.DragonsBloodChestBlockEntity;
import moriyashiine.bewitchment.common.registry.BWBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DragonsBloodChestBlock extends BWChestBlock {
	public DragonsBloodChestBlock(Settings settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, boolean trapped) {
		super(settings, supplier, trapped);
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new DragonsBloodChestBlockEntity(BWBlockEntityTypes.DRAGONS_BLOOD_CHEST, trapped);
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ActionResult result = HasSigil.onUse(world, pos, player, hand);
		if (result == ActionResult.FAIL) {
			return ActionResult.FAIL;
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
}
