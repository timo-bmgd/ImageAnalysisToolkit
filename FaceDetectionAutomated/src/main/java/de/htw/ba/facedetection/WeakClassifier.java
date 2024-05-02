/**
 * Copyright 2016 by Klaus Jung. All right reserved.
 * 
 * @author Klaus Jung
 */
package de.htw.ba.facedetection;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * A Weak Classifier compares its internal pattern with the pattern found at the given image 
 * 
 * @author Klaus Jung
 */
public interface WeakClassifier {
	
	/**
	 * Class to return matching results in {@link #featureValueAt(IntegralImage, int, int) featureValueAt}
	 */
	public class WeakMatchingResult {
		public double featureValue = 0;
		public boolean isDetected = false;
	}
	
	/**
	 * Return the bounding box of its internal patterns relative to the
	 * coordinate system of a detector region.
	 * 
	 * @return the position of its internal pattern
	 */
	public Rectangle getPositionInDetector();
	
	/**
	 * Calculate the feature value by comparing the classifier's internal 
	 * pattern with the given image at detector position (x, y). The
	 * feature value should be in the interval [-1 , 1].
	 * 
	 * Calculate the matching by comparing the feature value with the
	 * classifier's threshold. True if the pattern matches, false otherwise.
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @return Matching result with featureValue in [-1 , 1] and isDetected
	 * true if the pattern matches.
	 */
	public WeakMatchingResult matchingAt(IntegralImage image, int x, int y);
	
	/**
	 * Set a new threshold used to calculate a match by comparing
	 * the feature value with the given threshold.
	 * 
	 * @param threshold
	 */
	public void setThreshold(double threshold);
	
	/**
	 * Return the current threshold value.
	 * 
	 * @return the feature value
	 */
	public double getThreshold();
	
	/**
	 * Set a weight "alpha" used when combining several weak classifiers
	 * to a strong classifier
	 * 
	 * @param weight
	 */
	public void setWeight(double weight);
	
	/**
	 * Return the current weight,
	 * 
	 * @return
	 */
	public double getWeight();
	
	/**
	 * Draw the internal patterns using two different colors for regions
	 * positive counting regions and and negative counting regions at
	 * position (x, y) of the detector.
	 *  
	 * @param g2d
	 * @param x
	 * @param y
	 */
	public void drawAt(Graphics2D g2d, int x, int y);

}
