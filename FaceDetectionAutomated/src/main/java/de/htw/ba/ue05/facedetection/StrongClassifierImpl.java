/**
 * Copyright 2016 by Klaus Jung. All right reserved.
 * 
 * @author Klaus Jung
 */
package de.htw.ba.ue05.facedetection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;

import de.htw.ba.facedetection.IntegralImage;
import de.htw.ba.facedetection.StrongClassifier;
import de.htw.ba.facedetection.WeakClassifier;

public class StrongClassifierImpl implements StrongClassifier {

	private Dimension size = new Dimension();
	private ArrayList<WeakClassifier> weakClassifier = new ArrayList<WeakClassifier>();
	private double threshold = 0.5;
	private double featureValueFactor = 1.0;
	
	public static boolean detectWithWeakClassifiersFeatureValues = true;
	
	public StrongClassifierImpl(Dimension size) {
		this.size = size;
	}

	@Override
	public void setSize(Dimension size) {
		this.size = size;
	}

	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public void addWeakClassifier(WeakClassifier classifier) {
		weakClassifier.add(classifier);
	}
	
	public int getWeakClassifierCount() {
		return weakClassifier.size();
	}

	@Override
	public void normalizeWeights() {
		double sum = 0;
		for(WeakClassifier classifier : weakClassifier) {
			sum += classifier.getWeight();
		}
		for(WeakClassifier classifier : weakClassifier) {
			classifier.setWeight(classifier.getWeight() / sum);
		}
	}

	@Override
	public MatchingResult matchingAt(IntegralImage image, int x, int y) {
		MatchingResult result = new MatchingResult();
		double value = 0;
		for(WeakClassifier classifier : weakClassifier) {
			WeakClassifier.WeakMatchingResult weakResult = classifier.matchingAt(image, x, y);
			if(detectWithWeakClassifiersFeatureValues)
				value += classifier.getWeight() * weakResult.featureValue;
			else
				value += classifier.getWeight() * (weakResult.isDetected ? 1 : 0);
		}
		value *= featureValueFactor;
		result.featureValue = value;
		result.isDetected = value > threshold;
		return result;
	}

	@Override
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public void setFeatureValueFactor(double factor) {
		this.featureValueFactor = factor;
	}

	@Override
	public double getThreshold() {
		return threshold;
	}

	@Override
	public void drawAt(Graphics2D g2d, int x, int y) {
		for(WeakClassifier classifier : weakClassifier) {
			classifier.drawAt(g2d, x, y);
		}
		g2d.setColor(Color.white);
		g2d.drawRect(x, y, size.width, size.height);
	}

}
