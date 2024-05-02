package de.htw.ba.model;

import java.nio.file.Path;

import javafx.scene.image.Image;

/**
 * Simple POJO (Plain old Java object) for holding information about an image file.
 * 
 * @author Nico Hezel
 */
public class ImageData {

	protected final String name;	
	protected final Path filePath;
	protected final Image image;
	
	protected float[] histogram;

	public ImageData(String name, Path filePath, Image image) {
		this.name = name;
		this.filePath = filePath;
		this.image = image;
	}

	public String getName() {
		return name;
	}
	
	public Path getFilePath() {
		return filePath;
	}
	
	public Image getImage() {
		return image;
	}
	
	public void setHistogram(float[] histogram) {
		this.histogram = histogram;
	}
	
	public float[] getHistogram() {
		return histogram;
	}
	
	@Override
	public String toString() {
		return ImageData.class.getSimpleName()+" for "+name;
	}
}