/**
 * Copyright 2016 by Klaus Jung. All right reserved.
 * 
 * @author Klaus Jung
 */
package de.htw.ba.facedetection;

import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * A Strong Classifier combines multiple Weak Classifier to a compound detector 
 * 
 * @author Klaus Jung
 */
public interface StrongClassifier {
	
	/**
	 * Class to return matching results in {@link #featureValueAt(IntegralImage, int, int) featureValueAt}
	 */
	public class MatchingResult {
		public double featureValue = 0;
		public boolean isDetected = false;
	}
	
	/**
	 * Set a new size of the detector by specifying its width and height.
	 * 
	 * @param size
	 */
	public void setSize(Dimension size);
	
	/**
	 * Get the current detector size.
	 * 
	 * @return
	 */
	public Dimension getSize();
	
	/**
	 * Add a Weak Classifier to the internal list.
	 */
	public void addWeakClassifier(WeakClassifier classifier);
	
	/**
	 * Normalize the weights of all added Weak Classifiers. This
	 * is typically called after all Weak Classifiers has been added.
	 * An implementation should divide each Weak Classifiers's weights
	 * by the sum of each such weights.
	 */
	public void normalizeWeights();
	
	/**
	 * Calculate the feature value for the given image at detector 
	 * position (x, y). Uses the contained Weak Classifiers to calculate 
	 * the feature value. 
	 * 
	 * Calculate the matching by comparing the feature value with the
	 * classifier's threshold. True if the pattern matches, false otherwise.
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @return Matching result with featureValue in [-1 , 1] and isDetected
	 * true if the classifier matches.
	 */
	public MatchingResult matchingAt(IntegralImage image, int x, int y);

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
	 * @return
	 */
	public double getThreshold();
	
	/**
	 * Draw the detector at position (x, y), visualizing the
	 * detector's region and its contained Weak Classifiers
	 * internal patterns. 
	 *  
	 * @param g2d
	 * @param x
	 * @param y
	 */
	public void drawAt(Graphics2D g2d, int x, int y);

}
