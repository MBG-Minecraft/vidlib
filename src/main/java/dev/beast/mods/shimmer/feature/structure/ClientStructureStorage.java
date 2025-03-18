package dev.beast.mods.shimmer.feature.structure;

import dev.beast.mods.shimmer.util.Side;

public class ClientStructureStorage extends StructureStorage {
	public static final ClientStructureStorage CLIENT = new ClientStructureStorage();

	public ClientStructureStorage() {
		super(Side.CLIENT);
	}
}
