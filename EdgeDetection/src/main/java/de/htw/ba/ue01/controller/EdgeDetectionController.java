/**
 * @author Nico Hezel
 */
package de.htw.ba.ue01.controller;

import java.awt.*;
import java.text.ParseException;

public class EdgeDetectionController extends EdgeDetectionBase {

	protected static enum Methods {
		Kopie, Graustufen, XGradient, YGradient, XSobel, YSobel, Gradientenbetrag, Gradientenwinkel, Gradientenwinkel_Farbe, Kombiniertes_Bild,
	};

	@Override
	public void runMethod(Methods currentMethod, int[] srcPixels, int[] dstPixels, int width, int height) throws Exception {
		double parameter1 = getParameter();
		switch (currentMethod) {
			case Graustufen:
				parameter1 = getParameter();
				doGray(srcPixels, dstPixels, width, height, parameter1);
				break;
			case XGradient:
				doGray(srcPixels, dstPixels, width, height, parameter1);
				convolute3x3kernel(srcPixels, dstPixels, width, height, parameter1, new int[]{
						0,-1,0,
						0,0,0,
						0,1,0
				});
				break;
			case XSobel:
				doGray(srcPixels, dstPixels, width, height, parameter1);
				convolute3x3kernel(srcPixels, dstPixels, width, height, parameter1, new int[]{
						-1,-2,-1,
						0,0,0,
						1,2,1
				});
				break;
			case YGradient:
				doGray(srcPixels, dstPixels, width, height, parameter1);
				convolute3x3kernel(srcPixels, dstPixels, width, height, parameter1, new int[]{
						0,0,0,
						-1,0,1,
						0,0,0
				});
				break;
			case YSobel:
				doGray(srcPixels, dstPixels, width, height, parameter1);
				convolute3x3kernel(srcPixels, dstPixels, width, height, parameter1, new int[]{
						-1,0,1,
						-2,0,2,
						-1,0,1
				});
				break;
			case Gradientenbetrag:
				gradientenBetrag(srcPixels, dstPixels, width, height, parameter1);
				break;
			case Gradientenwinkel:
				gradientenwinkel(srcPixels, dstPixels, width, height, parameter1);
				break;
			case Gradientenwinkel_Farbe:
				gradientenwinkel_farbe(srcPixels, dstPixels, width, height, parameter1);
				break;
			case Kombiniertes_Bild:
				kombiniert(srcPixels, dstPixels, width, height, parameter1);
				break;
			case Kopie:
			default:
				doCopy(srcPixels, dstPixels, width, height);
				break;
		}
	}

	private void doGray(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;

				int c = srcPixels[pos];
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = (c) & 0xFF;

				int lum = (int) Math.max(0,Math.min(255,(0.299 * r + 0.587 * g + 0.114 * b + parameter)));
				dstPixels[pos] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
			}
		}
	}

	private int getKernelSize(int kernel[]){
		int sum = 0;
		for(int i = 0; i<kernel.length; i++){
			sum += Math.abs(kernel[i]);
		}
		return sum;
	}

	private void convolute3x3kernel(int srcPixels[], int dstPixels[], int width, int height, double parameter, int[] kernel) throws ParseException {
		// loop over all pixels of the destination image
		int kernelsize = getKernelSize(kernel);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				int ableitung = (int)parameter;
				int pos = y * width + x;

				for (int k = 0; k < 9; k++) {
					int dx = k % 3 - 1;
					int dy = k / 3 - 1;

					int yIndex = Math.min(height-1,Math.max(0,y+dy));
					int xIndex = Math.min(width-1,Math.max(0,x+dx));

					int kPos = yIndex * width + xIndex;
					ableitung += (int)((double)(srcPixels[kPos]&0xFF)*kernel[k]/(double)kernelsize);  ///(double)kernelsize);
				}

				//calc it
				int c = ableitung;

				//clamp it
				c = Math.max(0, Math.min(c, 255));

				dstPixels[pos] = 0xFF000000 | (c << 16) | (c << 8) | c;
			}
		}
	}

	private void convolute3x3kernel_abs(int srcPixels[], int dstPixels[], int width, int height, double parameter, int[] kernel) throws ParseException {
		// loop over all pixels of the destination image
		int kernelsize = getKernelSize(kernel);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				int c = 0;

				int ableitung = (int)parameter;
				int pos = y * width + x;

				for (int k = 0; k < 9; k++) {
					int dx = k % 3 - 1;
					int dy = k / 3 - 1;

					int yIndex = Math.min(height-1,Math.max(0,y+dy));
					int xIndex = Math.min(width-1,Math.max(0,x+dx));

					int kPos = yIndex * width + xIndex;
					ableitung += (int)((double)(srcPixels[kPos]&0xFF)*kernel[k]/(double)kernelsize);  ///(double)kernelsize);
				}

				//calc it
				c = Math.abs(ableitung);

				//clamp it
				c = Math.max(0, Math.min(c, 255));

				dstPixels[pos] = 0xFF000000 | (c << 16) | (c << 8) | c;
			}
		}
	}

	private void gradientenBetrag(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int[] xPixels = new int[dstPixels.length];
		doGray(srcPixels, xPixels, width, height, parameter);
		convolute3x3kernel_abs(srcPixels, xPixels, width, height, 0, new int[]{
				-1,-2,-1,
				0,0,0,
				1,2,1
		});
		int[] yPixels = new int[dstPixels.length];
		doGray(srcPixels, yPixels, width, height, parameter);
		convolute3x3kernel_abs(srcPixels, yPixels, width, height, 0, new int[]{
				-1,0,1,
				-2,0,2,
				-1,0,1
		});

		for(int i = 0; i<srcPixels.length; i++){

			int yValue = yPixels[i]&0xFF;
			int xValue = xPixels[i]&0xFF;

			int c = (int)Math.hypot((double)yValue,(double)xValue);

			c = Math.max(0,Math.min(c*2 ,255));

			dstPixels[i] = 0xFF000000 | (c << 16) | (c << 8) | c;
		}
	}



	private void gradientenwinkel(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int[] xPixels = new int[dstPixels.length];
		int[] srcBufferX = srcPixels;
		doGray(srcPixels, srcBufferX, width, height, parameter);
		convolute3x3kernel(srcBufferX, xPixels, width, height, 127, new int[]{
				-1,-2,-1,
				0,0,0,
				1,2,1
		});
		int[] yPixels = new int[dstPixels.length];
		int[] srcBufferY = srcPixels;
		convolute3x3kernel(srcBufferY, yPixels, width, height, 127, new int[]{
				-1,0,1,
				-2,0,2,
				-1,0,1
		});

		for(int i = 0; i<srcPixels.length; i++){

			int yValue = yPixels[i]&0xFF -127;
			int xValue = xPixels[i]&0xFF -127;

			double d = (Math.atan2(yValue, xValue));
			d = Math.toDegrees(d);

			float hue = (float)(d);
			float sat = 1;
			float bright = 1;

			dstPixels[i] = Color.HSBtoRGB(hue,sat,bright);
		}
	}

	private void gradientenwinkel2(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int[] kernel = new int[]{
				0,0,0,
				-1,0,1,
				0,0,0
		};
		int kernelsize = getKernelSize(kernel);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				int c = 0;

				int ableitung = (int) parameter;
				int pos = y * width + x;

				for (int k = 0; k < 9; k++) {
					int dx = k % 3 - 1;
					int dy = k / 3 - 1;

					int yIndex = Math.min(height - 1, Math.max(0, y + dy));
					int xIndex = Math.min(width - 1, Math.max(0, x + dx));

					int kPos = yIndex * width + xIndex;
					ableitung += (int) ((double) (srcPixels[kPos] & 0xFF) * kernel[k] / (double) kernelsize);  ///(double)kernelsize);
				}

				//calc it
				c = Math.abs(ableitung);

				//clamp it
				c = Math.max(0, Math.min(c, 255));

				dstPixels[pos] = 0xFF000000 | (c << 16) | (c << 8) | c;
			}
		}
	}

	private void gradientenwinkel_farbe(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int[] xPixels = new int[dstPixels.length];
		doGray(srcPixels, xPixels, width, height, parameter);
		convolute3x3kernel(srcPixels, xPixels, width, height, 0, new int[]{
				-1,-2,-1,
				0,0,0,
				1,2,1
		});
		int[] yPixels = new int[dstPixels.length];
		doGray(srcPixels, yPixels, width, height, parameter);
		convolute3x3kernel(srcPixels, yPixels, width, height, 0, new int[]{
				-1,0,1,
				-2,0,2,
				-1,0,1
		});

		for(int i = 0; i<srcPixels.length; i++){

			int yValue = yPixels[i]&0xFF;
			int xValue = xPixels[i]&0xFF;

			double d = (Math.atan2(yValue, xValue));
			d = Math.toDegrees(d);

			float hue = (float)(d);
			float sat = 1;
			float bright = 1;

			dstPixels[i] = Color.HSBtoRGB(hue,sat,bright);
		}
	}

	private void kombiniert(int srcPixels[], int dstPixels[], int width, int height, double parameter) throws ParseException {
		int[] xPixels = new int[dstPixels.length];
		doGray(srcPixels, xPixels, width, height, parameter);
		convolute3x3kernel(srcPixels, xPixels, width, height, 0, new int[]{
				-1,-2,-1,
				0,0,0,
				1,2,1
		});
		int[] yPixels = new int[dstPixels.length];
		doGray(srcPixels, yPixels, width, height, parameter);
		convolute3x3kernel(srcPixels, yPixels, width, height, 0, new int[]{
				-1,0,1,
				-2,0,2,
				-1,0,1
		});

		for(int i = 0; i<srcPixels.length; i++){

			int yValue = yPixels[i]&0xFF;
			int xValue = xPixels[i]&0xFF;

			double d = (Math.atan2(yValue, xValue));
			double c = Math.hypot((double)yValue,(double)xValue)/128;
			d = Math.toDegrees(d);

			float hue = (float)(d);
			float sat = 1;
			float bright = 1;

			dstPixels[i] = Color.HSBtoRGB(hue,sat,(float)c);
		}
	}



	private void doCopy(int srcPixels[], int dstPixels[], int width, int height) {
		// loop over all pixels of the destination image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pos = y * width + x;
				dstPixels[pos] = srcPixels[pos];
			}
		}
	}
}
