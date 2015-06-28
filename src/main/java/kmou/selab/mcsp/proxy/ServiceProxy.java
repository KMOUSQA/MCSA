package kmou.selab.mcsp.proxy;

import java.util.HashMap;

import kmou.selab.mcsp.dma.DmaMaritimeCloud;
import kmou.selab.mcsp.util.MaritimeJsonFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.MessageHeader;

import com.bbn.openmap.MapHandlerChild;

import dk.dma.epd.common.prototype.service.MaritimeCloudService;

public class ServiceProxy extends MapHandlerChild {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServiceProxy.class);
	
	DmaMaritimeCloud dmaService = null;
	
	VertxServer vertxServer;
	
	public HashMap<MaritimeId, VertxClient> proxyClients = new HashMap<MaritimeId, VertxClient>();
	//public HashMap<MaritimeId, ChatServiceClient> chatServices = new HashMap<MaritimeId, ChatServiceClient>();
	
	public ServiceProxy(){
		
	}
	
	public void connectToMc(MaritimeId id){
		dmaService.connect(id);
	}
	
	public void disconnectFromMc(MaritimeId id){
		dmaService.disconnect(id);
	}
	
	public void registerProxyClients(VertxClient client){
		
		{
			client.setProxy(this);
			proxyClients.put(client.id	, client);
			connectToMc(client.id);
			
		}
		
	}
	
	public void sendMessage(String endpointType, MaritimeId sender, MaritimeId target, String param){
		LOG.info("[ServiceProxy] sendMessage : [type : " + endpointType + "] [message : " + param + "] from " + sender + " to " + target);
		VertxClient client = proxyClients.get(target);
		if(client != null){
			LOG.info("[ServiceProxy] sendMessage : find vertx client : " + target);
			//client.sendMessage(type, sender, message);
			client.sendMessage(MaritimeJsonFactory.toJsonObject(endpointType, sender , target, param));
		}else{
			LOG.info("[ServiceProxy] sendMessage : Can't find vertx client : " + target);
			dmaService.sendMessage(endpointType, sender, target, param);
		}
	}
	
	public void sendMessage(String endpointType, MaritimeId sender, MaritimeId target, JsonObject param){
		LOG.info("[ServiceProxy] sendMessage : [type : " + endpointType + "] [message : " + param + "] from " + sender + " to " + target);
		VertxClient client = proxyClients.get(target);
		if(client != null){
			LOG.info("[ServiceProxy] sendMessage : find vertx client : " + target);
			//client.sendMessage(type, sender, message);
			client.sendMessage(MaritimeJsonFactory.toJsonObject(endpointType, sender , target, param));
		}else{
			LOG.info("[ServiceProxy] sendMessage : Can't find vertx client : " + target);
			dmaService.sendMessage(endpointType, sender, target, param);
		}
	}
	
	
	public void sendMessage(String endpointType, MaritimeId target, MessageHeader header, String param){
		LOG.info("[ServiceProxy] sendMessage : [type : " + endpointType + "] [message : " + param + "] from " + header.toString() + " to " + target);
		
		
		VertxClient client = proxyClients.get(target);
		if(client != null){
			LOG.info("[ServiceProxy] sendMessage : find vertx client : " + target);
			client.sendMessage(MaritimeJsonFactory.toJsonObject(endpointType, target, header, param));
		}else{
			LOG.info("[ServiceProxy] sendMessage : Can't find vertx client : " + target);
			dmaService.sendMessage(endpointType, header.getSender(), target, param);
		}
	}
	public void sendMessageToVertxClient(MaritimeId sender, MaritimeId target, JsonObject object){
		LOG.info("[ServiceProxy] sendMessage : " + object.toString());
		VertxClient client = proxyClients.get(target);
		if(client != null){
			LOG.info("[ServiceProxy] sendMessage : find vertx client : " + target);
			client.sendMessage( object);
		}else{
			LOG.info("[ServiceProxy] sendMessage : Can't find vertx client : " + target);
			//dmaService.sendChatMessage(sender, target, message);
		}
	}
	

	
	
	
	@Override
	public void findAndInit(Object obj) {
		
		if(obj instanceof DmaMaritimeCloud){
			dmaService = (DmaMaritimeCloud)obj;
		}else if(obj instanceof VertxServer){
			vertxServer = (VertxServer) obj;
		}
		
	}
	@Override
	public void findAndUndo(Object obj) {
		if(obj instanceof DmaMaritimeCloud){
			dmaService = null;
		}else if(obj instanceof VertxServer){
			vertxServer = null;
		}
	}
/*
	@Override
	public void connected(String handle) {
		//vertxServer.addClient(handle, new VertxClient(handle) );
	}*/
	
	public void disconnectProxyClient(MaritimeId id) {
		VertxClient client = proxyClients.remove(id);
		vertxServer.removeClient(client.vertxHandleId);
		client.close();
	}

/*	@Override
	public void disconnected(MaritimeId id) {
		// TODO Auto-generated method stub
		
	}
*/

	
	
	

}
