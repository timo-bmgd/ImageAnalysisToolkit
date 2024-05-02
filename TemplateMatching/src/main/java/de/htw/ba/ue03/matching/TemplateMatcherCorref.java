package de.htw.ba.ue03.matching;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Erstellt nur eine Absolute von dem Template Bild
 * 
 * @author Nico
 *
 */
public class TemplateMatcherCorref extends TemplateMatcherBase {

	public TemplateMatcherCorref(int[] templatePixel, int templateWidth, int templateHeight) {
		super(templatePixel, templateWidth, templateHeight);
	}
	
	@Override
	public double[][] getDistanceMap(int[] srcPixels, int srcWidth, int srcHeight) {
		double[][] distanceMap =  new double[srcWidth-templateWidth][srcHeight-templateHeight];


		for(int x = 0; x < distanceMap.length; x++) {
			for (int y = 0; y < distanceMap[x].length; y++) {
				double distance = 0;

				// Mittelwert des Templates und des Bildes berechnen
				double mittelwert_template = 0;
				double mittelwert_bild = 0;
				for (int tx = 0; tx < templateWidth; tx++) {
					for (int ty = 0; ty < templateHeight; ty++) {
						int pixelTemplate = templatePixel[ty * templateWidth + tx] & 0xFF;
						int pixelImage = srcPixels[(y + ty) * srcWidth + (x + tx)] & 0xFF;
						mittelwert_bild += pixelImage;
						mittelwert_template += pixelTemplate;
					}
				}
				mittelwert_template /= (templateHeight*templateWidth);
				mittelwert_bild /= (templateHeight*templateWidth);

				double upperpart = 0;
				double lowerpartR = 0;
				double lowerpartL = 0;

				//generate distance for current pixel
				for (int tx = 0; tx < templateWidth; tx++) {
					for (int ty = 0; ty < templateHeight; ty++) {
						int pixelImage = srcPixels[(y + ty) * srcWidth + (x + tx)] & 0xFF;
						int pixelTemplate = templatePixel[ty * templateWidth + tx] & 0xFF;
						upperpart += (pixelImage-mittelwert_bild) * (pixelTemplate-mittelwert_template);
						lowerpartR += Math.pow((pixelImage-mittelwert_bild),2);
						lowerpartL += Math.pow((pixelTemplate-mittelwert_template),2);

					}
				}
				lowerpartR = Math.sqrt(lowerpartR);
				lowerpartL = Math.sqrt(lowerpartL);
				distance = upperpart /(lowerpartR*lowerpartL);

				// Mittelwert Bild
				// Mittelwert Bildausschnitt
				// SUMME ( (Bildpixel - Bildmittelwert) * (Tempixelp - Tempmittelwert) )
				// Geteilt durch
				// sqrt( SUMME (Bildpixel - Bildmittelwert)^2)) * sqrt( SUMME (Temppixel - Tempmittelwert)^2))

				distanceMap[x][y] = distance ;
			}
		}

		//TODO: loop over srcPixels-dstPixels
		// For every pixel, check
		return distanceMap;

	}

	private static double getMax(double[][] array){
		double max = Integer.MIN_VALUE;
		for(int j = 0; j<array.length; j++){
			for(int i = 0; i<array[j].length; i++){
				if(array[j][i]>max){
					max = array[j][i];
				}
			}
		}
		return max;
	}
	private static double getMin(double[][] array){
		double min = Integer.MAX_VALUE;
		for(int j = 0; j<array.length; j++){
			for(int i = 0; i<array[j].length; i++){
				if(array[j][i]<min){
					min = array[j][i];
				}
			}
		}
		return min;
	}

	private static double getArithmetric(int[] array){
		double sum = 0;
		for(int j = 0; j<array.length; j++){
			sum = sum + (double)array[j];
		}
		return sum/(array.length);
	}

	private double[][] normalizeMap(double[][] map){
		double max = getMax(map);
		double min = getMin(map);
		double range = max - min;
		for(int j = 0; j<map.length; j++){
			for(int i = 0; i<map[j].length; i++){
				map[j][i] = ((map[j][i]-min)/range);
			}
		}
		return map;
	}

	@Override
	public void distanceMapToIntARGB(double[][] distanceMap, int[] dstPixels, int dstWidth, int dstHeight) {
		distanceMap = normalizeMap(distanceMap);
		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				int dstPosition = y * dstWidth + x;
				double distance = ((distanceMap[x][y])*255);
				dstPixels[dstPosition] = 0xFF000000 | (((int) distance & 0xff) << 16) | (((int) distance & 0xff) << 8) | ((int) distance & 0xff);

			}
		}
	}


	@Override
	public List<Point> findMaximas(double[][] distanceMap) {
		distanceMap = normalizeMap(distanceMap);
		ArrayList<Point> result = new ArrayList<>();
		int dstWidth = distanceMap.length;
		int dstHeight = distanceMap[0].length;
		double maxAccumulatorValue = getMax(distanceMap);
		//System.out.println(maxAccumulatorValue);
		double thresholdValue = (0.6 * maxAccumulatorValue);
		for(int x = 0; x<dstWidth; x++){
			for(int y = 0; y<dstHeight; y++){
				if(distanceMap[x][y]<thresholdValue){
					continue;
					//skip
				}
				// kernel mit Radius von 10
				// 60 % der Werte sollen abgecuttet werden
				boolean isLocalMaximum = true;
				double center = distanceMap[x][y];
				int kernelRadius = 5;
				for(int dx = -kernelRadius; dx<=kernelRadius;dx++){
					for(int dy = -kernelRadius; dy<=kernelRadius;dy++){
						//Wenn einer der umrandeten Pixel größer als center ist, dann soll dieser weiß gefärbt werden.
						int xPos = x + dx;
						int yPos = y + dy;
						if(xPos<0 || xPos>=dstWidth || yPos<0 || yPos>=dstHeight || ((xPos==x)&&(yPos==y))){
							//skip this pixel, don't take into account
							continue;
						}
						double currentPixel = distanceMap[xPos][yPos];
						//System.out.println(currentPixel);
						if(currentPixel>=center) {
							isLocalMaximum = false;
						}
					}
				}
				//if(num>8) isLocalMaximum = true;
				if(isLocalMaximum){
					result.add(new Point(x,y));
				}
			}
		}
		return result;
	}	
}
