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

public class UpdateChecker {

	private boolean done = false, gameExists = false;
	
	public UpdateChecker(Launcher l) {
		boolean shouldUpdate = false;
		String cVersion = "0";
		
		l.write("Beginning version check...");
		
		File vFile = l.getFile("version.config");
		
		if (!vFile.exists()) {
			l.write("Did not find local version information. Going to update.");
			shouldUpdate = true;
		}
		
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(vFile));
				String line = reader.readLine();
				cVersion = line.substring("version: ".length());
				reader.close();
				
				l.write("Found local version " + cVersion);
			}
			catch (Exception e) {
				l.write("An error occurred in reading the version information. Going to update.");
				shouldUpdate = true;
			}
		}
		
		if (!shouldUpdate) {
			l.write("Getting version information from server.");
			
			try {
				URL url = new URL("http://rpg-core.comule.com/game/latestversion.html");
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		        
		        String rVersion = in.readLine();
		        
		        in.close();
		        
		        l.write("Got remote version " + rVersion);
		        
		        if (!rVersion.equals(cVersion)) {
		        	l.write("Remote version is different. Going to update.");
		        	shouldUpdate = true;
		        }
			}
			catch (Exception e) {
				l.write("Could not retrieve remote information. Not going to update.");
				shouldUpdate = false;
			}
		}
		
		if (shouldUpdate) {
			l.write("Done checks. Beginning update.");
			
			try {
				File game = l.getFile("game.jar");
				
				try {
					l.write("Beginning connection to server.");
					
					URL url = new URL("http://rpg-core.comule.com/game/game.jar");
				    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				    FileOutputStream fos = new FileOutputStream(game);
				    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				    fos.close();
				    
				    l.write("Download done.");
				    
				    gameExists = true;
				    
				    File oldVFile = l.getFile("version.config");
				    
				    oldVFile.delete();
				    
				    l.write("Deleted old version file.");
				    
				    done = true;
				}
				catch (Exception ex) {
					l.write("Could not download game.");
					ex.printStackTrace();
					
					if (!game.exists()) {
						l.write("No existing copy found. Relaunch with an internet connection.");
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
			l.write("Not going to update.");
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