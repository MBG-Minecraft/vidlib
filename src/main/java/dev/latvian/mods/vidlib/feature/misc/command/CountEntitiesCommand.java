package dev.latvian.mods.vidlib.feature.misc.command;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.commons.lang3.mutable.MutableInt;

public interface CountEntitiesCommand {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("count-entities", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.argument("filter", EntityArgument.entities())
			.executes(ctx -> countEntities(ctx.getSource(), EntityArgument.getEntities(ctx, "filter")))
		)
		.executes(ctx -> countEntities(ctx.getSource(), ctx.getSource().getLevel().getAllEntities()))
	);

	private static int countEntities(CommandSourceStack source, Iterable<? extends Entity> entities) {
		int totalCount = 0;
		var count = new Reference2ObjectArrayMap<EntityType<?>, MutableInt>();

		for (var entity : entities) {
			totalCount++;
			count.computeIfAbsent(entity.getType(), t -> new MutableInt(0)).add(1);
		}

		for (var entry : count.entrySet().stream().sorted((o1, o2) -> Integer.compare(o2.getValue().intValue(), o1.getValue().intValue())).toList()) {
			source.tell(Component.empty().append(entry.getKey().getDescription()).append(": %,d".formatted(entry.getValue().intValue())));
		}

		source.tell("Total: " + totalCount);
		return totalCount;
	}
}
