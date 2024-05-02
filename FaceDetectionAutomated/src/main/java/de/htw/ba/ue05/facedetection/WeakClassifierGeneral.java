package de.htw.ba.ue05.facedetection;

	import java.awt.Color;
	import java.awt.Graphics2D;
	import java.awt.Point;
	import java.awt.Rectangle;

	import javax.swing.colorchooser.ColorSelectionModel;

	import org.w3c.dom.css.RGBColor;

	import de.htw.ba.facedetection.IntegralImage;
	import de.htw.ba.facedetection.StrongClassifier;
	import de.htw.ba.facedetection.WeakClassifier;
	import de.htw.ba.facedetection.WeakClassifier.WeakMatchingResult;

public class WeakClassifierGeneral implements WeakClassifier{
			double weight = 1;
			double threshold = 0.8;
			int height;
			int width;
			int x_weak;
			int y_weak;
			Rectangle avgFace;
			boolean horizontal = false;
			boolean leftOrTopBrighter = false; 
			
			public WeakClassifierGeneral(Rectangle avgFace) {
				this.avgFace = avgFace;
				height = generateNumberInRange(1, avgFace.height);
				width  = generateNumberInRange(1, avgFace.width);
				x_weak = generateNumberInRange(1, avgFace.width-width);
				y_weak = generateNumberInRange(1, avgFace.height-height);
				double x = Math.random();
				double y = Math.random();
				if(x >= 0.5) {
					horizontal = true;
				}
				if(y >= 0.5) {
					leftOrTopBrighter = true;
				}
			}
			
			public int generateNumberInRange(int min, int max) {
			    return (int) ((Math.random() * (max - min)) + min);
			}
			
			@Override
			public Rectangle getPositionInDetector() {
				return new  Rectangle(x_weak,y_weak,this.width, this.height);
			}

			@Override
			public WeakMatchingResult matchingAt(IntegralImage image, int x, int y) {
				WeakMatchingResult ret = new WeakMatchingResult();
				double featureValue = 0;
				if(horizontal  && leftOrTopBrighter ) {
					double right = image.meanValue(x + x_weak + width/2, y+y_weak, width/2, height);
					double left = image.meanValue(x + x_weak,y+y_weak, width/2, height);
					double nrElements = 1; // nr of how many areas are there for each color? 
					featureValue = ((left/nrElements)-(right/nrElements))/255.0;
				}
				else if(!horizontal && leftOrTopBrighter) {
					double top = image.meanValue(x + x_weak, y+y_weak, width, height/2);
					double bottom = image.meanValue(x +x_weak, (y+y_weak + height/2-1), width, height/2);
					double nrElements = 1; // nr of how many areas are there for each color? 
					featureValue = ((top/nrElements)-(bottom/nrElements))/255.0;
					ret.featureValue = featureValue;
				}
				else if(horizontal  && !leftOrTopBrighter ) {
					double right = image.meanValue(x + x_weak + width/2, y+y_weak, width/2, height);
					double left = image.meanValue(x + x_weak,y+y_weak, width/2, height);
					double nrElements = 1; // nr of how many areas are there for each color? 
					featureValue = ((right/nrElements)-(left/nrElements))/255.0;
				}
				else if(!horizontal && !leftOrTopBrighter) {
					double top = image.meanValue(x + x_weak, y+y_weak, width, height/2);
					double bottom = image.meanValue(x +x_weak, (y+y_weak + height/2-1), width, height/2);
					double nrElements = 1; // nr of how many areas are there for each color? 
					featureValue = ((bottom/nrElements)-(top/nrElements))/255.0;
					ret.featureValue = featureValue;
				}
				ret.featureValue = featureValue;
				if(featureValue > threshold) ret.isDetected = true;
				else ret.isDetected = false;
				return ret;
			}
			
			@Override
			public void setThreshold(double threshold) {
				this.threshold = threshold;//strong.getThreshold();
			}

			@Override
			public double getThreshold() {
				return threshold;
			}

			@Override
			public void setWeight(double weight) {
				this.weight = weight;
			}

			@Override
			public double getWeight() {
				return weight;
			}

			@Override
			public void drawAt(Graphics2D g2d, int x, int y) {
				if(leftOrTopBrighter) {
			    	g2d.setColor(new Color(0, 255, 0, 90));
			    	g2d.fill(new Rectangle((x_weak + x),(y_weak + y), this.width, this.height/2));
			    	g2d.setColor(new Color(255, 0, 0, 90));
			    	g2d.fill(new Rectangle((x_weak + x) , (y_weak + y + height/2), this.width, this.height/2));
				}	
				else {
			    	g2d.setColor(new Color(255, 0, 0, 90));
			    	g2d.fill(new Rectangle((x_weak + x),(y_weak + y), this.width, this.height/2));
			    	g2d.setColor(new Color(0, 255, 0, 90));
			    	g2d.fill(new Rectangle((x_weak + x) , (y_weak + y + height/2), this.width, this.height/2));
				}
				
				
		}
}
