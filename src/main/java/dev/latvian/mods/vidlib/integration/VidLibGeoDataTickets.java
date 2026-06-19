package dev.latvian.mods.vidlib.integration;

import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;

public interface VidLibGeoDataTickets {
	DataTicket<Float> ENTITY_ROLL = DataTicket.create("entity_roll", Float.class);
	DataTicket<Float> WIDTH = DataTicket.create("width", Float.class);
	DataTicket<Float> HEIGHT = DataTicket.create("height", Float.class);
	DataTicket<Double> CAMERA_DISTANCE = DataTicket.create("camera_distance", Double.class);
	DataTicket<Identifier> MODEL = DataTicket.create("model", Identifier.class);
	DataTicket<Identifier> TEXTURE = DataTicket.create("texture", Identifier.class);
}
