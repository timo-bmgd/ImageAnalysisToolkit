package de.htw.ba.ue04.facedetection;

import de.htw.ba.facedetection.IntegralImage;
import de.htw.ba.facedetection.WeakClassifier;

import java.awt.*;
import java.sql.SQLOutput;

public class VerticalWeakClassifier extends BaseWeakClassifier implements WeakClassifier {

	private double threshold;
	private double weight;

	/* Local position within the StrongClassifier */
	private int xOffset;
	private int yOffset;
	private int width;
	private int height;

	public VerticalWeakClassifier(int xOffset,int yOffset,int width,int height){
		if (xOffset < 0 || yOffset < 0) {
			throw new IllegalArgumentException("Wrong offset");
		}
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.width = width;
		this.height = height;
	}

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
	public WeakMatchingResult matchingAt(IntegralImage image, int x, int y) {

		WeakMatchingResult result = new WeakMatchingResult();

		int evenHeight = height/2;

		double green = image.meanValue(x+xOffset,y+yOffset,width,evenHeight);
		double red = image.meanValue(x+xOffset,y+yOffset + evenHeight,width,evenHeight);

		double max = 255;
		double min = 0;

		result.featureValue = (((green-red))/max);
		// 0 255 -> -1
		// 255 0 -> 1

		if(result.featureValue>1 || result.featureValue<-1){
			System.out.println("");
		}

		System.out.println(result.featureValue);

		return result;
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
	public void drawAt(Graphics2D g2d, int x, int y) {
		g2d.setColor(Color.GREEN);
		g2d.fillRect(x+xOffset,y+yOffset,width,height/2);
		g2d.setColor(Color.RED);
		g2d.fillRect(x+xOffset,y+yOffset + height/2,width,height/2);
	}
}
