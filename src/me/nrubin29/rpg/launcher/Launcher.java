package me.nrubin29.rpg.launcher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

public class Launcher extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JTextArea log = new JTextArea();
	
	public Launcher() {
		super("RPG-Core Launcher");
		
		write("Loading...");
		
		JLabel logo = new JLabel(ResourceUtil.getImage("logo"));
		logo.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		final JTextArea news = new JTextArea("Loading news...");
		news.setEditable(false);
		
		new Thread(new Runnable() {
			public void run() {
				try {
					URL url = new URL("http://rpg-core.comule.com/news/news.html");
			        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			        
			        news.setText("");
			        
			        while (in.ready()) {
			        	String line = in.readLine();
			        	
			        	if (line.startsWith("<!--")) break;
			        	
			        	news.append(line + "\n");
			        }
			        
			        in.close();
				}
				catch (Exception e) { news.setText("Could not load news."); }
			}
		}).start();
		
		log.setEditable(false);
		
		final JTextField name = new JTextField();
		
		JButton play = new JButton("Play");
		play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				write("Downloading game...");
				
				try {
					File game = getFile("game.jar");
					
					try {
						URL url = new URL("http://rpg-core.comule.com/game/game.jar");
					    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
					    FileOutputStream fos = new FileOutputStream(game);
					    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					    fos.close();
					}
					catch (Exception ex) {
						write("Could not download game.");
						ex.printStackTrace();
						
						if (game.exists()) {
							write("Found existing copy; launching.");
							
							Process p = new ProcessBuilder("java", "-jar", game.getPath(), name.getText()).start();
							
							setProcess(p);
							
							return;
						}
						else {
							write("No existing copy. Relaunch with an internet connection.");
							
							return;
						}
					}
				    
				    write("Done. Launching...");
					
					Process p = new ProcessBuilder("java", "-jar", game.getPath(), name.getText()).start();
					
					setProcess(p);
				}
				catch (Exception ex) { ex.printStackTrace(); }
			}
		});
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
		textPanel.add(new JScrollPane(news));
		textPanel.add(new JScrollPane(log));
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setMaximumSize(new Dimension(800, 40));
		bottomPanel.add(new JLabel(" Name: "));
		bottomPanel.add(name);
		bottomPanel.add(play);
		
		add(logo);
		add(textPanel);
		add(bottomPanel);
		
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(new Dimension(800, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        
        write("Done!");
	}
	
	private void write(String str) {
		log.append("[" + new Date() + "] " + str + "\n");
	}
	
	private void setProcess(Process process) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						String line = errorReader.readLine();
						if (line != null) write(line);
					}
				}
				catch (Exception e) { }
			}
		}).start();
		
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						String line = reader.readLine();
						if (line != null) write(line);
					}
				}
				catch (Exception e) { }
			}
		}).start();
	}
	
	private final File getFile(String name) {
		String homedir = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		String osname = System.getProperty("os.name").toLowerCase();
		File rootFolder;
			
		if (osname.startsWith("mac")) rootFolder = new File(homedir + "/Library/Application Support/RPG-Core");
		else if (osname.startsWith("linux")) rootFolder = new File(homedir + "/.RPG-Core/");
		else if (osname.startsWith("win")) rootFolder = new File(System.getenv("APPDATA") + "\\.RPG-Core\\");
		else throw new RuntimeException("Unsupported OS: " + osname);

        if (!rootFolder.exists()) {

            boolean success = false;

            try { success = rootFolder.mkdir(); }
            catch (Exception e) { write("Could not create folder."); }

            if (!success) write("Could not create folder.");
        }
		
		File f = new File(rootFolder, name);
		
		return f;
	}
	
	public static void main(String[] args) {
        new Launcher();
    }
}