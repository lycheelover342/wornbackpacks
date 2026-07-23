package net.wornbackpacks;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Addon for Sophisticated Backpacks that requires a backpack to be equipped
 * in the chest slot before it can be opened. Blocks both known access paths:
 *   1. Right-click while held in hand (WornBackpacksEvents)
 *   2. The "B" open-backpack keybind / hover-and-open from inventory (BackpackOpenPayloadMixin)
 *
 * Rename the package / modid (currently "wornbackpacks") to fit your pack's
 * internal addon naming if you have a convention already.
 */
@Mod(WornBackpacks.MODID)
public class WornBackpacks {
	public static final String MODID = "wornbackpacks";

	public WornBackpacks(IEventBus modEventBus) {
		// Register the plain (non-mixin) event handler on the game event bus.
		NeoForge.EVENT_BUS.register(WornBackpacksEvents.class);
	}
}
