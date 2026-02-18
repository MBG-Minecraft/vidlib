package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public interface InvSeeCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("invsee", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("player", EntityArgument.player())
			.executes(ctx -> invsee(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))
		)
	);

	private static int invsee(ServerPlayer self, ServerPlayer player) {
		self.openMenu(new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return player.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
				return ChestMenu.sixRows(containerId, playerInventory, new Container() {
					@Override
					public int getContainerSize() {
						return 9 * 6;
					}

					@Override
					public boolean isEmpty() {
						return false;
					}

					@Override
					public ItemStack getItem(int slot) {
						if (slot == 0) {
							return player.getItemBySlot(EquipmentSlot.HEAD);
						} else if (slot == 1) {
							return player.getItemBySlot(EquipmentSlot.CHEST);
						} else if (slot == 2) {
							return player.getItemBySlot(EquipmentSlot.LEGS);
						} else if (slot == 3) {
							return player.getItemBySlot(EquipmentSlot.FEET);
						} else if (slot == 8) {
							return player.getItemBySlot(EquipmentSlot.OFFHAND);
						} else if (slot >= 9 && slot < 36) {
							return player.getInventory().getItem(slot);
						} else if (slot >= 45 && slot < 54) {
							return player.getInventory().getItem(slot - 45);
						} else {
							return ItemStack.EMPTY;
						}
					}

					@Override
					public ItemStack removeItem(int slot, int amount) {
						if (amount > 0) {
							var stack = getItem(slot);

							if (!stack.isEmpty()) {
								return stack.split(amount);
							}
						}

						return ItemStack.EMPTY;
					}

					@Override
					public ItemStack removeItemNoUpdate(int slot) {
						return ItemStack.EMPTY;
					}

					@Override
					public void setItem(int slot, ItemStack stack) {
						if (slot == 0) {
							player.setItemSlot(EquipmentSlot.HEAD, stack);
						} else if (slot == 1) {
							player.setItemSlot(EquipmentSlot.CHEST, stack);
						} else if (slot == 2) {
							player.setItemSlot(EquipmentSlot.LEGS, stack);
						} else if (slot == 3) {
							player.setItemSlot(EquipmentSlot.FEET, stack);
						} else if (slot == 8) {
							player.setItemSlot(EquipmentSlot.OFFHAND, stack);
						} else if (slot >= 9 && slot < 36) {
							player.getInventory().setItem(slot, stack);
						} else if (slot >= 45 && slot < 54) {
							player.getInventory().setItem(slot - 45, stack);
						} else {
							ItemHandlerHelper.giveItemToPlayer(player, stack);
							return;
						}

						setChanged();
					}

					@Override
					public void setChanged() {
						player.getInventory().setChanged();
					}

					@Override
					public boolean stillValid(Player player) {
						return true;
					}

					@Override
					public void clearContent() {
					}
				});
			}
		});

		return 1;
	}
}
