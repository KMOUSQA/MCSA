package kmou.selab.mcsp;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import kmou.selab.mcsp.dma.DmaMaritimeCloud;
import kmou.selab.mcsp.proxy.ServiceProxy;
import kmou.selab.mcsp.proxy.VertxServer;
import kmou.selab.mcsp.util.SysStreamsLogger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.LoggerFactory;

import com.bbn.openmap.MapHandler;

public class ProxyBootstraper {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProxyBootstraper.class);
	private JFrame frame;
 
	private MapHandler mapHandler;
	private DmaMaritimeCloud dmaMcService;
	private ServiceProxy proxy;
	private VertxServer proxyServer;
	private final JPanel panel = new JPanel();
	private final JScrollPane scrollPane = new JScrollPane();
	private JTextArea textArea = new JTextArea();

	public Properties  prop = new Properties();
	
	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public ProxyBootstraper() {

		mapHandler = new MapHandler();

		dmaMcService = new DmaMaritimeCloud(this);
		mapHandler.add(dmaMcService);

		proxy = new ServiceProxy();
		mapHandler.add(proxy);

		proxyServer = new VertxServer();
		mapHandler.add(proxyServer);


		guiInitialize();
		
		try {
			initConfiguration();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws Exception 
	 * @wbp.parser.entryPoint
	 */
	private void guiInitialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setTitle("Maritime Cloud Service Agent 0.0.5");
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		panel.add(scrollPane);

		
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		SysStreamsLogger.bindSystemStreams(textArea);
			
		
		
		//PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
		//System.setOut(printStream);
		//System.setErr(printStream);
		
		
		
		JTextAreaAppender appender = new JTextAreaAppender(textArea);
		appender.addFilter(new Filter() {
		    @Override
		    public int decide(LoggingEvent event) {
		        if (event.getLevel().equals(Level.INFO)) {
		            return ACCEPT;
		        } else {
		            return DENY;
		        }
		    }
		});
		Logger.getRootLogger().addAppender(appender);
		
		
	}
	
	
	private void initConfiguration() throws MalformedURLException, IOException {
		
		
		String directory = new File(ProxyBootstraper.class.getProtectionDomain().getCodeSource().getLocation().getFile())
			.getAbsoluteFile().getParentFile().getAbsolutePath();
		
		File dir = new File(directory);
		File properiesFile = new File(dir, "mcsa.properties");
		
		
		
		if(properiesFile.exists()){
			prop.load(properiesFile.toURI().toURL().openStream());
		}else{
			prop.load(getClass().getResource("/mcsa.properties").openStream());
		}
		
		//System.out.println("[ProxyBootstraper]dma.mms.url : " + prop.getProperty("dma.mms.url"));
		
		
		String s = (String)JOptionPane.showInputDialog(
                frame,
                "Please enter the url of MMS",
                prop.getProperty("dma.mms.url"));
		
		//If a string was returned, say so.
		if ((s != null) && (s.length() > 0)) {
			prop.setProperty("dma.mms.url", s);
		}else{
			
		}
		
		prop.store(new FileOutputStream(properiesFile), "MCSA Property file");
	}

	/**
	 * This class extends from OutputStream to redirect output to a JTextArrea
	 */
	/*
	public class CustomOutputStream extends OutputStream {
	    private JTextArea textArea;
	     
	    public CustomOutputStream(JTextArea textArea) {
	        this.textArea = textArea;
	    }
	     
	    @Override
	    public void write(int b) throws IOException {
	        // redirects data to the text area
	        textArea.append(String.valueOf((char)b));
	        // scrolls the text area to the end of data
	        textArea.setCaretPosition(textArea.getDocument().getLength());
	    }
	}
	*/
	
	
	public class JTextAreaAppender extends AppenderSkeleton implements
			DocumentListener {
		// --------------------------------------------------------------------------
		// Fields
		// --------------------------------------------------------------------------

		/**
		 * Text area that logging statements are directed to.
		 */
		private JTextArea textArea_;

		/**
		 * Layout for logging statements.
		 */
		private PatternLayout layout_;

		// --------------------------------------------------------------------------
		// Constructors
		// --------------------------------------------------------------------------

		/**
		 * Creates a new text area appender.
		 *
		 * @param textArea
		 *            Text area to connect the appender to.
		 */
		public JTextAreaAppender(JTextArea textArea) {
			textArea_ = textArea;
			textArea_.getDocument().addDocumentListener(this);
			layout_ = new PatternLayout("%-5p %3x - %m%n");
		}

		// --------------------------------------------------------------------------
		// Public
		// --------------------------------------------------------------------------

		/**
		 * Returns the text area.
		 *
		 * @return JTextArea
		 */
		public JTextArea getTextArea() {
			return textArea_;
		}

		// --------------------------------------------------------------------------
		// Overrides org.apache.log4j.AppenderSkeleton
		// --------------------------------------------------------------------------
		
		/**
		 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
		 */
		@Override
		public void append(LoggingEvent loggingEvent) {
			loggingEvent.getLevel();
			
			textArea_.append(layout_.format(loggingEvent));
		}

		/**
		 * @see org.apache.log4j.Appender#requiresLayout()
		 */
		public boolean requiresLayout() {
			return false;
		}

		/**
		 * @see org.apache.log4j.Appender#close()
		 */
		public void close() {
		}

		// --------------------------------------------------------------------------
		// javax.swing.event.DocumentListener Interface
		// --------------------------------------------------------------------------

		/**
		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
		 */
		public void changedUpdate(DocumentEvent event) {
		}

		/**
		 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
		 */
		public void removeUpdate(DocumentEvent event) {
		}

		/**
		 * Sets the caret position to the end of the text in the text component
		 * whenever it is updated.
		 *
		 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
		 */
		public void insertUpdate(DocumentEvent event)
	    {
			textArea_.setCaretPosition(textArea_.getDocument().getLength());
	    }

	
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// MmsiId id = new MmsiId(123456789);
		// System.out.println(Integer.getInteger(id.toString()));

		/*
		 * JsonObject json = new JsonObject(); json.putString("type",
		 * "chat-message"); json.putNumber("source", Integer.parseInt((new
		 * MmsiId(123456780)).toString().substring(5)));
		 * json.putNumber("target", Integer.parseInt((new
		 * MmsiId(123456789)).toString().substring(5))); json.putString("data",
		 * "asdf"); SimpleDateFormat format = new
		 * SimpleDateFormat("yyyy.MM.dd HH:mm"); json.putString("time",
		 * format.format(new Date(System.currentTimeMillis())) );
		 * System.out.println(json.toString());
		 */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProxyBootstraper serviceProxy = new ProxyBootstraper();
					serviceProxy.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
