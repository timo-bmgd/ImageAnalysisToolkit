package de.htw.ba.ue04.facedetection;

import de.htw.ba.facedetection.IntegralImage;
import de.htw.ba.facedetection.WeakClassifier;

import java.awt.*;

public abstract class BaseWeakClassifier implements WeakClassifier {

	private double threshold;
	private double weight;


	/**
	 * Information: Wir nehmen mehrere Klassen für verschiedene WeakPatterns
	 */
	/**
	 * Calculate the feature value by comparing the classifier's internal
	 * pattern with the given image at detector position (x, y). The
	 * feature value should be in the interval [-1 , 1].
	 * <p>
	 * Calculate the matching by comparing the feature value with the
	 * classifier's threshold. True if the pattern matches, false otherwise.
	 *
	 * @param image
	 * @param x
	 * @param y
	 * @return Matching result with featureValue in [-1 , 1] and isDetected
	 * true if the pattern matches.
	 */
	@Override
	public abstract WeakMatchingResult matchingAt(IntegralImage image, int x, int y);

	/**
	 * Set a new threshold used to calculate a match by comparing
	 * the feature value with the given threshold.
	 *
	 * @param threshold
	 */
	@Override
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Return the current threshold value.
	 *
	 * @return the feature value
	 */
	@Override
	public double getThreshold() {
		return threshold;
	}

	/**
	 * Set a weight "alpha" used when combining several weak classifiers
	 * to a strong classifier
	 *
	 * @param weight
	 */
	@Override
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * Return the current weight,
	 *
	 * @return
	 */
	@Override
	public double getWeight() {
		return this.weight;
	}

	/**
	 * Draw the internal patterns using two different colors for regions
	 * positive counting regions and negative counting regions at
	 * position (x, y) of the detector.
	 *
	 * @param g2d
	 * @param x
	 * @param y
	 */
	@Override
	public abstract void drawAt(Graphics2D g2d, int x, int y);
}
