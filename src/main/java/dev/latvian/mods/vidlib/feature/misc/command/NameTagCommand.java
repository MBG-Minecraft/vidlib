package dev.latvian.mods.vidlib.feature.misc.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public interface NameTagCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("name-tag", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("name", ComponentArgument.textComponent(buildContext))
			.executes(ctx -> nameTag(ctx.getSource(), ComponentArgument.getResolvedComponent(ctx, "name")))
		)
	);

	static int nameTag(CommandSourceStack source, Component name) throws CommandSyntaxException {
		var item = new ItemStack(Items.NAME_TAG);
		item.set(DataComponents.CUSTOM_NAME, name);
		ItemHandlerHelper.giveItemToPlayer(source.getPlayerOrException(), item);
		return 1;
	}
}
