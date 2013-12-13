package me.nrubin29.core.launcher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileSystemView;

public class Launcher extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public static final String
			NAME = "Core",
			VERSION = "1.2.3";
	
	private JTextArea log = new JTextArea();
	JTextField userName = new JTextField();
	JPasswordField password = new JPasswordField();
	
	private UpdateChecker checker;
	
	public Launcher() {
		super("Core Launcher");
		
		write("Loading...");
		
		JLabel logo = new JLabel(ResourceUtil.getImage("logo"));
		logo.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		final JTextPane news = new JTextPane();
		news.setText("Loading news...");
		news.setEditable(false);
		
		new Thread(new Runnable() {
			public void run() {
				try { news.setPage("http://rpg-core.comule.com/game/news.html"); }
				catch (Exception e) { news.setText("Could not load news."); }
			}
		}).start();
		
		log.setEditable(false);
		
		JButton play = new JButton("Play");
		play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!checker.isDone()) {
					JOptionPane.showMessageDialog(Launcher.this, "Update checker still running.");
					return;
				}
				
				if (!checker.gameExists()) {
					JOptionPane.showMessageDialog(Launcher.this, "It appears the game isn't installed. Try restarting the launcher with an internet connection.");
					return;
				}
				
				if (userName.getText().equals("")) {
					JOptionPane.showMessageDialog(Launcher.this, "You didn't enter a username.");
					return;
				}
				
				if (new String(password.getPassword()).equals("")) {
					JOptionPane.showMessageDialog(Launcher.this, "You didn't enter a password.");
					return;
				}
				
				boolean correctLogin = false;
				
				try {
					write("Connecting to server to verify credentials.");
					
					URL url = new URL("http://rpg-core.comule.com/game/users.html");
			        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			        
			        while (in.ready()) {
			        	String[] strs = in.readLine().split(" ");
			        	
			        	if (strs[0].equals(userName.getText()) && strs[1].equals(stringToSHA256(new String(password.getPassword())))) {
			        		correctLogin = true;
			        		break;
			        	}
			        }
			        
			        in.close();
			        
			        if (correctLogin) {
			        	write("Credentials accepted. Launching.");
			        	
			        	try {
							Process p = new ProcessBuilder("java", "-jar", getFile("game.jar").getPath(), userName.getText()).start();
							setProcess(p);
						}
						catch (Exception ex) {
							write("Could not launch game.");
							ex.printStackTrace();
						}
			        }
			        
			        else {
			        	JOptionPane.showMessageDialog(Launcher.this, "Incorrect username or password.");
						return;
			        }
				}
				catch (Exception ex) {
					JOptionPane.showMessageDialog(Launcher.this, "Could not verify credentials. Make sure you are connected to the internet.");
				}
			}
		});
		
		JButton force = new JButton("Force Update");
		force.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!checker.isDone()) return;
				checker = new UpdateChecker(Launcher.this, true);
			}
		});
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
		textPanel.add(new JScrollPane(news));
		textPanel.add(new JScrollPane(log));
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setMaximumSize(new Dimension(800, 40));
		bottomPanel.add(new JLabel(" Username: "));
		bottomPanel.add(userName);
		bottomPanel.add(new JLabel(" Password: "));
		bottomPanel.add(password);
		bottomPanel.add(play);
		bottomPanel.add(force);
		
		add(logo);
		add(new JLabel("v" + VERSION));
		add(textPanel);
		add(bottomPanel);
		
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(new Dimension(800, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        
        write("Done!");
        
        checker = new UpdateChecker(this, false);
	}
	
	void write(String str) {
		log.append("[" + new Date() + "] " + str + "\n");
	}
	
	void setProcess(Process process) {
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
	
	public File getRootFolder() {
		String homedir = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		String osname = System.getProperty("os.name").toLowerCase();
		File rootFolder;
			
		if (osname.startsWith("mac")) rootFolder = new File(homedir + "/Library/Application Support/" + NAME);
		else if (osname.startsWith("linux")) rootFolder = new File(homedir + "/." +  NAME + "/");
		else if (osname.startsWith("win")) rootFolder = new File(System.getenv("APPDATA") + "\\." + NAME + "\\");
		else throw new RuntimeException("Unsupported OS: " + osname);

        if (!rootFolder.exists()) {

            boolean success = false;

            try { success = rootFolder.mkdir(); }
            catch (Exception e) { write("Could not create folder."); }

            if (!success) write("Could not create folder.");
        }
        
        return rootFolder;
	}
	
	public File getFile(String name) {
		File rootFolder = getRootFolder();
		
		File f = new File(rootFolder, name);
		
		return f;
	}
	
	private String stringToSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        }
        catch (Exception ex) { ex.printStackTrace(); return null; }
    }
	
	public static void main(String[] args) {
        new Launcher();
    }
}