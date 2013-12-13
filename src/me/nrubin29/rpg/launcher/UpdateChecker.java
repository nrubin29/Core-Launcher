package me.nrubin29.rpg.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.JOptionPane;

public class UpdateChecker {

	private boolean done = false, gameExists = false;
	
	public UpdateChecker(Launcher l) {
		boolean shouldUpdate = false;
		l.write("Beginning launcher update check. Current version is " + Launcher.VERSION);
		
		l.write("Getting launcher version information from server.");
		
		try {
			URL url = new URL("http://rpg-core.comule.com/game/launcher_latestversion.html");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        
	        String rVersion = in.readLine();
	        
	        in.close();
	        
	        l.write("Got remote launcher version " + rVersion);
	        
	        if (!rVersion.equals(Launcher.VERSION)) {
	        	l.write("Remote version is different. Going to update.");
	        	shouldUpdate = true;
	        }
		}
		catch (Exception e) {
			l.write("Could not retrieve remote launcher information. Not going to update.");
			shouldUpdate = false;
		}
		
		if (shouldUpdate) {
			l.write("Done checks. Beginning launcher update.");
			
			try {
				File launcher = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
				
				try {
					l.write("Beginning connection to server.");
					
					URL url = new URL("http://rpg-core.comule.com/game/launcher.jar");
				    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				    FileOutputStream fos = new FileOutputStream(launcher);
				    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				    fos.close();
				    
				    l.write("Launcher download done.");
				    
				    JOptionPane.showMessageDialog(l, "Launcher update done. Quitting. Please restart.");
				    
				    System.exit(0);
				}
				catch (Exception ex) {
					l.write("Could not download launcher.");
					ex.printStackTrace();
				}
			}
			catch (Exception ex) { ex.printStackTrace(); }
		}
		
		else {
			l.write("Not going to update launcher.");
		}
		
		l.write("*****");
		
		shouldUpdate = false;
		String cVersion = "0";
		
		l.write("Beginning game version check...");
		
		File vFile = l.getFile("version.config");
		
		if (!vFile.exists()) {
			l.write("Did not find local game version information. Going to update.");
			shouldUpdate = true;
		}
		
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(vFile));
				String line = reader.readLine();
				cVersion = line.substring("version: ".length());
				reader.close();
				
				l.write("Found local game version " + cVersion);
			}
			catch (Exception e) {
				l.write("An error occurred in reading the game version information. Going to update.");
				shouldUpdate = true;
			}
		}
		
		if (!shouldUpdate) {
			l.write("Getting game version information from server.");
			
			try {
				URL url = new URL("http://rpg-core.comule.com/game/latestversion.html");
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		        
		        String rVersion = in.readLine();
		        
		        in.close();
		        
		        l.write("Got remote game version " + rVersion);
		        
		        if (!rVersion.equals(cVersion)) {
		        	l.write("Remote game version is different. Going to update.");
		        	shouldUpdate = true;
		        }
			}
			catch (Exception e) {
				l.write("Could not retrieve remote game information. Not going to update.");
				shouldUpdate = false;
			}
		}
		
		if (shouldUpdate) {
			l.write("Done checks. Beginning game update.");
			
			try {
				File game = l.getFile("game.jar");
				
				try {
					l.write("Beginning connection to server.");
					
					URL url = new URL("http://rpg-core.comule.com/game/game.jar");
				    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				    FileOutputStream fos = new FileOutputStream(game);
				    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				    fos.close();
				    
				    l.write("Game download done.");
				    
				    gameExists = true;
				    
				    File oldVFile = l.getFile("version.config");
				    
				    oldVFile.delete();
				    
				    l.write("Deleted old game version file.");
				    
				    done = true;
				}
				catch (Exception ex) {
					l.write("Could not download game.");
					ex.printStackTrace();
					
					if (!game.exists()) {
						l.write("No existing copy of game found. Relaunch with an internet connection.");
						done = true;
						return;
					}
					
					gameExists = true;
					done = true;
				}
			}
			catch (Exception ex) { ex.printStackTrace(); done = true; }
		}
		
		else {
			l.write("Not going to update game.");
			done = true;
		}
	}
	
	public boolean isDone() {
		return done;
	}
	
	public boolean gameExists() {
		return gameExists;
	}
}