package de.htw.ba.ue03.matching;

import java.awt.Point;
import java.util.List;

public interface TemplateMatcher {

	int getTemplateWidth();
	int getTemplateHeight();
	
	/**
	 * Gibt die matching Distanz zwischen dem Template und dem Bild an jeder Position zurück.
	 * Die zurück gelieferte Map ist [srcWidth-templateWidth][srcHeight-templateHeight] groß.
	 * 
	 * @param srcPixels
	 * @param srcWidth
	 * @param srcHeight
	 * @return
	 */
	double[][] getDistanceMap(int[] srcPixels, int srcWidth, int srcHeight);
	
	/**
	 * Wandelt die DistanceMap zu einem Graustufenbild um
	 * 
	 * @param distanceMap
	 * @param dstPixels
	 * @return
	 */
	void distanceMapToIntARGB(double[][] distanceMap, int[] dstPixels, int dstWidth, int dstHeight);
	
	/**
	 * Gibt an Liste an Lokalen Maximas zurück.
	 * 
	 * @param distanceMap
	 * @return
	 */
	List<Point> findMaximas(double[][] distanceMap);
}
