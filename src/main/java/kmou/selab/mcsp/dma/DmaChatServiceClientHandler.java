package kmou.selab.mcsp.dma;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import kmou.selab.mcsp.proxy.ServiceProxy;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.net.MessageHeader;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.util.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import dk.dma.epd.common.prototype.service.ChatServiceData;
import dk.dma.epd.common.prototype.service.ChatServiceHandlerCommon;
import dk.dma.epd.common.prototype.service.EnavServiceHandlerCommon;
import dk.dma.epd.common.prototype.service.MaritimeCloudUtils;
import dk.dma.epd.common.prototype.service.ChatServiceHandlerCommon.IChatServiceListener;
import dma.messaging.AbstractMaritimeTextingService;
import dma.messaging.MaritimeText;
import dma.messaging.MaritimeTextingService;
import dma.messaging.MaritimeTextingNotificationSeverity;
/*import dma.messaging.AbstractMCChatMessageService;
import dma.messaging.MCChatMessage;
import dma.messaging.MCChatMessageService;
import dma.messaging.MCNotificationSeverity;*/
 
public class DmaChatServiceClientHandler extends EnavServiceHandlerCommon implements IChatServiceListener{
	 
	private static final Logger LOG = LoggerFactory.getLogger(DmaChatServiceClientHandler.class);
	protected List<MaritimeTextingService> chatServiceList = new ArrayList<>();
	DmaMaritimeCloud dmaService;
	public ServiceProxy proxy;
	
	public static final String ENDPOINT_SENDMESSAGE = "dma.messaging.MaritimeTextingService.sendMessage";
	
	
	public DmaChatServiceClientHandler() {
        super();

     // Schedule a refresh of the chat services approximately every minute
        scheduleWithFixedDelayWhenConnected(new Runnable() {
            @Override
            public void run() {
                RefresheChatServices();
            }
        }, 5, 5, TimeUnit.SECONDS);
        //}, 5, 64, TimeUnit.SECONDS);
    }

	public DmaChatServiceClientHandler(DmaMaritimeCloud dmaService){
		this.dmaService = dmaService;
	}
	
	@Override
	public void chatMessagesUpdated(MaritimeId targetId) {
		//for(MCChatMessage message:  getChatMessages().get(targetId).getLatestMessage())
		{
			System.out.println(getChatMessages().get(targetId).getLatestMessage().toString());
		}
	}
	
	protected void receiveChatMessage(String endpointType, MessageHeader header, MaritimeText msg){
		LOG.info("[DmaChatServiceClientHandler] receiveChatMessage : msg " + msg.toJSON()); 
		dmaService.receiveMessage(endpointType, getServiceClient().getMaritimeId(), header, msg.toJSON());
		
	}

	/*protected void receiveChatMessage(MaritimeId senderId, MaritimeText message) {
		LOG.info("[DmaChatServiceClientHandler] receiveChatMessage : " + senderId.toString() + " " + message.getMsg());
		dmaService.receiveChatMessage(senderId, getServiceClient().getMaritimeId(), message);
	}
	
	
	
	//receiveChatMessage(header.getSender(), msg, header.getSenderTime());
	protected void receiveChatMessage(MaritimeId senderId, MaritimeText msg, Timestamp time){
		dmaService.receiveChatMessage(senderId, getServiceClient().getMaritimeId(), message);
	}*/
	
	/*public void sendChatMessage(MaritimeId senderId, String message){
		LOG.info("[DmaChatServiceClientHandler] sendChatMessage : " + senderId.toString() + " " + message);
		this.sendChatMessage(senderId, message, MaritimeTextingNotificationSeverity.MESSAGE);	
	}*/
	
	public void close() {
		getMmsClient().close();
		findAndUndo(getMaritimeCloudService());
	}

	/*public void sendMessage(MaritimeId sender, String message) {
		
		sendChatMessage(sender, message);
		
	}*/
	
	@Override
	public void cloudDisconnected() {
		dmaService.cloudDisconnected(this.getMmsClient().getClientId());
	}
	
	public DmaServiceClient getServiceClient(){
		return (DmaServiceClient)getMaritimeCloudService();
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void cloudConnected(final MmsClient connection) {
        // Refresh the service list
    	RefresheChatServices();

        // Register a cloud chat service
        try {
            getMmsClient().endpointRegister(new AbstractMaritimeTextingService() {
				
				@Override
				protected void sendMessage(MessageHeader header, MaritimeText msg) {
					// TODO Auto-generated method stub
					//receiveChatMessage(context.getCaller(), msg);
					//receiveChatMessage(header.getSender(), msg, header.getSenderTime());
					receiveChatMessage(ENDPOINT_SENDMESSAGE, header, msg);
					
				}
			}).awaitRegistered(4, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            LOG.error("Error hooking up services", e);
        }
    }
    
	
	/**
     * Refreshes the list of chat services
     */
    public void RefresheChatServices() {
        try {
            chatServiceList = getMmsClient().endpointLocate(MaritimeTextingService.class).findAll().get();

            List<MaritimeId> newChatTargets = new ArrayList<>();
            
            JsonObject json = new JsonObject();
            json.putString("type", "mc-targetlist-update");
            
            
            JsonArray jArray = new JsonArray();
            
            for (MaritimeTextingService chatService : chatServiceList) {
            	//jArray.addObject(new JsonObject().putNumber("target", Integer.parseInt(chatService.getCaller().toString().substring(5))));
            	jArray.addObject(new JsonObject().putNumber("target", Integer.parseInt(chatService.getRemoteId().toString().substring(5))));
            	LOG.info("[DmaChatServiceClientHandler] Update available chat services : " + jArray.get(jArray.size()-1));
            	//LOG.info("Update available chat services : " + chatService.getCaller());
            }
            json.putArray("targetlist", jArray);
            proxy.sendMessageToVertxClient(getServiceClient().getMaritimeId(), getServiceClient().getMaritimeId(), json);
            
            // Create an empty chat service data for new chat services
            /*for (MCChatMessageService chatService : chatServiceList) {
                if (!chatMessages.containsKey(chatService.getCaller())) {
                    getOrCreateChatServiceData(chatService.getCaller());
                    newChatTargets.add(chatService.getCaller());
                    LOG.info("Found new chat serves: " + chatService.getCaller());
                }
            }*/

            // Notify listeners
            /*for (MaritimeId id : newChatTargets) {
                fireChatMessagesUpdated(id);
            }*/
        } catch (Exception e) {
            LOG.error("[DmaChatServiceClientHandler] Failed looking up chat services", e.getMessage());
        }
    }
	

    //private static final Logger LOG = LoggerFactory.getLogger(ChatServiceHandlerCommon.class);

    //private List<MCChatMessageService> chatServiceList = new ArrayList<>();
    protected List<IChatServiceListener> listeners = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<MaritimeId, ChatServiceData> chatMessages = new ConcurrentHashMap<>();

    

    

   /**
     * Refreshes the list of chat services
     */
    /*private void fetchChatServices() {
        try {
            chatServiceList = getMmsClient().endpointFind(MCChatMessageService.class).findAll().get();

            List<MaritimeId> newChatTargets = new ArrayList<>();
            
            // Create an empty chat service data for new chat services
            for (MCChatMessageService chatService : chatServiceList) {
                if (!chatMessages.containsKey(chatService.getCaller())) {
                    getOrCreateChatServiceData(chatService.getCaller());
                    newChatTargets.add(chatService.getCaller());
                    LOG.info("Found new chat serves: " + chatService.getCaller());
                }
            }

            // Notify listeners
            for (MaritimeId id : newChatTargets) {
                fireChatMessagesUpdated(id);
            }
        } catch (Exception e) {
            LOG.error("Failed looking up chat services", e.getMessage());
        }
    }*/

    /**
     * Returns the chat services list
     * 
     * @return the chat services list
     */
    public List<MaritimeTextingService> getChatServiceList() {
        return chatServiceList;
    }

    /**
     * Checks the given MMSI in the chat service list
     * 
     * @param id the MMSI of the ship to search for
     * @return if the MMSI supports chat
     */
    public boolean availableForChat(MaritimeId id) {
        return availableForChat(MaritimeCloudUtils.toMmsi(id));
    }
    
    /**
     * Checks the given MMSI in the chat service list
     * 
     * @param mmsi the MMSI of the ship to search for
     * @return if the MMSI supports chat
     */
    public boolean availableForChat(long mmsi) {
        return MaritimeCloudUtils.findServiceWithMmsi(chatServiceList, mmsi) != null;
    }

    /**
     * Sends a chat message to the given ship
     * 
     * @param targetId the id of the ship
     * @param message the message
     * @param severity the severity
     */
    public void sendChatMessage(MaritimeId targetId, String message, MaritimeTextingNotificationSeverity severity) {

        // Create a new chat message
        MaritimeText chatMessage = new MaritimeText();
        chatMessage.setMsg(message);
        //chatMessage.setOwnMessage(true);
        switch (severity) {
            case ALERT:  chatMessage.setSeverity(MaritimeTextingNotificationSeverity.ALERT); break;
            case SAFETY : chatMessage.setSeverity(MaritimeTextingNotificationSeverity.SAFETY); break;
            case MESSAGE:  chatMessage.setSeverity(MaritimeTextingNotificationSeverity.MESSAGE); break;
            case WARNING:  chatMessage.setSeverity(MaritimeTextingNotificationSeverity.WARNING); break;
        }
        //chatMessage.setSendDate(Timestamp.now());

        LOG.info("Sending chat message to maritime id: " + targetId);

        // Store the message
        //getOrCreateChatServiceData(targetId).addChatMessage(chatMessage);

        // Find a matching chat end point and send the message
        MaritimeTextingService chatMessageService = MaritimeCloudUtils
                .findServiceWithId(chatServiceList, targetId);//(chatServiceList, MaritimeCloudUtils.toMmsi(targetId));
        if (chatMessageService != null) {
            chatMessageService.sendMessage(chatMessage);
        } else {
            LOG.error("Could not find chat service for maritime id: " + targetId);
            return;
        }

        // Notify listeners
        fireChatMessagesUpdated(targetId);
    }

    /**
     * Returns all the stored chat messages
     * 
     * @return the chatMessages
     */
    public ConcurrentHashMap<MaritimeId, ChatServiceData> getChatMessages() {
        return chatMessages;
    }

    /**
     * Returns all the stored chat messages for the given maritime id
     * 
     * @param id the maritime id
     * @return the chatMessages
     */
    public ChatServiceData getChatServiceData(MaritimeId id) {
        return chatMessages.get(id);
    }

    /**
     * Returns all the stored chat messages for the given maritime id.
     * Creates a new empty element if it did not exist in advance
     * 
     * @param id the maritime id
     * @return the chatMessages
     */
    public ChatServiceData getOrCreateChatServiceData(MaritimeId id) {
        if (!chatMessages.containsKey(id)) {
            chatMessages.put(id, new ChatServiceData(id));
        }
        return chatMessages.get(id);
    }
    
    /**
     * Called upon receiving a new chat message. Broadcasts the message to all listeners.
     * 
     * @param senderId the id of the sender
     * @param message the message
     */
    /*protected void receiveChatMessage(MaritimeId senderId, MCChatMessage message) {
        message.setOwnMessage(false);
        getOrCreateChatServiceData(senderId).addChatMessage(message);

        // Notify listeners
        fireChatMessagesUpdated(senderId);
    }*/
    
    /**
     * Clears the chat message for the given maritime id.
     * 
     * @param id the maritime id
     */
    public void clearChatMessages(MaritimeId id) {
        ChatServiceData chatData = getChatServiceData(id);
        if (chatData != null) {
            chatData.getMessages().clear();
            
            // Notify listeners
            fireChatMessagesUpdated(id);
        }
    }
    
    /**
     * Called when the chat message exchange has been updated for the given maritime id
     * 
     * @param targetId the maritime id of the target
     */
    synchronized void fireChatMessagesUpdated(MaritimeId targetId) {
        for (IChatServiceListener listener : listeners) {
            listener.chatMessagesUpdated(targetId);
        }
    }
    

    /**
     * Adds an chat service listener
     * 
     * @param listener
     *            the listener to add
     */
    public void addListener(IChatServiceListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an chat service listener
     * 
     * @param listener
     *            the listener to remove
     */
    public void removeListener(IChatServiceListener listener) {
        listeners.remove(listener);
    }

    /**
     * Interface implemented by chat service listeners
     */
    public interface IChatServiceListener {

        /**
         * Called when the chat message exchange has been updated for the given maritime id
         * 
         * @param targetId the maritime id of the target
         */
        void chatMessagesUpdated(MaritimeId targetId);
    }

	
	
	
	
	
	

}
