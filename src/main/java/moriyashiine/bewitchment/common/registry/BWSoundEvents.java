package moriyashiine.bewitchment.common.registry;

import moriyashiine.bewitchment.common.Bewitchment;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class BWSoundEvents {
	private static final Map<SoundEvent, Identifier> SOUND_EVENTS = new LinkedHashMap<>();
	
	public static final SoundEvent BLOCK_GLPYH_PLACE = create("block.glyph.place");
	public static final SoundEvent ITEM_ATHAME_STRIP = create("item.athame.strip");
	public static final SoundEvent ITEM_CONTRACT_USE = create("item.contract.use");
	public static final SoundEvent ENTITY_GENERIC_TELEPORT = create("entity.generic.teleport");
	public static final SoundEvent ENTITY_GENERIC_PLING = create("entity.generic.pling");
	public static final SoundEvent ENTITY_GENERIC_SHOOT = create("entity.generic.shoot");
	public static final SoundEvent ENTITY_GENERIC_PLEDGE = create("entity.generic.pledge");
	public static final SoundEvent ENTITY_OWL_AMBIENT = create("entity.owl.ambient");
	public static final SoundEvent ENTITY_OWL_HURT = create("entity.owl.hurt");
	public static final SoundEvent ENTITY_OWL_DEATH = create("entity.owl.death");
	public static final SoundEvent ENTITY_OWL_FLY = create("entity.owl.fly");
	public static final SoundEvent ENTITY_RAVEN_AMBIENT = create("entity.raven.ambient");
	public static final SoundEvent ENTITY_RAVEN_HURT = create("entity.raven.hurt");
	public static final SoundEvent ENTITY_RAVEN_DEATH = create("entity.raven.death");
	public static final SoundEvent ENTITY_RAVEN_FLY = create("entity.raven.fly");
	public static final SoundEvent ENTITY_SNAKE_AMBIENT = create("entity.snake.ambient");
	public static final SoundEvent ENTITY_SNAKE_HURT = create("entity.snake.hurt");
	public static final SoundEvent ENTITY_SNAKE_DEATH = create("entity.snake.death");
	public static final SoundEvent ENTITY_TOAD_AMBIENT = create("entity.toad.ambient");
	public static final SoundEvent ENTITY_TOAD_HURT = create("entity.toad.hurt");
	public static final SoundEvent ENTITY_TOAD_DEATH = create("entity.toad.death");
	public static final SoundEvent ENTITY_GHOST_AMBIENT = create("entity.ghost.ambient");
	public static final SoundEvent ENTITY_GHOST_HURT = create("entity.ghost.hurt");
	public static final SoundEvent ENTITY_GHOST_DEATH = create("entity.ghost.death");
	public static final SoundEvent ENTITY_BLACK_DOG_AMBIENT = create("entity.black_dog.ambient");
	public static final SoundEvent ENTITY_BLACK_DOG_HURT = create("entity.black_dog.hurt");
	public static final SoundEvent ENTITY_BLACK_DOG_DEATH = create("entity.black_dog.death");
	public static final SoundEvent ENTITY_HELLHOUND_AMBIENT = create("entity.hellhound.ambient");
	public static final SoundEvent ENTITY_HELLHOUND_HURT = create("entity.hellhound.hurt");
	public static final SoundEvent ENTITY_HELLHOUND_DEATH = create("entity.hellhound.death");
	public static final SoundEvent ENTITY_DEMON_AMBIENT = create("entity.demon.ambient");
	public static final SoundEvent ENTITY_DEMON_HURT = create("entity.demon.hurt");
	public static final SoundEvent ENTITY_DEMON_DEATH = create("entity.demon.death");
	public static final SoundEvent ENTITY_LEONARD_AMBIENT = create("entity.leonard.ambient");
	public static final SoundEvent ENTITY_LEONARD_HURT = create("entity.leonard.hurt");
	public static final SoundEvent ENTITY_LEONARD_DEATH = create("entity.leonard.death");
	public static final SoundEvent ENTITY_BAPHOMET_AMBIENT = create("entity.baphomet.ambient");
	public static final SoundEvent ENTITY_BAPHOMET_HURT = create("entity.baphomet.hurt");
	public static final SoundEvent ENTITY_BAPHOMET_DEATH = create("entity.baphomet.death");
	
	private static SoundEvent create(String name) {
		Identifier id = new Identifier(Bewitchment.MODID, name);
		SoundEvent soundEvent = new SoundEvent(id);
		SOUND_EVENTS.put(soundEvent, id);
		return soundEvent;
	}
	
	public static void init() {
		SOUND_EVENTS.keySet().forEach(soundEvent -> Registry.register(Registry.SOUND_EVENT, SOUND_EVENTS.get(soundEvent), soundEvent));
	}
}
