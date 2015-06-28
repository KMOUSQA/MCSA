package kmou.selab.mcsp.util;

import org.vertx.java.core.json.JsonObject;

import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.net.MessageHeader;

public class MaritimeJsonFactory {

	public static JsonObject toJsonObject(String endpointType, MaritimeId target, MessageHeader header, String jsonParam){
		JsonObject json = new JsonObject();
		
		json.putNumber("receiverId", Integer.parseInt(target.toString().substring(5)));
		json.putNumber("senderId", Integer.parseInt(header.getSender().toString().substring(5)));
		json.putString("endpointType", endpointType);
		json.putObject("parameters", new JsonObject(jsonParam));
		
		return json;
	}
	public static JsonObject toJsonObject(String endpointType, MaritimeId target, MessageHeader header, JsonObject jsonParam){
		JsonObject json = new JsonObject();
		
		json.putNumber("receiverId", Integer.parseInt(target.toString().substring(5)));
		json.putNumber("senderId", Integer.parseInt(header.getSender().toString().substring(5)));
		json.putString("endpointType", endpointType);
		json.putObject("parameters", jsonParam);
		
		return json;
	} 
	
	public static JsonObject toJsonObject(String endpointType, MaritimeId sender, MaritimeId target, String param){
		JsonObject json = new JsonObject();
		
		json.putNumber("receiverId", Integer.parseInt(target.toString().substring(5)));
		json.putNumber("senderId", Integer.parseInt(sender.toString().substring(5)));
		json.putString("endpointType", endpointType);
		json.putObject("parameters", new JsonObject(param));
		
		return json;
	}
	
	public static JsonObject toJsonObject(String endpointType, MaritimeId sender, MaritimeId target, JsonObject param){
		JsonObject json = new JsonObject();
		
		json.putNumber("receiverId", Integer.parseInt(target.toString().substring(5)));
		json.putNumber("senderId", Integer.parseInt(sender.toString().substring(5)));
		json.putString("endpointType", endpointType);
		json.putObject("parameters", param);
		
		return json;
	}
}
