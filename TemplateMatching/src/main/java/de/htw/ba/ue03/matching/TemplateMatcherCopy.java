package de.htw.ba.ue03.matching;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Erstellt nur eine Kopie von dem Template Bild
 * 
 * @author Nico
 *
 */
public class TemplateMatcherCopy extends TemplateMatcherBase {

	public TemplateMatcherCopy(int[] templatePixel, int templateWidth, int templateHeight) {
		super(templatePixel, templateWidth, templateHeight);
	}
	
	@Override
	public double[][] getDistanceMap(int[] srcPixels, int srcWidth, int srcHeight) {
		return new double[templateWidth][templateHeight];

		//TODO: loop over srcPixels-dstPixels
		// For every pixel, check

	}

	@Override
	public void distanceMapToIntARGB(double[][] distanceMap, int[] dstPixels, int dstWidth, int dstHeight) {
		for (int y = 0; y < templateHeight; y++) {
			for (int x = 0; x < templateWidth; x++) {
				int templatePosition = y * templateWidth + x;
				int dstPosition = y * dstWidth + x;


				//TODO: get max from distance map
				//TODO: normalize the pixels from the distance map
				//TODO: pixels onto argb

				dstPixels[dstPosition] = templatePixel[templatePosition];
			}
		}
	}

	@Override
	public List<Point> findMaximas(double[][] distanceMap) {
		return new ArrayList<>();
	}	
}
