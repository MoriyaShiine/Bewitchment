package moriyashiine.bewitchment.client.model.entity.living;

import moriyashiine.bewitchment.common.entity.living.GhostEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class GhostEntityModel<T extends GhostEntity> extends EntityModel<T> {
	private final ModelPart rightArm;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart bodyTrail00;
	private final ModelPart leftArm;
	private final ModelPart skull;
	
	public GhostEntityModel() {
		textureWidth = 64;
		textureHeight = 64;
		rightArm = new ModelPart(this);
		rightArm.setPivot(-5.0F, 2.0F, 0.0F);
		setRotationAngle(rightArm, -1.3963F, 0.0F, 0.1F);
		rightArm.setTextureOffset(40, 16).addCuboid(-3.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F, 0.0F, true);
		
		ModelPart rArmWisp = new ModelPart(this);
		rArmWisp.setPivot(-1.0F, 2.7F, 1.7F);
		rightArm.addChild(rArmWisp);
		rArmWisp.setTextureOffset(40, 34).addCuboid(-1.5F, -4.5F, 0.1F, 3.0F, 11.0F, 4.0F, 0.0F, true);
		
		head = new ModelPart(this);
		head.setPivot(0.0F, 0.0F, 0.0F);
		head.setTextureOffset(0, 0).addCuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		
		body = new ModelPart(this);
		body.setPivot(0.0F, 0.0F, 0.0F);
		setRotationAngle(body, 0.2094F, 0.0F, 0.0F);
		body.setTextureOffset(14, 16).addCuboid(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 4.0F, 0.0F, false);
		
		bodyTrail00 = new ModelPart(this);
		bodyTrail00.setPivot(0.0F, 7.7F, 0.0F);
		body.addChild(bodyTrail00);
		setRotationAngle(bodyTrail00, 0.1047F, 0.0F, 0.0F);
		bodyTrail00.setTextureOffset(33, 51).addCuboid(-4.5F, 0.0F, -2.5F, 9.0F, 6.0F, 5.0F, 0.0F, false);
		
		ModelPart bodyTrail01 = new ModelPart(this);
		bodyTrail01.setPivot(0.0F, 5.8F, 0.1F);
		bodyTrail00.addChild(bodyTrail01);
		setRotationAngle(bodyTrail01, 0.1047F, 0.0F, 0.0F);
		bodyTrail01.setTextureOffset(0, 39).addCuboid(-5.0F, 0.0F, -3.0F, 10.0F, 9.0F, 6.0F, 0.0F, false);
		
		leftArm = new ModelPart(this);
		leftArm.setPivot(5.0F, 2.0F, 0.0F);
		setRotationAngle(leftArm, -1.3963F, 0.0F, -0.1F);
		leftArm.setTextureOffset(40, 16).addCuboid(-1.0F, -2.0F, -2.0F, 4.0F, 13.0F, 4.0F, 0.0F, true);
		
		ModelPart lArmWisp = new ModelPart(this);
		lArmWisp.setPivot(1.0F, 2.7F, 1.7F);
		leftArm.addChild(lArmWisp);
		lArmWisp.setTextureOffset(40, 34).addCuboid(-1.5F, -4.5F, 0.1F, 3.0F, 11.0F, 4.0F, 0.0F, true);
		
		skull = new ModelPart(this);
		skull.setPivot(0.0F, 0.0F, 0.0F);
		skull.setTextureOffset(34, 0).addCuboid(-3.5F, -7.5F, -3.0F, 7.0F, 5.0F, 6.0F, 0.0F, false);
		
		ModelPart skullJaw = new ModelPart(this);
		skullJaw.setPivot(0.0F, -1.9F, 0.9F);
		skull.addChild(skullJaw);
		setRotationAngle(skullJaw, 0.1745F, 0.0F, 0.0F);
		skullJaw.setTextureOffset(19, 30).addCuboid(-2.5F, -1.0F, -3.5F, 5.0F, 2.0F, 5.0F, 0.0F, false);
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		head.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		skull.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		leftArm.render(matrices, vertices, light, overlay, red, green, blue, alpha);
		rightArm.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
	
	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		head.pitch = (float) (headPitch * (Math.PI / 180f));
		head.yaw = (float) (headYaw * (Math.PI / 180f)) * 2 / 3f;
		bodyTrail00.pitch = MathHelper.sin(animationProgress / 12) / 6;
		leftArm.pitch = MathHelper.sin(animationProgress / 8) / 8;
		rightArm.pitch = MathHelper.sin((float) (animationProgress / 8 + Math.PI)) / 8;
		if (entity.getDataTracker().get(GhostEntity.HAS_TARGET)) {
			leftArm.pitch += 3.25;
			leftArm.roll = MathHelper.sin((float) (animationProgress + Math.PI)) / 2;
			rightArm.pitch += 3.25;
			rightArm.roll = MathHelper.sin(animationProgress) / 2;
		}
		else {
			leftArm.roll = -0.1f;
			rightArm.roll = 0.1f;
		}
	}
	
	private void setRotationAngle(ModelPart bone, float x, float y, float z) {
		bone.pitch = x;
		bone.yaw = y;
		bone.roll = z;
	}
	
	
}