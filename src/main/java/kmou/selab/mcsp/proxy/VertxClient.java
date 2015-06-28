package kmou.selab.mcsp.proxy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kmou.selab.mcsp.dma.DmaChatServiceClientHandler;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;




public class VertxClient {
	private static final Logger LOG = LoggerFactory.getLogger(VertxServer.class);
	
	public String vertxHandleId = "";
	VertxServer vertxServer;
	MaritimeId id;
	private String name;
	private ServiceProxy proxy;
	private ServerWebSocket ws;
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public String getHandleId(){
		return vertxHandleId;
	}
	public void setHandleId(String handleId){
		this.vertxHandleId = handleId;
	}

	public VertxClient(String vertxHandlerId, ServerWebSocket ws){
		setHandleId(vertxHandlerId);
		this.ws = ws;
	}
	
	public void setVertxServer(VertxServer server){
		this.vertxServer = server;
	}
	
	public void setProxy(ServiceProxy proxy){
		this.proxy = proxy;
	}
	
	
	
	public void setMmsi(int mmsi){
		this.id = new MmsiId(mmsi);
	}
	public MaritimeId getMaritimeId(){
		return id;
	}
	
	/*public void sendMessage(String type, MaritimeId sender, String message){
		JsonObject json = new JsonObject();
		//json.putString("type", type);
		json.putNumber("receiverId", Integer.parseInt(((MmsiId)sender).toString().substring(5)));
		json.putNumber("senderId", Integer.parseInt(((MmsiId)id).toString().substring(5)));
		json.putString("endpointType", "dma.messaging.MaritimeTextingService.sendMessage");
		
		JsonObject param = new JsonObject();
		
		
		json.putObject("parameters", message);
		
		//json.putString("time", dateFormat.format(new Date(System.currentTimeMillis())) );
		//json.putString("time", dateFormat.format(new Date(System.currentTimeMillis())) );
		
		LOG.info("[VertxClient] send message : " + json.toString());
		
		vertxServer.eventBus.send(vertxHandleId, json.toString());
	}*/
	
	/*public void sendMessage(MaritimeId sender, String message){
		JsonObject json = new JsonObject();
		//json.putString("type", type);
		json.putNumber("receiverId", Integer.parseInt(((MmsiId)sender).toString().substring(5)));
		json.putNumber("senderId", Integer.parseInt(((MmsiId)id).toString().substring(5)));
		json.putString("endpointType", "dma.messaging.MaritimeTextingService.sendMessage");
		
		JsonObject param = new JsonObject();
		
		
		json.putObject("parameters", message);
		
		//json.putString("time", dateFormat.format(new Date(System.currentTimeMillis())) );
		//json.putString("time", dateFormat.format(new Date(System.currentTimeMillis())) );
		
		LOG.info("[VertxClient] send message : " + json.toString());
		
		vertxServer.eventBus.send(vertxHandleId, json.toString());
	}*/
	

	
	/*public void sendMessage(MaritimeId sender, JsonObject object) {
		
		JsonObject json = new JsonObject();
		json.putString("type", type);
		json.putNumber("source", Integer.parseInt(((MmsiId)sender).toString().substring(5)));
		json.putNumber("target", Integer.parseInt(((MmsiId)id).toString().substring(5)));
		json.putString("data", message);
		json.putString("time", dateFormat.format(new Date(System.currentTimeMillis())) );
		
		LOG.info("[VertxClient] send message : " + object.toString());
		
		vertxServer.eventBus.send(vertxHandleId, object.toString());
	}*/
	public void sendMessage(JsonObject jsonObject) {
		LOG.info("[VertxClient] sendMessage : " + jsonObject.toString());
		vertxServer.eventBus.send(vertxHandleId, jsonObject.toString());
	}
	
	public void receiveMessage(JsonObject json) {
		/*if(json.getString("type").equalsIgnoreCase("chat-message")){
			int source = json.getNumber("source").intValue();
			int target = json.getNumber("target").intValue();
			String message = json.getString("data");
			//String date = json.getString("time");
			proxy.sendMessage("chat-message", new MmsiId(source), new MmsiId(target), message);
		}else{
			int source = json.getNumber("source").intValue();
			int target = json.getNumber("target").intValue();
			String message = json.getString("data");
			//String date = json.getString("time");
			proxy.sendMessage(json.getString("type"), new MmsiId(source), new MmsiId(target), message);
		}*/
		
		int source = json.getNumber("senderId").intValue();
		int target = json.getNumber("receiverId").intValue();
		JsonObject param = json.getObject("parameters");
		//String date = json.getString("time");
		
		LOG.info("[VertxClient] receiveMessage : " + json.toString());
		
		proxy.sendMessage(json.getString("endpointType"), new MmsiId(source), new MmsiId(target), param);
	}


	/*public void receiveMessage(String message) {
		
	}*/


	public void close() {
		ws.close();
		
	}
	
	public void setName(String name) {
		this.name = name;
		
	}
	


	
	
	
	
	
	
}

