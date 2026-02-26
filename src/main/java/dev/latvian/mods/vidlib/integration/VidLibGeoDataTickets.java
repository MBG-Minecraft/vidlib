package dev.latvian.mods.vidlib.integration;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.dataticket.DataTicket;

public interface VidLibGeoDataTickets {
	DataTicket<Float> ENTITY_ROLL = DataTicket.create("entity_roll", Float.class);
	DataTicket<Float> WIDTH = DataTicket.create("width", Float.class);
	DataTicket<Float> HEIGHT = DataTicket.create("height", Float.class);
	DataTicket<Double> CAMERA_DISTANCE = DataTicket.create("camera_distance", Double.class);
	DataTicket<ResourceLocation> MODEL = DataTicket.create("model", ResourceLocation.class);
	DataTicket<ResourceLocation> TEXTURE = DataTicket.create("texture", ResourceLocation.class);
}
