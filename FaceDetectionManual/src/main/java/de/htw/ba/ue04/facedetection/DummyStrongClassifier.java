package de.htw.ba.ue04.facedetection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import de.htw.ba.facedetection.IntegralImage;
import de.htw.ba.facedetection.StrongClassifier;
import de.htw.ba.facedetection.WeakClassifier;

public class DummyStrongClassifier implements StrongClassifier {

	private Dimension size = new Dimension();
	private double threshold;
	private List<WeakClassifier> weakClassifierList = new ArrayList<>();
	
	public DummyStrongClassifier(int width, int height) {
		size = new Dimension(width, height);
		addWeakClassifier(new VerticalWeakClassifier(14,31,105,57));
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
		weakClassifierList.add(classifier);
	}

	@Override
	public void normalizeWeights() {
		double sum = 0;
		for (WeakClassifier c: weakClassifierList ) {
			sum += c.getWeight();
		}
		for (WeakClassifier c: weakClassifierList ) {
			c.setWeight(c.getWeight()/sum);
		}
	}

	@Override
	public MatchingResult matchingAt(IntegralImage image, int x, int y) {
		double sum = 0;
		for (WeakClassifier c: weakClassifierList ) {
			sum += c.matchingAt(image,x,y).featureValue * c.getWeight();
		}
		MatchingResult result = new MatchingResult();
		result.featureValue = sum;
		return result;
	}

	@Override
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public double getThreshold() {
		return threshold;
	}

	@Override
	public void drawAt(Graphics2D g2d, int x, int y) {
		g2d.setColor(Color.WHITE);
		g2d.drawRect(x,y,getSize().width,getSize().height);
		for (WeakClassifier c: weakClassifierList ) {
			c.drawAt(g2d,x,y);
		}
	}

}
