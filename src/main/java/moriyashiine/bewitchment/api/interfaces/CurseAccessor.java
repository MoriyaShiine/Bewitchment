package moriyashiine.bewitchment.api.interfaces;

import moriyashiine.bewitchment.api.registry.Curse;

import java.util.List;
import java.util.Optional;

public interface CurseAccessor {
	static Optional<CurseAccessor> of(Object entity) {
		if (entity instanceof CurseAccessor) {
			return Optional.of(((CurseAccessor) entity));
		}
		return Optional.empty();
	}
	
	List<Curse.Instance> getCurses();
	
	default boolean hasCurse(Curse curse) {
		return getCurses().stream().anyMatch(instance -> instance.curse == curse);
	}
	
	default void addCurse(Curse.Instance instance) {
		if (hasCurse(instance.curse)) {
			for (Curse.Instance curse : getCurses()) {
				if (curse.curse == instance.curse) {
					curse.duration = instance.duration;
					return;
				}
			}
		}
		getCurses().add(instance);
	}
	
	default void removeCurse(Curse curse) {
		if (hasCurse(curse)) {
			for (Curse.Instance instance : getCurses()) {
				if (instance.curse == curse) {
					instance.duration = 0;
				}
			}
		}
	}
}
