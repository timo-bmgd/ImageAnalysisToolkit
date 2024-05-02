package de.htw.ba.facedetection;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dient zur Bewertung von Facedetection Klassifizierer.
 * Überprüft die gefunden ROIs.
 * 
 * @author Nico
 */
public class Evaluation {

	protected static float minCoextensive = 0.6f;
	
	/**
	 * Gibt an ob in der Region ein Gesicht ist.
	 * 
	 * @param regionOfInterest
	 * @param image
	 * @param minCoextensive
	 * @return
	 */
	public static boolean isFace(Rectangle regionOfInterest, TestImage image) {
		return getStrongIntersectingRectangle(regionOfInterest, image.getFaceRectangles(), minCoextensive).getWidth() > 0;
	}
	
	/**
	 * Liefert eine Wahrheitsmatrix an die angibt wieviele der 
	 * regionsOfInterest ein Gesicht getroffen haben und wieviele nicht. 
	 * 
	 * Der minCoextensive Faktor gibt an zu wieviel Prozent sich die beiden
	 * Regionen (Gesicht vs ROI) überschneiden müssen.
	 * 
	 * @param regionsOfInterest
	 * @param image
	 * @param minCoextensive zwischen 0 und 1
	 * @return { truePositiveCount, falsePositiveCount, trueNegativeCount, falseNegativeCount, unknown }
	 */
	public static void evaluate(List<Rectangle> regionsOfInterest, TestImage image) {
 	
		if (regionsOfInterest == null)
			return;
		
	  	// wieviele Bilder wurden erkannt
    	List<Rectangle> faces = image.getFaceRectangles();
      	List<Rectangle> nonFaces = image.getNonFaceRectangles();
      	
      	Set<Rectangle2D> foundUniqueFaces = new HashSet<Rectangle2D>();
      	Set<Rectangle2D> foundUniqueNonFaces = new HashSet<Rectangle2D>();
      	
      	int unknown = 0;
      	
    	// durchlaufe alle gefundenen Punkte
		for (Rectangle region : regionsOfInterest) {

			Rectangle bestNonFace = getStrongIntersectingRectangle(region, nonFaces, minCoextensive);
			Rectangle2D nonFaceIntersectRect = region.createIntersection(bestNonFace);
			float nonFaceArea = calcArea(nonFaceIntersectRect);
			
			Rectangle bestFace = getStrongIntersectingRectangle(region, faces, minCoextensive);
			Rectangle2D faceIntersectRect = region.createIntersection(bestFace);
			float faceArea = calcArea(faceIntersectRect);
			
		

			if(faceArea < nonFaceArea) {
				foundUniqueNonFaces.add(bestNonFace);
			} else if(faceArea > nonFaceArea) {
				foundUniqueFaces.add(bestFace);
			} else {
				unknown++;
//				falsePositiveCount++; // Gesicht gefunden, dort ist aber keins
			}
		}

		int falsePositiveCount = foundUniqueNonFaces.size();
		int truePositiveCount = foundUniqueFaces.size();
		int trueNegativeCount = nonFaces.size() - foundUniqueNonFaces.size(); 
		int falseNegativeCount = faces.size() - foundUniqueFaces.size(); 

		
		System.out.println("\nEvaluation");
		System.out.println("True Positive: "+truePositiveCount);
		System.out.println("False Negative: "+falsePositiveCount);
		System.out.println("True Negative: "+trueNegativeCount);
		System.out.println("False Positive: "+falseNegativeCount);
		System.out.println("Unknowns: "+unknown);
	}

	
	private static Rectangle getStrongIntersectingRectangle(Rectangle region, List<Rectangle> rects, float minCoextensive) {
		float biggestArea = 0;
		Rectangle strongest = new Rectangle(0,0,0,0);
		float regionArea = calcArea(region);
		
		for (Rectangle rect : rects) {
			Rectangle2D intersectRect = region.createIntersection(rect);
			float intersectArea = calcArea(intersectRect);
			float rectArea = calcArea(rect);
			
			if(intersectArea / (regionArea + rectArea - intersectArea) > minCoextensive) {
				if((biggestArea == 0 || biggestArea < intersectArea)) {
					biggestArea = intersectArea;
					strongest = rect;
				}
			}
		}

		return strongest;
	}
	
    private static float calcArea(Rectangle2D rect) {
    	return (rect.getWidth() < 0 || rect.getHeight() < 0) ? 0 : (float)(rect.getWidth() * rect.getHeight());
    }
}