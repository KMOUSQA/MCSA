package kmou.selab.mcsp.proxy;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.websocket.DecodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;


import com.bbn.openmap.MapHandlerChild;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dk.dma.epd.common.prototype.service.MaritimeCloudService;

public class VertxServer extends MapHandlerChild{
	
	//ServiceProxy proxy;
	EventBus eventBus;
	Vertx vertx;
	
	//ProxyConnectionListener connectedListener;
	ServiceProxy proxy;
	HashMap<String, VertxClient> clients = new HashMap<String, VertxClient>();
	
	
	
	private static final Logger LOG = LoggerFactory.getLogger(VertxServer.class);
	//public VertxServer(ServiceProxy proxy){
	public VertxServer(){
		//this.proxy = proxy;
		vertxInit();
	}
	
	private void vertxInit(){
		vertx = VertxFactory.newVertx();
		HttpServer http = vertx.createHttpServer();
		eventBus = vertx.eventBus();

		http.requestHandler(new Handler<HttpServerRequest>(){
			@Override
			public void handle(HttpServerRequest req) {
				LOG.info("[VertxServer] requestHandler HttpServerRequest");
				if(req.path().equals("/")){
					req.response().sendFile("./ws2.html");
				}
			}
			
		}).listen(8080);
		vertx.createHttpServer().websocketHandler(new ChatSocketHandler(vertx)).listen(8090);
		
	}
	
	public void addClient(String vertxHandlerId, VertxClient client){
		LOG.info("[VertxServer] addClient " + vertxHandlerId + " " + client.getMaritimeId());
		clients.put(vertxHandlerId, client);
	}
	
	public VertxClient getClient(String handleId){
		return clients.get(handleId);
	}
	
	/*public void setConnectedListener(ProxyConnectionListener listener){
		connectedListener = listener;
	}
	*/
	public void connectedClient(String handle,ServerWebSocket ws){
		LOG.info("[VertxServer] connectedClient : " + handle);
		
		VertxClient client = new VertxClient(handle,ws);
		client.setVertxServer(this);
		addClient(handle,client );
	}
	
	public void removeClient(String handle){
		LOG.info("[VertxServer] removeClient " + handle);
		clients.remove(handle);
	}
	
	public void disconnectClient(String handle){
		//connecting.remove(handle);
		LOG.info("[VertxServer] disconnectClient " + handle);
		clients.remove(handle).close();
	}
	
	public void disconnectedClient(String handle){
		LOG.info("[VertxServer] disconnectedClient " + handle);

		proxy.disconnectFromMc(clients.remove(handle).getMaritimeId());
	}
	
	
	
	@Override
	public void findAndInit(Object obj) {
		// TODO Auto-generated method stub
		/*if(obj instanceof ProxyConnectionListener){
			setConnectedListener((ProxyConnectionListener) obj);
		}*/
		if(obj instanceof ServiceProxy){
			proxy = (ServiceProxy)obj;
		}
	}

	/*private void receivedData(String handleId, Buffer data){
		//JsonObject json = new JsonObject(data.toString());
		clients.get(handleId).receiveMessage(data.toString());
	}*/
	private void initClient(String handleId, int mmsi, String name){
		LOG.info("[VertxServer] initChatClient : " + handleId + " " + mmsi + " " + name);
		VertxClient client = clients.get(handleId);
		client.setMmsi(mmsi);
		client.setName(name);
		proxy.registerProxyClients(client);
	}
	
	class ChatSocketHandler implements Handler<ServerWebSocket> {

		Vertx vertx;
		
		public ChatSocketHandler(Vertx vertx){
			this.vertx = vertx;
			
		}
		
		@Override
		public void handle(ServerWebSocket ws) {
			LOG.info("[VertxServer] Handling WS : " + ws.textHandlerID());
			final String id = ws.textHandlerID();
			//if(ws.path().equals("/mcchat"))
			{
				if(!clients.containsKey(id)){
					connectedClient(id,ws);
				}
				ws.dataHandler(new Handler<Buffer>(){
					public void handle(Buffer data) {
						LOG.info("[VertxServer] dataHandler WS : " + ws.textHandlerID() + " : " + data.toString());
						
						try{
							JsonObject json = new JsonObject(data.toString());
							String messageType = json.getString("type");
							String endpointType = json.getString("endpointType");
							if(messageType != null){
								if(messageType.equalsIgnoreCase("connect")){
									int mmsi = json.getNumber("source").intValue();
									String name = json.getString("name");
									initClient(id, mmsi, name);
								}else{// if(messageType.equalsIgnoreCase("chat-message")){
									//clients.get(ws.textHandlerID()).receiveMessage(json);
								}
							}else if(endpointType.equalsIgnoreCase("dma.messaging.MaritimeTextingService.sendMessage")){
								clients.get(ws.textHandlerID()).receiveMessage(json);
							}
						}catch(org.vertx.java.core.json.DecodeException ex){
							ex.printStackTrace();
							ws.close();
							removeClient(ws.textHandlerID());
							
						}
					}
				});
			}/*else{
				ws.reject();
			}*/
			ws.closeHandler(new Handler<Void>(){

				@Override
				public void handle(Void paramE) {
					
					LOG.info("[VertxServer] closeHandler handle " + ws.textHandlerID());
					disconnectedClient(ws.textHandlerID());
					
				}
				
			});
			ws.drainHandler(new Handler<Void>(){

				@Override
				public void handle(Void paramE) {
					LOG.info("[VertxServer] drainHandler handle "+ ws.textHandlerID());

				}
				
			});
			ws.endHandler(new Handler<Void>(){
				@Override
				public void handle(Void paramE) {
					LOG.info("[VertxServer] endHandler handle " + ws.textHandlerID());
					
				}
			});
			
			
		}
		
	}
}
