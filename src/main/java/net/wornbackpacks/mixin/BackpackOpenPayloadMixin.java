package net.wornbackpacks.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.IContextAwareContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenPayload;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restricts the "open backpack" keybind (B by default) / hover-and-open-from-
 * inventory path to only ever open the backpack currently worn in the chest
 * slot. Mirrors the branch structure of BackpackOpenPayload#handlePayload:
 *
 *  - handlerName == "armor"                -> allowed (the worn backpack)
 *  - handlerName == "main"/"offhand"        -> blocked (not worn)
 *  - handlerName empty, slotIndex == -1      -> allowed (closing a sub-backpack
 *                                               back up to its parent - the
 *                                               parent was already validated
 *                                               when it was first opened)
 *  - handlerName empty, already in a backpack UI -> allowed (opening a nested
 *                                               backpack inside one that's
 *                                               already validly open)
 *  - handlerName empty, no backpack UI open  -> this is the "find first
 *                                               backpack anywhere" fallback;
 *                                               replaced entirely so it only
 *                                               ever finds the worn one.
 */
@Mixin(BackpackOpenPayload.class)
public class BackpackOpenPayloadMixin {

	@Inject(method = "handlePayload", at = @At("HEAD"), cancellable = true)
	private static void wornBackpacks$restrictToWorn(BackpackOpenPayload payload, IPayloadContext context, CallbackInfo ci) {
		Player player = context.player();
		if (player == null) {
			return;
		}

		String handlerName = payload.handlerName();

		if (!handlerName.isEmpty()) {
			if (!handlerName.equals(PlayerInventoryProvider.ARMOR_INVENTORY)) {
				sendDeniedMessage(player);
				ci.cancel();
			}
			return;
		}

		// handlerName is empty from here on.
		if (payload.slotIndex() == -1) {
			return; // navigating up to a parent backpack context - fine
		}
		if (player.containerMenu instanceof BackpackContainer) {
			return; // opening a nested backpack inside an already-open one - fine
		}
		if (player.containerMenu instanceof IContextAwareContainer) {
			return; // already in a validated backpack-aware context - fine
		}

		// This is the "no backpack context, find any backpack" fallback path
		// (B pressed with no relevant GUI open). Take it over completely so
		// it only ever opens the worn (chest slot) backpack.
		ci.cancel();

		ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chestStack.getItem() instanceof BackpackItem)) {
			return; // nothing worn - do nothing, no message
		}

		BackpackContext.Item backpackContext = new BackpackContext.Item(PlayerInventoryProvider.ARMOR_INVENTORY, 0);
		player.openMenu(
				new SophisticatedMenuProvider((w, p, pl) -> new BackpackContainer(w, pl, backpackContext),
						backpackContext.getDisplayName(player), false),
				backpackContext::toBuffer);
	}

	private static void sendDeniedMessage(Player player) {
		player.displayClientMessage(Component.translatable("message.wornbackpacks.must_be_worn"), true);
	}
}
