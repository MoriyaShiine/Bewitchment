package moriyashiine.bewitchment.common.block.dragonsblood;

import com.terraformersmc.terraform.wood.block.TerraformPressurePlateBlock;
import moriyashiine.bewitchment.common.block.entity.SigilBlockEntity;
import moriyashiine.bewitchment.common.block.entity.interfaces.SigilHolder;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DragonsBloodPressurePlateBlock extends TerraformPressurePlateBlock implements BlockEntityProvider {
	public DragonsBloodPressurePlateBlock(Settings settings) {
		super(settings);
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SigilBlockEntity(pos, state);
	}
	
	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity instanceof LivingEntity) {
			SigilHolder.onUse(world, pos, (LivingEntity) entity, Hand.MAIN_HAND);
		}
		super.onEntityCollision(state, world, pos, entity);
	}
}
