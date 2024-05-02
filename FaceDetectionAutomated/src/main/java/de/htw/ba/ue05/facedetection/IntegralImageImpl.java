/*
 * Source code from Nico Hezel: Originally class ImageIntegral
 */

package de.htw.ba.ue05.facedetection;

import de.htw.ba.facedetection.IntegralImage;

public class IntegralImageImpl implements IntegralImage {


	private long[] integrals;
	private int width;
	private int height;
	
	
	public IntegralImageImpl(int[] values, int width, int height) {
		this.width = width;
		this.height = height;
		
		calc(values, width, height);
	}
    
	private void calc(int[] values, int width, int height) {
  		integrals = new long[values.length];
  	  	
		for (int y = 0; y < height; y++) 
		{
			long currentLineSum = 0;
			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x;
				
				// berechne die aktuellen Integralwert
				long value = currentLineSum += values[pos];
				if(y > 0)
					value += integrals[(y-1) * width + x];
				
				// speichere den errechneten Wert
				integrals[pos] = value;
			}
		}
	}
	
	@Override
	public void toIntARGB(int[] pixels) {
		double highestValue = integrals[integrals.length-1];
    	for (int pos = 0; pos < integrals.length; pos++) {
    		int grey = (int)(integrals[pos] / highestValue * 255.0);
    		pixels[pos] = (0xFF << 24) | (grey << 16) | (grey << 8) | grey;
       }
	}

	private long get(int x, int y) {
		
		// Randwiederholung
		x = Math.max(0, Math.min(x, width-1));
		y = Math.max(0, Math.min(y, height-1));
		
		// Rand mit 0 auffüllen
//		int pos = (y * width + x);
//		if(integrals.length-1 < pos || pos < 0)
//			return 0;
		
		return integrals[y * width + x];
	}

//	private int getAreaInImage(int x, int y, int width, int height) {
//		
//		int xBR = Math.max(0, Math.min(x+width, getWidth()-1));
//		int yBR = Math.max(0, Math.min(y+height, getHeight()-1));
//		int xTL = Math.max(0, Math.min(x, getWidth()-1));
//		int yTL = Math.max(0, Math.min(y, getHeight()-1));
//		
//		return (xBR-xTL)*(yBR-yTL);
//	}
	
	@Override
	public double meanValue(int x, int y, int width, int height) {
		
		long area1 = get(x, y);
		long area2 = get(x + width, y);
		long area3 = get(x, y + height);
		long area4 = get(x + width, y + height);
		
		double result = area4 - area3 - area2 + area1;
		int pixelCount = height * width;
//		int pixelCount = getAreaInImage(x, y, width, height);
		
		return result / pixelCount;    	 
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
