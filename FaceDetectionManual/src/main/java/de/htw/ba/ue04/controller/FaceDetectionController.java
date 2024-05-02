/**
 * @author Nico Hezel, Klaus Jung
 */
package de.htw.ba.ue04.controller;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;

import de.htw.ba.facedetection.StrongClassifier.MatchingResult;
import de.htw.ba.ue04.facedetection.EasyIntegralImage;
import de.htw.ba.ue04.facedetection.DummyStrongClassifier;

public class FaceDetectionController extends FaceDetectionBase {

	@Override
	protected void calculateIntegralImage(int[] srcPixels, int width, int height) {
		System.out.println("calculateIntegralImage");
		
		// TODO: Replace DummyIntegralImage by your own implementation of the 
		// IntegralImage interface (in a separate file).
		
		// TODO: Calculate the integral image for the given source image pixels.
		integralImage = new EasyIntegralImage(srcPixels, width, height);
	}

	@Override
	protected void createManualClassifier() {
		System.out.println("createManualClassifier");

		// Average face size to be used for the classifier's detector size 
		Rectangle avgFace = testImage.getAverageFaceDimensions();
		
		// TODO: Replace DummyStrongClassifier by your own implementation of the 
		// StrongClassifier interface (in a separate file).
		
		// TODO: Create an implementation of the WeakClassifier interface (in a separate file).
		
		// TODO: Create an instance of your strong classifier and add some
		// manually created weak classifiers.
		
		strongClassifier = new DummyStrongClassifier(avgFace.width, avgFace.height);
	}
	
	@Override
	protected void createTrainedClassifier(int weakClassifierCount) {
		System.out.println("createTrainedClassifier");
		
		// Average face size to be used for the classifier's detector size 
		Rectangle avgFace = testImage.getAverageFaceDimensions();
		
		// TODO: This is Exercise 5 only! Nothing to do for Exercise 4.
		
		// TODO: Exercise 5: Create a set of random weak classifiers.
		
		// TODO: Exercise 5: Train your weak classifiers with AdaBoost
		// and construct a strong classifier out of the result.
		
		testImage.getFaceRectangles();
		testImage.getNonFaceRectangles();
		
		
		strongClassifier = new DummyStrongClassifier(avgFace.width, avgFace.height);
	}

	

    /**
     * Use strongClassifier to calculate a feature heat map.
     * Store all detected regions in the detectionResult list of rectangles.
     * The use of nonMaxSuppression is optional for the exercise.
	 * 
     * @param featureHeatMapPixels
     * @param width
     * @param height
     * @param threshold
     * @param nonMaxSuppression
     */
	protected void doDetection(int[] featureHeatMapPixels, int width, int height, float threshold, boolean nonMaxSuppression) {
		System.out.println("doDetection");
		
	   	// set current threshold for detection
    	strongClassifier.setThreshold(threshold);
     	
     	// detector size
		Dimension size = strongClassifier.getSize();
		
		double featureValue[] = new double[width * height];
		boolean isDetected[] = new boolean[width * height];

		// for all image positions that fully contain the detector region
     	for(int y = 0; y < height - size.height; y++) {	
			for(int x = 0; x < width - size.width; x++)	{
				int pos = y * width + x;
				
				// calculate feature value and classification result
				MatchingResult result = strongClassifier.matchingAt(integralImage, x, y);
				featureValue[pos] = result.featureValue;
				isDetected[pos] = result.isDetected;
				
				// draw feature map
				int gray = (int)(featureValue[pos] * 255 / threshold); // increase contrast by inverse threshold
				if(gray < 0)
					gray = 0;
				if(gray > 255)
					gray = 255;
				int red = gray;
				if(isDetected[pos]) {
					// colorize detected positions
					red = 255;
					gray = 0;
				}
				
				featureHeatMapPixels[pos] =  (0xFF << 24) | (red << 16) | (gray << 8) | gray;
			}
		}

     	// TODO: Apply a non-maximum suppression (use the featureValue[] data and 
     	// adjust isDetected[] and featureHeatMapPixels[] accordingly). Or change 
     	// the lines above to reach the same goal.
     	
     	detectionResult = new ArrayList<>();
     	
     	// TODO: Store all detected regions in the detectionResult list of rectangles. Each rectangle
     	// should have detector size.
	}
}
