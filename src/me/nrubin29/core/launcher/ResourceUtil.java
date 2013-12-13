package me.nrubin29.core.launcher;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ResourceUtil {
	
	public static URL getResource(String path) {
		return Launcher.class.getResource("/res/" + path);
	}
	
	public static InputStream getResourceAsStream(String path) {
		return Launcher.class.getResourceAsStream("/res/" + path);
	}
	
	public static BufferedImage getBufferedImage(String imagePath) {
		try { return ImageIO.read(getResource(imagePath + ".png")); }
        catch (Exception e) { return null; }
	}

    public static ImageIcon getImage(String imagePath) {
        return new ImageIcon(getBufferedImage(imagePath));
    }
    
    public static ImageIcon getImage(String imagePath, int width, int height) {
    	return resizeImage(getImage(imagePath), width, height);
    }
    
    public static ImageIcon getImageScaled(String imagePath, int widthX, int heightX) {
    	ImageIcon icon = getImage(imagePath);
    	return resizeImage(icon, icon.getIconWidth() / widthX, icon.getIconHeight() / heightX);
    }

	public static ImageIcon resizeImage(ImageIcon image, int width, int height) {
		return new ImageIcon(image.getImage().getScaledInstance(width, height, 0));
	}
	
	public static ImageIcon resizeImageScaled(ImageIcon image, int widthX, int heightX) {
		return resizeImage(image, image.getIconWidth() / widthX, image.getIconHeight() / heightX);
	}
}