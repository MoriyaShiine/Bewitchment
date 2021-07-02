package moriyashiine.bewitchment.client;

import com.terraformersmc.terraform.sign.SpriteIdentifierRegistry;
import moriyashiine.bewitchment.api.entity.BroomEntity;
import moriyashiine.bewitchment.client.misc.SpriteIdentifiers;
import moriyashiine.bewitchment.client.network.packet.*;
import moriyashiine.bewitchment.client.particle.CauldronBubbleParticle;
import moriyashiine.bewitchment.client.particle.IncenseSmokeParticle;
import moriyashiine.bewitchment.client.renderer.blockentity.BrazierBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.blockentity.PoppetShelfBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.blockentity.WitchAltarBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.blockentity.WitchCauldronBlockEntityRenderer;
import moriyashiine.bewitchment.client.renderer.entity.*;
import moriyashiine.bewitchment.client.renderer.entity.living.*;
import moriyashiine.bewitchment.client.screen.DemonScreen;
import moriyashiine.bewitchment.client.screen.DemonScreenHandler;
import moriyashiine.bewitchment.common.Bewitchment;
import moriyashiine.bewitchment.common.block.entity.BWChestBlockEntity;
import moriyashiine.bewitchment.common.entity.interfaces.BroomUserAccessor;
import moriyashiine.bewitchment.common.entity.living.DemonEntity;
import moriyashiine.bewitchment.common.item.TaglockItem;
import moriyashiine.bewitchment.common.network.packet.TogglePressingForwardPacket;
import moriyashiine.bewitchment.common.network.packet.TransformationAbilityPacket;
import moriyashiine.bewitchment.common.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Environment(EnvType.CLIENT)
public class BewitchmentClient implements ClientModInitializer {
	public static final KeyBinding TRANSFORMATION_ABILITY = KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + Bewitchment.MODID + ".transformation_ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "itemGroup." + Bewitchment.MODID + "." + Bewitchment.MODID));
	
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncContractsPacket.ID, SyncContractsPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(SyncDemonTradesPacket.ID, (client, network, buf, sender) -> {
			int syncId = buf.readInt();
			List<DemonEntity.DemonTradeOffer> offers = DemonEntity.DemonTradeOffer.fromPacket(buf);
			int traderId = buf.readInt();
			boolean discount = buf.readBoolean();
			client.execute(() -> {
				if (client.player != null) {
					ScreenHandler screenHandler = client.player.currentScreenHandler;
					if (syncId == screenHandler.syncId && screenHandler instanceof DemonScreenHandler) {
						((DemonScreenHandler) screenHandler).demonMerchant.setCurrentCustomer(client.player);
						((DemonScreenHandler) screenHandler).demonMerchant.setOffersClientside(offers);
						((DemonScreenHandler) screenHandler).demonMerchant.setDemonTraderClientside((LivingEntity) client.world.getEntityById(traderId));
						((DemonScreenHandler) screenHandler).demonMerchant.setDiscountClientside(discount);
					}
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(SyncHornedSpearEntity.ID, SyncHornedSpearEntity::handle);
		ClientPlayNetworking.registerGlobalReceiver(SyncClientSerializableBlockEntity.ID, SyncClientSerializableBlockEntity::handle);
		ClientPlayNetworking.registerGlobalReceiver(SyncWitchAltarBlockEntity.ID, SyncWitchAltarBlockEntity::handle);
		ClientPlayNetworking.registerGlobalReceiver(SyncBrazierBlockEntity.ID, SyncBrazierBlockEntity::handle);
		ClientPlayNetworking.registerGlobalReceiver(SyncPoppetShelfBlockEntity.ID, SyncPoppetShelfBlockEntity::handle);
		ClientPlayNetworking.registerGlobalReceiver(SyncTaglockHolderBlockEntity.ID, SyncTaglockHolderBlockEntity::handle);
		ClientPlayNetworking.registerGlobalReceiver(SpawnSmokeParticlesPacket.ID, SpawnSmokeParticlesPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(SpawnPortalParticlesPacket.ID, SpawnPortalParticlesPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(SpawnExplosionParticlesPacket.ID, SpawnExplosionParticlesPacket::handle);
		ParticleFactoryRegistry.getInstance().register(BWParticleTypes.CAULDRON_BUBBLE, CauldronBubbleParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(BWParticleTypes.INCENSE_SMOKE, IncenseSmokeParticle.Factory::new);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0xffff00, BWObjects.GOLDEN_GLYPH);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0xc00000, BWObjects.FIERY_GLYPH);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0x8000a0, BWObjects.ELDRITCH_GLYPH);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> tintIndex == 1 ? ((BedBlock) state.getBlock()).getColor().getFireworkColor() : 0xffffff, BWObjects.WHITE_COFFIN, BWObjects.ORANGE_COFFIN, BWObjects.MAGENTA_COFFIN, BWObjects.LIGHT_BLUE_COFFIN, BWObjects.YELLOW_COFFIN, BWObjects.LIME_COFFIN, BWObjects.PINK_COFFIN, BWObjects.GRAY_COFFIN, BWObjects.LIGHT_GRAY_COFFIN, BWObjects.CYAN_COFFIN, BWObjects.PURPLE_COFFIN, BWObjects.BLUE_COFFIN, BWObjects.BROWN_COFFIN, BWObjects.GREEN_COFFIN, BWObjects.RED_COFFIN, BWObjects.BLACK_COFFIN);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 1 ? ((BedBlock) Block.getBlockFromItem(stack.getItem())).getColor().getFireworkColor() : 0xffffff, BWObjects.WHITE_COFFIN, BWObjects.ORANGE_COFFIN, BWObjects.MAGENTA_COFFIN, BWObjects.LIGHT_BLUE_COFFIN, BWObjects.YELLOW_COFFIN, BWObjects.LIME_COFFIN, BWObjects.PINK_COFFIN, BWObjects.GRAY_COFFIN, BWObjects.LIGHT_GRAY_COFFIN, BWObjects.CYAN_COFFIN, BWObjects.PURPLE_COFFIN, BWObjects.BLUE_COFFIN, BWObjects.BROWN_COFFIN, BWObjects.GREEN_COFFIN, BWObjects.RED_COFFIN, BWObjects.BLACK_COFFIN);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? 0x7f0000 : 0xffffff, BWObjects.BOTTLE_OF_BLOOD);
		FabricModelPredicateProviderRegistry.register(BWObjects.HEDGEWITCH_HAT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> stack.getName().asString().toLowerCase().contains("faith") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.ALCHEMIST_HAT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> stack.getName().asString().toLowerCase().contains("faith") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.BESMIRCHED_HAT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> stack.getName().asString().toLowerCase().contains("faith") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.NAZAR, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> stack.hasTag() && stack.getTag().getBoolean("Worn") ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.PRICKLY_BELT, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> stack.hasTag() && stack.getTag().getInt("PotionUses") > 0 ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.HORNED_SPEAR, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.TAGLOCK, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> TaglockItem.hasTaglock(stack) ? 1 : 0);
		FabricModelPredicateProviderRegistry.register(BWObjects.WAYSTONE, new Identifier(Bewitchment.MODID, "variant"), (stack, world, entity, seed) -> stack.hasTag() && stack.getOrCreateTag().contains("LocationPos") ? 1 : 0);
		//		ArmorRenderingRegistry.registerModel((livingEntity, itemStack, equipmentSlot, bipedEntityModel) -> new WitchArmorModel<>(equipmentSlot, itemStack.getItem() == BWObjects.HEDGEWITCH_HOOD || itemStack.getItem() == BWObjects.ALCHEMIST_HOOD || itemStack.getItem() == BWObjects.BESMIRCHED_HOOD, !livingEntity.getEquippedStack(EquipmentSlot.FEET).isEmpty()), BWObjects.HEDGEWITCH_HOOD, BWObjects.HEDGEWITCH_HAT, BWObjects.HEDGEWITCH_ROBES, BWObjects.HEDGEWITCH_PANTS, BWObjects.ALCHEMIST_HOOD, BWObjects.ALCHEMIST_HAT, BWObjects.ALCHEMIST_ROBES, BWObjects.ALCHEMIST_PANTS, BWObjects.BESMIRCHED_HOOD, BWObjects.BESMIRCHED_HAT, BWObjects.BESMIRCHED_ROBES, BWObjects.BESMIRCHED_PANTS, BWObjects.HARBINGER);
		//		Identifier WITCH_HAT_VARIANT = new Identifier(Bewitchment.MODID, "textures/entity/armor/witch_hat_variant.png");
		//		ArmorRenderingRegistry.registerTexture((livingEntity, itemStack, equipmentSlot, b, s, identifier) -> itemStack.getItem() == BWObjects.HEDGEWITCH_HAT && itemStack.getName().asString().toLowerCase().contains("faith") ? WITCH_HAT_VARIANT : new Identifier(Bewitchment.MODID, "textures/entity/armor/hedgewitch.png"), BWObjects.HEDGEWITCH_HOOD, BWObjects.HEDGEWITCH_HAT, BWObjects.HEDGEWITCH_ROBES, BWObjects.HEDGEWITCH_PANTS);
		//		ArmorRenderingRegistry.registerTexture((livingEntity, itemStack, equipmentSlot, b, s, identifier) -> itemStack.getItem() == BWObjects.ALCHEMIST_HAT && itemStack.getName().asString().toLowerCase().contains("faith") ? WITCH_HAT_VARIANT : new Identifier(Bewitchment.MODID, "textures/entity/armor/alchemist.png"), BWObjects.ALCHEMIST_HOOD, BWObjects.ALCHEMIST_HAT, BWObjects.ALCHEMIST_ROBES, BWObjects.ALCHEMIST_PANTS);
		//		ArmorRenderingRegistry.registerTexture((livingEntity, itemStack, equipmentSlot, b, s, identifier) -> itemStack.getItem() == BWObjects.BESMIRCHED_HAT && itemStack.getName().asString().toLowerCase().contains("faith") ? WITCH_HAT_VARIANT : new Identifier(Bewitchment.MODID, "textures/entity/armor/besmirched.png"), BWObjects.BESMIRCHED_HOOD, BWObjects.BESMIRCHED_HAT, BWObjects.BESMIRCHED_ROBES, BWObjects.BESMIRCHED_PANTS);
		//		ArmorRenderingRegistry.registerTexture((livingEntity, itemStack, equipmentSlot, b, s, identifier) -> new Identifier(Bewitchment.MODID, "textures/entity/armor/harbinger.png"), BWObjects.HARBINGER);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.BW_CHEST, ChestBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.WITCH_ALTAR, ctx -> new WitchAltarBlockEntityRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.WITCH_CAULDRON, ctx -> new WitchCauldronBlockEntityRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.BRAZIER, ctx -> new BrazierBlockEntityRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.POPPET_SHELF, ctx -> new PoppetShelfBlockEntityRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.JUNIPER_CHEST, ChestBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.ELDER_CHEST, ChestBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BWBlockEntityTypes.DRAGONS_BLOOD_CHEST, ChestBlockEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.JUNIPER_BOAT, BoatEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.CYPRESS_BOAT, BoatEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.ELDER_BOAT, BoatEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.DRAGONS_BLOOD_BOAT, BoatEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.JUNIPER_BROOM, JuniperBroomEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.CYPRESS_BROOM, CypressBroomEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.ELDER_BROOM, ElderBroomEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.DRAGONS_BLOOD_BROOM, DragonsBloodBroomEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.SILVER_ARROW, SilverArrowEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.HORNED_SPEAR, HornedSpearEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.OWL, OwlEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.RAVEN, RavenEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.SNAKE, SnakeEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.TOAD, ToadEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.GHOST, GhostEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.VAMPIRE, VampireEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.WEREWOLF, WerewolfEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.HELLHOUND, HellhoundEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.DEMON, DemonEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.LEONARD, LeonardEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.BAPHOMET, BaphometEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.LILITH, LilithEntityRenderer::new);
		EntityRendererRegistry.INSTANCE.register(BWEntityTypes.HERNE, HerneEntityRenderer::new);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.SALT_LINE, BWObjects.TEMPORARY_COBWEB, BWObjects.GLYPH, BWObjects.GOLDEN_GLYPH, BWObjects.FIERY_GLYPH, BWObjects.ELDRITCH_GLYPH, BWObjects.SIGIL);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.ACONITE_CROP, BWObjects.BELLADONNA_CROP, BWObjects.GARLIC_CROP, BWObjects.MANDRAKE_CROP);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.JUNIPER_SAPLING, BWObjects.POTTED_JUNIPER_SAPLING, BWObjects.JUNIPER_DOOR, BWObjects.JUNIPER_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.CYPRESS_SAPLING, BWObjects.POTTED_CYPRESS_SAPLING, BWObjects.CYPRESS_DOOR, BWObjects.CYPRESS_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.ELDER_SAPLING, BWObjects.POTTED_ELDER_SAPLING, BWObjects.ELDER_DOOR, BWObjects.ELDER_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.DRAGONS_BLOOD_SAPLING, BWObjects.POTTED_DRAGONS_BLOOD_SAPLING, BWObjects.DRAGONS_BLOOD_DOOR, BWObjects.DRAGONS_BLOOD_TRAPDOOR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.GLOWING_BRAMBLE, BWObjects.ENDER_BRAMBLE, BWObjects.FRUITING_BRAMBLE, BWObjects.SCORCHED_BRAMBLE, BWObjects.THICK_BRAMBLE, BWObjects.FLEETING_BRAMBLE);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.STONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.MOSSY_COBBLESTONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.PRISMARINE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.NETHER_BRICK_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.BLACKSTONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.GOLDEN_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.END_STONE_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.OBSIDIAN_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.PURPUR_WITCH_ALTAR);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), BWObjects.CRYSTAL_BALL);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BWObjects.BRAZIER);
		ScreenRegistry.register(BWScreenHandlers.DEMON_SCREEN_HANDLER, DemonScreen::new);
		ScreenRegistry.register(BWScreenHandlers.BAPHOMET_SCREEN_HANDLER, DemonScreen::new);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.JUNIPER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_JUNIPER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.JUNIPER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_JUNIPER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.JUNIPER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_JUNIPER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.CYPRESS);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_CYPRESS);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.CYPRESS_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_CYPRESS_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.CYPRESS_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_CYPRESS_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.ELDER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_ELDER);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.ELDER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_ELDER_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.ELDER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_ELDER_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.DRAGONS_BLOOD);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_DRAGONS_BLOOD);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.DRAGONS_BLOOD_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_DRAGONS_BLOOD_LEFT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.DRAGONS_BLOOD_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(SpriteIdentifiers.TRAPPED_DRAGONS_BLOOD_RIGHT);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.JUNIPER_SIGN.getTexture()));
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.CYPRESS_SIGN.getTexture()));
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.ELDER_SIGN.getTexture()));
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, BWObjects.DRAGONS_BLOOD_SIGN.getTexture()));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.JUNIPER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.JUNIPER_CHEST.getDefaultState(), BWChestBlockEntity.Type.JUNIPER, false), matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_JUNIPER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.TRAPPED_JUNIPER_CHEST.getDefaultState(), BWChestBlockEntity.Type.JUNIPER, true), matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.CYPRESS_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.CYPRESS_CHEST.getDefaultState(), BWChestBlockEntity.Type.CYPRESS, false), matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_CYPRESS_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.TRAPPED_CYPRESS_CHEST.getDefaultState(), BWChestBlockEntity.Type.CYPRESS, true), matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.ELDER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.ELDER_CHEST.getDefaultState(), BWChestBlockEntity.Type.ELDER, false), matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_ELDER_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.TRAPPED_ELDER_CHEST.getDefaultState(), BWChestBlockEntity.Type.ELDER, true), matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.DRAGONS_BLOOD_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.DRAGONS_BLOOD_CHEST.getDefaultState(), BWChestBlockEntity.Type.DRAGONS_BLOOD, false), matrices, vertexConsumers, light, overlay));
		BuiltinItemRendererRegistry.INSTANCE.register(BWObjects.TRAPPED_DRAGONS_BLOOD_CHEST, (stack, mode, matrices, vertexConsumers, light, overlay) -> MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(new BWChestBlockEntity(BWBlockEntityTypes.BW_CHEST, BlockPos.ORIGIN, BWObjects.TRAPPED_DRAGONS_BLOOD_CHEST.getDefaultState(), BWChestBlockEntity.Type.DRAGONS_BLOOD, true), matrices, vertexConsumers, light, overlay));
		//		TrinketRendererRegistry.registerRenderer(BWObjects.NAZAR, (stack, slotReference, contextModel, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch) -> {
		//			ItemStack copy = stack.copy();
		//			copy.getOrCreateTag().putBoolean("Worn", true);
		//			TrinketRenderer.translateToChest(matrices, (PlayerEntityModel<AbstractClientPlayerEntity>) contextModel, (AbstractClientPlayerEntity) entity);
		//			matrices.translate(0, -1 / 4.25f, 1 / 48f);
		//			matrices.scale(1 / 3f, 1 / 3f, 1 / 3f);
		//			matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));
		//			MinecraftClient.getInstance().getItemRenderer().renderItem(copy, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);
		//		});
		//		TrinketRendererRegistry.registerRenderer(BWObjects.SPECTER_BANGLE, new TrinketRenderer() {
		//			private static final Identifier TEXTURE = new Identifier(Bewitchment.MODID, "textures/entity/trinket/specter_bangle.png");
		//			private static final Model MODEL = new SpecterBangleModel();
		//
		//			@Override
		//			public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		//				TrinketRenderer.translateToRightLeg(matrices, (PlayerEntityModel<AbstractClientPlayerEntity>) contextModel, (AbstractClientPlayerEntity) entity);
		//				MODEL.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		//			}
		//		});
		//		TrinketRendererRegistry.registerRenderer(BWObjects.PRICKLY_BELT, new TrinketRenderer() {
		//			private static final Identifier TEXTURE = new Identifier(Bewitchment.MODID, "textures/entity/trinket/prickly_belt.png");
		//			private static final Model MODEL = new PricklyBeltModel();
		//
		//			@Override
		//			public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		//				TrinketRenderer.translateToChest(matrices, (PlayerEntityModel<AbstractClientPlayerEntity>) contextModel, (AbstractClientPlayerEntity) entity);
		//				MODEL.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		//			}
		//		});
		//		TrinketRendererRegistry.registerRenderer(BWObjects.HELLISH_BAUBLE, (stack, slotReference, contextModel, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch) -> {
		//			TrinketRenderer.translateToChest(matrices, (PlayerEntityModel<AbstractClientPlayerEntity>) contextModel, (AbstractClientPlayerEntity) entity);
		//			matrices.translate(0, -1 / 4.25f, 1 / 48f);
		//			matrices.scale(1 / 3f, 1 / 3f, 1 / 3f);
		//			matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));
		//			MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);
		//		});
		//		TrinketRendererRegistry.registerRenderer(BWObjects.DRUID_BAND, new TrinketRenderer() {
		//			private static final Identifier TEXTURE = new Identifier(Bewitchment.MODID, "textures/entity/trinket/druid_band.png");
		//			private static final Model MODEL = new DruidBandModel();
		//
		//			@Override
		//			public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		//				TrinketRenderer.translateToLeftLeg(matrices, (PlayerEntityModel<AbstractClientPlayerEntity>) contextModel, (AbstractClientPlayerEntity) entity);
		//				MODEL.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		//			}
		//		});
		//		TrinketRendererRegistry.registerRenderer(BWObjects.ZEPHYR_HARNESS, new TrinketRenderer() {
		//			private static final Identifier TEXTURE = new Identifier(Bewitchment.MODID, "textures/entity/trinket/zephyr_harness.png");
		//			private static final Model MODEL = new ZephyrHarnessModel();
		//
		//			@Override
		//			public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		//				TrinketRenderer.translateToChest(matrices, (PlayerEntityModel<AbstractClientPlayerEntity>) contextModel, (AbstractClientPlayerEntity) entity);
		//				MODEL.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
		//			}
		//		});
		ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
			private int transformationAbilityCooldown = 0;
			
			@Override
			public void onEndTick(MinecraftClient minecraftClient) {
				if (minecraftClient.player != null) {
					if (transformationAbilityCooldown > 0) {
						transformationAbilityCooldown--;
					}
					else if (BewitchmentClient.TRANSFORMATION_ABILITY.isPressed()) {
						transformationAbilityCooldown = 20;
						TransformationAbilityPacket.send();
					}
					if (((BroomUserAccessor) minecraftClient.player).getPressingForward()) {
						TogglePressingForwardPacket.send(false);
					}
					if (MinecraftClient.getInstance().options.keyForward.isPressed() && minecraftClient.player.getVehicle() instanceof BroomEntity) {
						TogglePressingForwardPacket.send(true);
					}
				}
			}
		});
	}
}
