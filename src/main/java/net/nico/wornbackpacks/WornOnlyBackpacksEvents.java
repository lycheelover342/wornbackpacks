package net.nico.wornbackpacks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

public class WornOnlyBackpacksEvents {

	private WornOnlyBackpacksEvents() {
	}

	/**
	 * Blocks BackpackItem#use() (right-click-to-open while held in hand) by
	 * canceling the interaction before it ever reaches the item.
	 * Placing a backpack in the world (shift-right-click on a block) goes
	 * through BackpackItem#useOn() instead and is untouched by this.
	 */
	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof BackpackItem) {
			event.setCanceled(true);
			if (!event.getLevel().isClientSide) {
				event.getEntity().displayClientMessage(
						Component.translatable("message.wornbackpacks.must_be_worn"), true);
			}
		}
	}
}
