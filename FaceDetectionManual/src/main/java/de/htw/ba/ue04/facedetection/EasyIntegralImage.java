package de.htw.ba.ue04.facedetection;

import de.htw.ba.ue04.faceutilities.ArrayMinMaxTools;

import de.htw.ba.facedetection.IntegralImage;

import java.util.Arrays;

public class EasyIntegralImage implements IntegralImage {

	private int width = 0;
	private int height = 0;
	private int[] integralPixels;
	public EasyIntegralImage(int[] srcPixels, int width, int height) {
		this.width = width;
		this.height = height;
		this.integralPixels = getIntegralImageFromPixels(srcPixels, width, height);
		//printIntegral();
	}

	public static int[] getIntegralImageFromPixels(int[] srcPixels, int width, int height){
		int[] grayPixels = new int[srcPixels.length];
		int[] integralPixels = new int[srcPixels.length];
		getGrayValues(srcPixels,width,height,grayPixels,width,height);
		for(int x = 0; x<width; x++){
			int last = 0;
			for(int y = 0; y<height; y++){
				int pos = y * width + x;
				int current = grayPixels[pos] + last;
				integralPixels[pos] = current;
				last = current;
			}
		}
		for(int y = 0; y<height; y++){
			int last = 0;
			for(int x = 0; x<width; x++){
				int pos = y * width + x;
				int current = integralPixels[pos] + last;
				integralPixels[pos] += current;
				last = current;
			}
		}
		return integralPixels;
	}

	private void printIntegral(){
		for(int y = 0; y<height/4; y++){
			for(int x = 0; x<width; x++){
				int pos = y * width + x;
				System.out.print(integralPixels[pos]+" ,");
			}
			System.out.println();
		}
	}

	private static void getGrayValues(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth, int dstHeight) {
		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				int pos = y * dstWidth + x;

				int c = srcPixels[y * srcWidth + x]; // RGB Value
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;

				int lum = (int) (0.299 * r + 0.587 * g + 0.114 * b); // Grauwert
				lum = Math.min(lum, 255);
				dstPixels[pos] = lum;
			}
		}
	}
	
	@Override
	public double meanValue(int x, int y, int width, int height) {
		int F1 = integralPixels[y*this.width + x];
		int F2 = integralPixels[(height+y)*this.width + x];
		int F3 = integralPixels[y*this.width + (width+x)];
		int F4 = integralPixels[(height+y)*this.width + (width+x)];
		int localF = F4-F2-F3+F1;

		return (double)localF/(width*height);
	}

	@Override
	public void toIntARGB(int[] dstImage) {
		int max = ArrayMinMaxTools.getMax(integralPixels);
		int min = ArrayMinMaxTools.getMin(integralPixels);
		for (int i = 0; i < integralPixels.length; i++) {
			int value = (int) (((double) (integralPixels[i] - min) / (double) (max - min)) * 255.0);
			int argbValue = 0xFF000000 | (value << 16) | (value << 8) | value;
			dstImage[i] = argbValue;
		}
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
