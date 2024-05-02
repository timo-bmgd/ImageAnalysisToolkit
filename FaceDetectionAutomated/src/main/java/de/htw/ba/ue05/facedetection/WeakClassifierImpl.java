/**
 * Copyright 2016 by Klaus Jung. All right reserved.
 * 
 * @author Klaus Jung
 */
package de.htw.ba.ue05.facedetection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import de.htw.ba.facedetection.IntegralImage;
import de.htw.ba.facedetection.WeakClassifier;

public class WeakClassifierImpl implements WeakClassifier {
	
	public enum Type { Double, Tripple, Diagonal };
	public enum Orientation { Horizontal, Vertical };
	public enum Mode { Normal, Inverted };

	private Rectangle position = new Rectangle();
	private ArrayList<Rectangle> posRects = new ArrayList<Rectangle>();
	private ArrayList<Rectangle> negRects = new ArrayList<Rectangle>();
	private double threshold = 0.5;
	private double weight = 1.0;
	
	public WeakClassifierImpl(Rectangle position) {
		this.position = position;
	}
	
	public void addAdditiveRect(Rectangle rect) {
		posRects.add(rect);
	}
	
	public void addSubstractiveRect(Rectangle rect) {
		negRects.add(rect);
	}
	
	public WeakClassifierImpl(Rectangle position, Type type, Orientation orientation, Mode mode) {
		this.position = position;
		int dx = position.width;
		int dy = position.height;
		switch(orientation) {
		case Horizontal:
			switch(type) {
			case Double:
				dy /= 2;
				break;
			case Tripple:
				dy /= 3;
				break;
			case Diagonal:
				dx /= 2;
				dy /= 2;
				break;
			}
			break;
		case Vertical:
			switch(type) {
			case Double:
				dx /= 2;
				break;
			case Tripple:
				dx /= 3;
				break;
			case Diagonal:
				dx /= 2;
				dy /= 2;
				break;
			}
			break;
		}
		boolean positiveStart = mode == Mode.Normal ? true : false;
		for(int y = 0; y <= position.height - dy; y += dy) {
			boolean isPositive = positiveStart;
			for(int x = 0; x <= position.width - dx; x += dx) {
				ArrayList<Rectangle> rects = isPositive ? posRects : negRects;
				rects.add(new Rectangle(position.x + x, position.y + y, dx, dy));
				isPositive = !isPositive;
			}
			positiveStart = !positiveStart;
		}
		// weight it proportional to its area
		//weight = position.width * position.height;
	}

	@Override
	public Rectangle getPositionInDetector() {
		return position;
	}

	@Override
	public WeakMatchingResult matchingAt(IntegralImage image, int x, int y) {
		WeakMatchingResult result = new WeakMatchingResult();
		double posSum = 0;
		for(Rectangle rect : posRects) {
			posSum += image.meanValue(rect.x + x, rect.y + y, rect.width, rect.height);
		}
				
		double negSum = 0;
	    for(Rectangle rect : negRects) {
			negSum += image.meanValue(rect.x + x, rect.y + y, rect.width, rect.height);
	    }
	    	
	    posSum /= posRects.size();
	    negSum /= negRects.size();
	    double value = (posSum - negSum) / 255;
	    result.featureValue = value;
	    result.isDetected = value > threshold;
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
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void modifyWeight(double factor) {
		weight *= factor;
	}

	@Override
	public double getWeight() {
		return weight;
	}

	@Override
	public void drawAt(Graphics2D g2d, int x, int y) {
		g2d.setColor(new Color(0x8000ff00, true));
		for(Rectangle rect : posRects) {
			g2d.fillRect(rect.x + x, rect.y + y, rect.width, rect.height);
		}
		g2d.setColor(new Color(0x80ff0000, true));
		for(Rectangle rect : negRects) {
			g2d.fillRect(rect.x + x, rect.y + y, rect.width, rect.height);
		}
	}

}
