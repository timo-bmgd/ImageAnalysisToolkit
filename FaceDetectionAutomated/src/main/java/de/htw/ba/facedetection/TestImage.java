package de.htw.ba.facedetection;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Einfacher Container, der ein Integralbild hält und 
 * eine Liste an ROIs (Regions of Interest). Diese Regionen
 * können Gesichter enthalten, müssen aber nicht.
 * 
 * TestImage Objekte dienen als Ground-Truth-Data in
 * Trainings und Testalgorithmen, sollten daher nicht 
 * von außen manipuliert werden. 
 * 
 * @author Nico Hezel
 */
public class TestImage {

	public enum Subject { PERSON, TEAM };
	
	protected int width;
	protected int height;
	protected int[] pixels;
	
	protected HashMap<Rectangle, Boolean> roi;

	public TestImage(int[] pixels, int width, int height) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
		this.roi = new HashMap<Rectangle, Boolean>();
	}
	
	/**
	 * Fügt eine beliebige Regionen auf dem Bild der Liste von ROIs
	 * hinzu. Es kann angegeben werden ob in der Region ein Gesicht 
	 * zu sehen ist.
	 * 
	 * @param rect
	 * @param containsFace
	 */
	private void addRegionOfInterest(Rectangle rect, boolean containsFace) {
		roi.put(rect, containsFace);
	}

	/**
	 * Breite des Testbildes
	 * @return
	 */
	public int[] getPixels() {
		return pixels;
	}
	
	/**
	 * Breite des Testbildes
	 * @return
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Höhe des Testbildes
	 * @return
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Liefert alle REgione auf dennen ein Gesicht zu sehen ist.
	 * 
	 * @return
	 */
	public List<Rectangle> getFaceRectangles() {
		List<Rectangle> result = new ArrayList<Rectangle>();
		for (Entry<Rectangle, Boolean> entry : roi.entrySet()) 
			if(entry.getValue())
				result.add(entry.getKey());
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Liefert alle Regionen auf dennen definitiv kein Gesicht zu sehen ist.
	 * 
	 * @return
	 */
	public List<Rectangle> getNonFaceRectangles() {
		List<Rectangle> result = new ArrayList<Rectangle>();
		for (Entry<Rectangle, Boolean> entry : roi.entrySet()) 
			if(entry.getValue() == false)
				result.add(entry.getKey());
		return Collections.unmodifiableList(result);
	}
	
	
	/**
	 * Berechnet einige Regionen in den kein Gesicht vorkommt und
	 * fügt diese den ROIs des TestImage hinzu.
	 * 
	 * @param additionalRegionCount
	 */
	public void addNonFaceRegions(int additionalRegionCount) {
		
		long start = System.currentTimeMillis();	
		
		// berechne die durchschnittliche Größe von einem Gesicht
		// und lösche alle nicht Gesichtregionen
		int avgImageWidth = 0, avgImageHeight = 0, faceCount = 0;
		Iterator<Map.Entry<Rectangle, Boolean>> it = roi.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Rectangle, Boolean> entry = it.next();
			if(entry.getValue()) {
				Rectangle faceRegion = entry.getKey();
				avgImageWidth += faceRegion.getWidth();
				avgImageHeight += faceRegion.getHeight();
				faceCount++;
			} else {
				it.remove();
			}
		}
		avgImageWidth /= faceCount;
		avgImageHeight /= faceCount;
		
		// erstelle zufällige Regionen
    	int count = 0, tryCount = 0;
    	Random rnd = new Random(7);
    	while(count < additionalRegionCount && tryCount < 100) {
    		
    		// nur Rechtecke im Bild
    		Rectangle newRect = new Rectangle(rnd.nextInt(getWidth()-avgImageWidth), rnd.nextInt(getHeight()-avgImageHeight), avgImageWidth, avgImageHeight);
    		
    		if(intersectsWith(newRect, getFaceRectangles(), 0.05f)) {
    			tryCount++;
    			continue;
    		}
    		
    		if(intersectsWith(newRect, getNonFaceRectangles(), 0.8f)) {
    			tryCount++;
    			continue;
    		}
    		
    		addRegionOfInterest(newRect, false);
    		count++;
    		tryCount=0;
    	}

		System.out.println("Created "+count+" additional regions in "+(System.currentTimeMillis()-start)+"ms");
	}
	
	/**
	 * Überschneidet sich das Rechteck r1 mit irgendein Rechteckt 
	 * in der Liste rctList zu mindesten minCoextensive
	 * 
	 * @param r1
	 * @param rctList
	 * @param minCoextensive
	 * @return
	 */
    private boolean intersectsWith(Rectangle r1, List<Rectangle> rctList, float minCoextensive) {
    	for (Rectangle rectangle : rctList) 
			if(intersectsWith(r1, rectangle, minCoextensive))
				return true;
		
    	return false;
    }
 
    /**
 	 * Überschneidet sich das Rechteck r1 mit r2 zu mindesten minCoextensive
 	 * 
     * @param r1
     * @param r2
     * @param minCoextensive
     * @return
     */
    private boolean intersectsWith(Rectangle r1, Rectangle r2, float minCoextensive) {
    	
    	Rectangle2D intersectRect = r1.createIntersection(r2);
    	float intersectArea = calcArea(intersectRect);
    	float rectArea = calcArea(r2);
    	float regionArea = calcArea(r1);
		
    	return (intersectArea > 0 && intersectArea/regionArea > minCoextensive && intersectArea/rectArea > minCoextensive);
    }
    
    /**
     * Flächeninhalt von rect
     * 
     * @param rect
     * @return
     */
    private static float calcArea(Rectangle2D rect) {
    	return (rect.getWidth() < 0 || rect.getHeight() < 0) ? 0 : (float)(rect.getWidth() * rect.getHeight());
    }
	
    
    /**
     * Durchschnitte größes eines Gesichtes auf dem Testbild
     * 
     * @return
     */
    public Rectangle getAverageFaceDimensions() {
		int avgFaceWidth = 0, avgFaceHeight = 0, faceCount = 0;
		for (Rectangle faceRegion : getFaceRectangles()) {
			avgFaceWidth += faceRegion.getWidth();
			avgFaceHeight += faceRegion.getHeight();
			faceCount++;
		}
		
		// nur max 80% der Größe
		avgFaceWidth /= faceCount;
		avgFaceHeight /= faceCount;		
		return new Rectangle(0,0,avgFaceWidth,avgFaceHeight);
    }
	
	/**
	 * Liefert ein TestBild mit den entsprechenden Regionen 
	 * 
	 * @param ii IntegralImage für das Fussballteam Bild
	 * @return
	 */
	private static TestImage createSoccerTestImage(int[] pixels, int width, int height) {
	    TestImage ti = new TestImage(pixels, width, height);
	    	
	    ti.addRegionOfInterest(new Rectangle(22,60,30,36), true);
	    ti.addRegionOfInterest(new Rectangle(154,58,28,34), true);
	    ti.addRegionOfInterest(new Rectangle(282,38,28,34), true);
	    ti.addRegionOfInterest(new Rectangle(399,38,28,34), true);
	    ti.addRegionOfInterest(new Rectangle(522,57,28,34), true);
	    ti.addRegionOfInterest(new Rectangle(643,35,29,34), true);
	    	
	    ti.addRegionOfInterest(new Rectangle(79,181,30,34), true);
	    ti.addRegionOfInterest(new Rectangle(230,170,28,34), true);
	    ti.addRegionOfInterest(new Rectangle(375,162,28,33), true);
	    ti.addRegionOfInterest(new Rectangle(496,157,29,32), true);
	    ti.addRegionOfInterest(new Rectangle(650,186,30,35), true);
	    	
	    ti.addRegionOfInterest(new Rectangle(84,263,30,33), true);
	    ti.addRegionOfInterest(new Rectangle(207,285,30,34), true);
	    ti.addRegionOfInterest(new Rectangle(341,267,29,35), true);
	    ti.addRegionOfInterest(new Rectangle(472,247,29,37), true);
	    ti.addRegionOfInterest(new Rectangle(615,279,29,34), true);
	    	
	    ti.addRegionOfInterest(new Rectangle(130,167,30,34), false);
	    ti.addRegionOfInterest(new Rectangle(205,420,28,34), false);
	    ti.addRegionOfInterest(new Rectangle(335,141,28,33), false);
	    ti.addRegionOfInterest(new Rectangle(10,127,29,32), false);
	    ti.addRegionOfInterest(new Rectangle(670,226,30,35), false);
	    ti.addRegionOfInterest(new Rectangle(670,376,30,35), false);
	    	
	    ti.addRegionOfInterest(new Rectangle(59,10,30,34), false);
	    ti.addRegionOfInterest(new Rectangle(277,112,27,36), false);
	    ti.addRegionOfInterest(new Rectangle(460,180,31,35), false);
	    ti.addRegionOfInterest(new Rectangle(360,330,29,33), false);
	    ti.addRegionOfInterest(new Rectangle(200,135,25,31), false);
	    	
	    ti.addRegionOfInterest(new Rectangle(4,317,25,32), false);
	    ti.addRegionOfInterest(new Rectangle(340,450,28,34), false);
	    ti.addRegionOfInterest(new Rectangle(520,450,28,33), false);
	    ti.addRegionOfInterest(new Rectangle(595,145,29,33), false);
	    ti.addRegionOfInterest(new Rectangle(500,210,30,35), false);
	    	
	    return ti;
	}
	
//	/**
//	 * Matt Damon
//	 * 
//	 * @param pixels
//	 * @param width
//	 * @param height
//	 * @return
//	 */
//	private static TestImage createDamonTestImage(int[] pixels, int width, int height) {
//	    TestImage ti = new TestImage(pixels, width, height);
//	    
//	    ti.addRegionOfInterest(new Rectangle(170,65,100,140), true);
//    	
//	    return ti;
//	}
//	
//	private static TestImage createJolieTestImage(int[] pixels, int width, int height) {
//	    TestImage ti = new TestImage(pixels, width, height);
//	    
//	    ti.addRegionOfInterest(new Rectangle(115,100,150,215), true);
//    	
//	    return ti;
//	}
	
	private static TestImage createJolie2TestImage(int[] pixels, int width, int height) {
	    TestImage ti = new TestImage(pixels, width, height);
	    
	    ti.addRegionOfInterest(new Rectangle(227,62,143,185), true);
    	
	    return ti;
	}
	

	
	public static TestImage createTestImage(Subject subject) throws IOException {
		
		// lese das Bild aus
		File file = new File(""+subject.toString().toLowerCase()+".jpg");
		BufferedImage image = ImageIO.read(file);
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width*height];
		image.getRGB(0, 0, width, height, pixels, 0, width);
				
		// entscheide welche Regionen geladen werden müssen
		switch (subject) {
			case TEAM:
				return createSoccerTestImage(pixels, width, height);	
			case PERSON:
				return createJolie2TestImage(pixels, width, height);
			default:
				return null;
		}
	}
}
