package moriyashiine.bewitchment.common.integration.requiem;

public class BWRequiemCompat {
	public static void init() {
		//		RequiemApi.registerPlugin(new RequiemPlugin() {
		//			@Override
		//			public void onRequiemInitialize() {
		//				PossessionStateChangeCallback.EVENT.register((player, possessed) -> ((RequiemCompatAccessor) player).setWeakToSilverFromRequiem(possessed != null && BewitchmentAPI.isWeakToSilver(possessed)));
		//				RemnantStateChangeCallback.EVENT.register((player, state) -> {
		//					((TransformationAccessor) player).setAlternateForm(false);
		//					if (state.isVagrant()) {
		//						((RequiemCompatAccessor) player).setCachedTransformationForRequiem(((TransformationAccessor) player).getTransformation());
		//						((TransformationAccessor) player).setTransformation(BWTransformations.HUMAN);
		//					}
		//					else {
		//						((TransformationAccessor) player).setTransformation(((RequiemCompatAccessor) player).getCachedTransformationForRequiem());
		//						((RequiemCompatAccessor) player).setCachedTransformationForRequiem(BWTransformations.HUMAN);
		//					}
		//				});
		//			}
		//		});
	}
}
