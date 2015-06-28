package kmou.selab.mcsp.dma;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import kmou.selab.mcsp.ProxyBootstraper;
import kmou.selab.mcsp.proxy.ServiceProxy;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.MessageHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import com.bbn.openmap.MapHandlerChild;

import dk.dma.epd.common.prototype.notification.Notification.NotificationSeverity;
import dma.messaging.MaritimeText;
import dma.messaging.MaritimeTextingNotificationSeverity;
import dma.messaging.MaritimeTextingService;

public class DmaMaritimeCloud extends MapHandlerChild {

	private static final Logger LOG = LoggerFactory.getLogger(DmaMaritimeCloud.class);
	
	//private List<MCChatMessageService> chatServiceList = new ArrayList<>();
	//private HashMap<MaritimeId, DmaServiceClient> maritimeCloudServiceClients = new HashMap<MaritimeId, DmaServiceClient>();
	
	private HashMap<MaritimeId, DmaChatServiceClientHandler> chatServiceHandlers = new HashMap<MaritimeId,DmaChatServiceClientHandler>();

	
	
	ServiceProxy proxy;
	
	ProxyBootstraper parent;
	public DmaMaritimeCloud(ProxyBootstraper parent){
		this.parent = parent;
		/*
		 * Test
		 */
		//MmsiId id = new MmsiId(111122222);
		//connect(id);
	}
	
	public DmaServiceClient connect(MaritimeId id){
		LOG.info("[DmaMaritimeCloud] connect : " + id.toString());
		DmaServiceClient client = new DmaServiceClient(id,parent.prop.getProperty("dma.mms.url"));
		client.proxy = proxy;
		//maritimeCloudServiceClients.put(id,client);
		
		initChatService(id, client);
		client.start();
		
		return client;
	}
	
	protected void initChatService(MaritimeId id, DmaServiceClient client){
		LOG.info("[DmaMaritimeCloud] initChatService : " + id.toString());
		DmaChatServiceClientHandler chatHandler = new DmaChatServiceClientHandler(this);
		chatHandler.findAndInit(client);
		chatHandler.proxy = proxy;
		//chatHandler.addListener(chatHandler);
		this.chatServiceHandlers.put(id, chatHandler);
	}
	
	public void disconnect(MaritimeId id) {
		LOG.info("[DmaMaritimeCloud] disconnect : " + id.toString());
		//maritimeCloudServiceClients.remove(id);
		try{
			chatServiceHandlers.remove(id).close();
		}catch(NullPointerException ex){
			ex.printStackTrace();
		}
	}
	public void cloudDisconnected(MaritimeId id){
		LOG.info("[DmaMaritimeCloud] cloudDisconnected : " + id.toString());
		chatServiceHandlers.remove(id);
		proxy.disconnectProxyClient(id);
	}
	
	public void sendChatMessage(MaritimeId sender, MaritimeId target, String message){
		LOG.info("[DmaMaritimeCloud] sendChatMessage : " +  sender.toString() + " " + target.toString() + " " + message);
		DmaChatServiceClientHandler chatClient = chatServiceHandlers.get(sender);
		if(chatClient != null){
			chatClient.sendChatMessage(target, message, MaritimeTextingNotificationSeverity.MESSAGE);
		}
	}
	
	public void sendMessage(String endpointType, MaritimeId sender, MaritimeId target, String jsonParam){
		LOG.info("[DmaMaritimeCloud] sendChatMessage : " +  sender.toString() + " " + target.toString() + " " + jsonParam);
		JsonObject param = new JsonObject(jsonParam);
		switch(endpointType){
		case DmaChatServiceClientHandler.ENDPOINT_SENDMESSAGE :
			DmaChatServiceClientHandler chatClient = chatServiceHandlers.get(sender);
			if(chatClient != null){
				chatClient.sendChatMessage(target ,param.getString("msg") , MaritimeTextingNotificationSeverity.valueOf(param.getString("severity")));
			}
			break;
		}
	}
	
	public void sendMessage(String endpointType, MaritimeId sender, MaritimeId target, JsonObject jsonParam){
		LOG.info("[DmaMaritimeCloud] sendChatMessage : " +  sender.toString() + " " + target.toString() + " " + jsonParam);
		switch(endpointType){
		case DmaChatServiceClientHandler.ENDPOINT_SENDMESSAGE :
			DmaChatServiceClientHandler chatClient = chatServiceHandlers.get(sender);
			if(chatClient != null){
				chatClient.sendChatMessage(target ,jsonParam.getString("msg") , MaritimeTextingNotificationSeverity.valueOf(jsonParam.getString("severity")));
			}
			break;
		}
	}
	
	/*
	public void receiveChatMessage(MaritimeId sender, MaritimeId target, String message){
		LOG.info("[DmaMaritimeCloud] receiveChatMessage : " +  sender.toString() + " " + target.toString() + " " + message);
		
		JsonObject json = new JsonObject();
		json.putString("type", "chat-message");
		json.putNumber("source", Integer.parseInt(((MmsiId)sender).toString().substring(5)));
		json.putNumber("target", Integer.parseInt(((MmsiId)target).toString().substring(5)));
		json.putString("data", message);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		json.putString("time", format.format(new Date(System.currentTimeMillis())) );
		
		
		proxy.sendMessage("chat-message", sender, target, message);
		
	}
	*/
	
	public void receiveMessage(String endpointType, MaritimeId target, MessageHeader header, String param){
		LOG.info("[DmaMaritimeCloud] receiveMessage : " +  target.toString() + " " + header.toString() + " " + param);
		
		proxy.sendMessage(endpointType, target, header, param);
	}
	
	



	
	@Override
	public void findAndInit(Object obj) {
		super.findAndInit(obj);
		/*if (obj instanceof MaritimeCloudService) {
            maritimeCloudService = (MaritimeCloudService)obj;
            maritimeCloudService.addListener(this);
        }*/
		if (obj instanceof ServiceProxy) {
			proxy = (ServiceProxy)obj;
        }/*else if(obj instanceof MessageDeliver){
        	deli
        }*/
		
	}

	
	


	
	

}
