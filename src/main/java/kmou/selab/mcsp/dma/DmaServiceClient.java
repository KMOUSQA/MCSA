package kmou.selab.mcsp.dma;

import java.net.URI;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import kmou.selab.mcsp.proxy.ServiceProxy;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.net.mms.MmsConnectionClosingCode;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;
import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.service.ChatServiceHandlerCommon;
import dk.dma.epd.common.prototype.service.MaritimeCloudService;
import dk.dma.epd.common.prototype.service.MaritimeCloudUtils;
import dk.dma.epd.common.util.Util;

public class DmaServiceClient extends MaritimeCloudService{

	private static final Logger LOG = LoggerFactory.getLogger(DmaServiceClient.class);
	
	protected static final boolean LOG_MARITIME_CLOUD_ACTIVITY = false;
	protected static final int MARITIME_CLOUD_SLEEP_TIME = 10000;
	
	public ServiceProxy proxy;
	
	MaritimeId id;
	
	String mmsUrl;
	
	public MaritimeId getMaritimeId(){
		return id;
	}
	
	public DmaServiceClient(MaritimeId id, String mmsUrl) {
		this.id = id;
		this.mmsUrl = mmsUrl;
	}
	/**
     * Thread run method
     */
    @Override
    public void run() {

        // Start by connecting
        while (!stopped) {
            Util.sleep(MARITIME_CLOUD_SLEEP_TIME);
            
            //MaritimeId id = EPD.getInstance().getMaritimeId();
            if (id != null || !(MaritimeCloudUtils.toMmsi(id)==0)) {
                if (initConnection(hostPort, id)) {
                    try {
                        fireConnected(connection);
                    } catch (Exception e) {
                        fireError(e.getMessage());
                    }
                    break;
                }
            }
        }

        // Periodic tasks
        while (!stopped) {
            Util.sleep(MARITIME_CLOUD_SLEEP_TIME);
        }

        // Flag that we are stopped
        fireDisconnected();
    }
    
    /**
     * Create the Maritime Cloud connection
     */
    private boolean initConnection(String host, MaritimeId id) {
        LOG.info("[DmaServiceClient] Connecting to cloud server: " + host + " with maritime id " + id);

        MmsClientConfiguration enavCloudConnection = MmsClientConfiguration.create(id);
        enavCloudConnection.properties().setName(DmaServiceClient.class.getSimpleName() + " - " + id.toString());


        // Hook up a position reader
        enavCloudConnection.setPositionReader(new PositionReader() {
            @Override
            public PositionTime getCurrentPosition() {
                long now = System.currentTimeMillis();
                Position pos = null;//EPD.getInstance().getPosition();
                if (pos != null) {
                    return PositionTime.create(pos.getLatitude(), pos.getLongitude(), now);
                } else {
                    return PositionTime.create(54.977531, 10.606059, System.currentTimeMillis());
                }
            }
        });

        // Check if we need to log the MaritimeCloudConnection activity
        enavCloudConnection.addListener(new MmsConnection.Listener() {
            @Override
            public void connecting(URI host) {
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("[DmaServiceClient] Connecting to " + host);
                }
            }

            

            @Override
            public void binaryMessageReceived(byte[] message) {
                cloudStatus.markCloudReception();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("[DmaServiceClient] Received binary message: " + (message == null ? 0 : message.length) + " bytes");
                }
            }

            @Override
            public void binaryMessageSend(byte[] message) {
                cloudStatus.markSuccesfullSend();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("[DmaServiceClient] Sending binary message: " + (message == null ? 0 : message.length) + " bytes");
                }
            }

            @Override
            public void textMessageReceived(String message) {
                cloudStatus.markCloudReception();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("[DmaServiceClient] Received text message: " + message);
                }
            }

            @Override
            public void textMessageSend(String message) {
                cloudStatus.markSuccesfullSend();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("[DmaServiceClient] Sending text message: " + message);
                }
            }

            @Override
            public void disconnected(MmsConnectionClosingCode closeReason) {
                cloudStatus.markFailedReceive();
                cloudStatus.markFailedSend();
                if (LOG_MARITIME_CLOUD_ACTIVITY) {
                    LOG.info("[DmaServiceClient] Disconnected with reason: " + closeReason);
                }
            }
        });

        try {
            enavCloudConnection.setHost(host);
            connection = enavCloudConnection.build();

            if (connection != null) {
//                cloudStatus.markCloudReception();
//                cloudStatus.markSuccesfullSend();
            	JsonObject json = new JsonObject();
            	json.putString("type", "mc-ready");
            	json.putString("data", "Connected succesfully to cloud server");
            	//proxy.sendMessage( "mc-ready",id, id, "Connected succesfully to cloud server");
            	
            	proxy.sendMessageToVertxClient(id, id, json);
            	
                LOG.info("[DmaServiceClient] Connected succesfully to cloud server: " + host + " with shipId " + id);
                return true;
            } else {
                fireError("Failed building a maritime cloud connection");
                return false;
            }
        } catch (Exception e) {
            fireError(e.getMessage());
            cloudStatus.markFailedSend();
            cloudStatus.markFailedReceive();
            LOG.error("Failed to connect to server: " + e);
            return false;
        }
    }

	
	protected void readEnavSettings() {
        this.hostPort = mmsUrl;//"mms03.maritimecloud.net:43234";
    }
	
	/*public void addChatServiceHandler(ChatServiceHandlerCommon chatHandler){
		addListener(chatHandler);
	}
	public void removeChatServiceHandler(ChatServiceHandlerCommon chatHandler){
		removeListener(chatHandler);
	}*/
}
