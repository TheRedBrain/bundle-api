package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.predicate.item.CustomBundleContentsPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;

public class BundleAPI {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static DataComponentType<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_COMPONENT = DataComponentType.<CustomBundleContentsComponent>builder().persistent(CustomBundleContentsComponent.CODEC).networkSynchronized(CustomBundleContentsComponent.PACKET_CODEC).cacheEncoding().build();
	public static DataComponentPredicate.Type<CustomBundleContentsPredicate> CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE = new DataComponentPredicate.ConcreteType<>(CustomBundleContentsPredicate.CODEC);
	public static ContainerComponentManipulator<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER;

	public static void init() {
		LOGGER.info("Customized Bundles!");
		registerContainerComponentManipulator();
	}

	/**
	 * Publishes our manipulator to vanilla's {@link ContainerComponentManipulators#ALL_MANIPULATORS}.
	 *
	 * <p><b>Why this is not done from a static initialiser.</b> {@code BundleAPI.<clinit>} is triggered from
	 * {@code DataComponentTypesMixin}, which injects at the TAIL of {@code DataComponents.<clinit>} (that inject has to
	 * stay there - it is the only point that is guaranteed to run inside {@code BuiltInRegistries.createContents()},
	 * i.e. before {@code freeze()}, on both loaders). Touching {@code ALL_MANIPULATORS} initialises
	 * {@code ContainerComponentManipulators}, whose map is built with
	 * {@code Collectors.toMap(ContainerComponentManipulator::type, ...)} and therefore calls {@code type()} on vanilla's
	 * three manipulators, reading {@code DataComponents.CONTAINER} / {@code BUNDLE_CONTENTS} /
	 * {@code CHARGED_PROJECTILES} - re-entering a class that is still initialising. Single-threaded the JVM permits that
	 * (recursive initialisation, and the fields are already assigned at TAIL), but a second thread initialising
	 * {@code ContainerComponentManipulators} while ours sits inside {@code DataComponents.<clinit>} deadlocks on the two
	 * class-init monitors. NeoForge 26.1 loads registry elements in parallel, so that race is reachable. Doing the put
	 * from {@code init()} keeps the dependency one-directional: our thread waits for {@code DataComponents}, and nothing
	 * ever waits for us.
	 *
	 * <p><b>Ordering.</b> {@code init()} runs from each loader's mod entrypoint - {@code FabricMod#onInitialize} (Fabric,
	 * on {@code main}) and the {@code @Mod} constructor (NeoForge, on a {@code modloading-worker-*} thread). Both run
	 * after {@code BuiltInRegistries.bootStrap()} - measured on both loaders, {@code DATA_COMPONENT_TYPE} already holds
	 * its 111 vanilla entries plus ours, and {@code ITEM} its full 1506 - so {@code DataComponents} is fully initialised
	 * by then and no cycle can form. Both also run before any datapack is read. {@code ALL_MANIPULATORS} is
	 * only ever consulted lazily, from the {@code ContainerComponentManipulators.CODEC} lambda: when parsing the
	 * {@code set_contents} / {@code modify_contents} loot functions and {@code ContentsSlotSource} item-model slots. All
	 * of that happens on the first resource reload - after every mod entrypoint, after registry freeze and after creative
	 * tabs are built - so the manipulator is always in place before anything can look for it. Registry freeze itself
	 * never touches the map; only the component registration in {@code DataComponentTypesMixin} has to beat the freeze,
	 * and that one stays where it is.
	 */
	private static void registerContainerComponentManipulator() {
		// Vanilla's ALL_MANIPULATORS is a plain HashMap (Collectors.toMap), and the field is a final interface constant,
		// so it cannot be swapped for a ConcurrentHashMap. Fabric calls init() from the single-threaded ModInitializer
		// dispatch, but NeoForge calls it from a parallel modloading-worker thread, so a single-threaded init point is
		// NOT guaranteed here: we take the map's own monitor, the only lock concurrent writers could ever agree on.
		synchronized (ContainerComponentManipulators.ALL_MANIPULATORS) {
			ContainerComponentManipulators.ALL_MANIPULATORS.put(CUSTOM_BUNDLE_CONTENTS_COMPONENT, CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER);
		}
	}

	static {
		CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER = new ContainerComponentManipulator<CustomBundleContentsComponent>() {
			@Override
			public DataComponentType<CustomBundleContentsComponent> type() {
				return CUSTOM_BUNDLE_CONTENTS_COMPONENT;
			}

			@Override
			public CustomBundleContentsComponent empty() {
				return CustomBundleContentsComponent.DEFAULT;
			}

			@Override
			public Stream<ItemStack> getContents(CustomBundleContentsComponent customBundleContentsComponent) {
				return customBundleContentsComponent.stream();
			}

			@Override
			public CustomBundleContentsComponent setContents(CustomBundleContentsComponent customBundleContentsComponent, Stream<ItemStack> stream) {
				CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear();
				stream.forEach(builder::add);
				return builder.build();
			}
		};
		// NOTE: the ALL_MANIPULATORS registration deliberately does NOT happen here - see
		// registerContainerComponentManipulator(). This static block is reached from DataComponents.<clinit> and must not
		// initialise ContainerComponentManipulators.
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}