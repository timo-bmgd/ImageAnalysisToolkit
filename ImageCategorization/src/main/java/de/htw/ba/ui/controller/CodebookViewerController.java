package de.htw.ba.ui.controller;

import java.util.*;

import de.htw.ba.model.ImageData;
import de.htw.ba.model.PixelCodebook;
import javafx.scene.image.Image;

/**
 * Codebook viewer. Lists all image files in a directory.
 * Provides an image and image patch viewer. Furthermore is visualizes the codebook.
 * 
 * The controller handles all events of the CodebookViewer.fxml view.
 * 
 * @author Nico Hezel
 */
public class CodebookViewerController extends CodebookViewerBase {
	
	/**
	 * Splits the image into several sub regions. The regions have a size of patchSize x patchSize.
	 * All the patches will be listed in scan line order into an array list. Each entry in the list 
	 * contains all the pixels of this sub region and their corresponding RGB pixels. There 
	 * are patchSize x patchSize x RGB (e.g. 16x16x3=768) values in a patch and every one of 
	 * them has a value between 0 and 255. The values are sorted in signed bytes:
	 * 
	 * int i = 200;				// prints 200
	 * byte b = (byte) 200;		// prints -256
	 * int r = b & 0xFF;		// prints 200
	 * 
	 * A similar technique is used to read and write ARGB values from and into an integer.
	 * 
	 * Example of a 6x4 pixel images divided into 3x2 sub regions with a patch size of 2.
	 * -------------------------------
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * -------------------------------
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * -------------------------------
	 * 
	 * @param image
	 * @param patchSize
	 * @return list of patches
	 */
	@Override
	public List<byte[]> extractPatches(Image image, int patchSize) {

		int patchDivisions = (int)image.getWidth()/patchSize;

		List<byte[]> patches = new ArrayList<byte[]>();

		//Then loop through the image with patch differences
		for (int y = 0; y<image.getHeight(); y += patchSize){
			for (int x = 0; x<image.getWidth(); x += patchSize){
				//Then loop through the single patch
				byte[] patch = new byte[patchSize*patchSize*3];
				int patchIndex = 0;
				for(int py = 0; py<patchSize; py++){
					for(int px = 0; px<patchSize; px++){
						//Then add 3 colors to the patch
						int argb = image.getPixelReader().getArgb(x+px,y+py);
						byte red = (byte) ((argb >> 16) & 0xFF);
						byte green = (byte) ((argb >> 8) & 0xFF);
						byte blue = (byte) (argb & 0xFF);

						patch[patchIndex] = red;
						patch[patchIndex+1] = green;
						patch[patchIndex+2] = blue;

						patchIndex += 3;
					}
				}
				patches.add(patch);
			}
		}
		return patches;
	}

	/**
	 * Creates a codebook by taking the pixel of a patch as a feature vector
	 * and clustering all provided feature vectors into a few clusters.
	 * The amount of cluster is determined by the codebook size.
	 * 
	 * @param patches
	 * @param codebookSize
	 * @return codebook
	 */
	@Override
	public PixelCodebook buildCodebook(List<byte[]> patches, int codebookSize) {

		//Generate a codebook which is saved in
		List<byte[]> codeBook = new ArrayList<byte[]>();

		//oberste for loop: Maximale Iterationszahl
		int iterations = 20;

		//choose codebookSize amount of cluster centers:
		//TODO: Make the cluster center choice random
		//Random random = new Random(132184512);

		// Get List of ClusterCentres
		List<byte[]> clusterCentres = new ArrayList<byte[]>();

		for(int i = 0; i<codebookSize; i++){
			int patchIndex = (int)Math.floor(i*(patches.size()/codebookSize));
			clusterCentres.add(Arrays.copyOf(patches.get(i), patches.get(i).length));
		}
		// Get List of ClusterGroups associated with each ClusterCentre

		/*
		for(int i = 0; i<codebookSize; i++){
			clusterCentres.add(Arrays.copyOf(patches.get(i), patches.get(i).length));
		}
		*/

		for(int i = 0; i<iterations; i++){
			// For every ClusterCenter, we need a patchGroup which contains all the patches

			// List to store patch groups associated with each cluster center
			List<List<byte[]>> patchGroups = new ArrayList<>();
			for (int j = 0; j < codebookSize; j++) {
				patchGroups.add(new ArrayList<>());
			}
			for (byte[] patch : patches) {
				byte[] closestCentre = clusterCentres.get(0);
				double shortestDistance = Double.MAX_VALUE;
				int closestIndex = 0;
				for (int k = 0; k < codebookSize; k++) {
					double currentDistance = getDistanceBetweenPatches(patch, clusterCentres.get(k));
					if (currentDistance < shortestDistance) {
						closestCentre = clusterCentres.get(k);
						shortestDistance = currentDistance;
						closestIndex = k;
					}
				}
				patchGroups.get(closestIndex).add(patch); // Add patch to corresponding patch group
			}
			for (int j = 0; j < codebookSize; j++) {
				byte[] newCentre = calculateNewClusterCenter(patchGroups.get(j));
				clusterCentres.set(j, newCentre);
			}
		}
		//Now loop select the final code book based on the clustercentres
		// Loop through final optimized cluster centers
		for (byte[] clusterCenter : clusterCentres) {
			// Find the patch closest to the cluster center
			byte[] closestPatch = patches.get(0);
			double shortestDistance = Double.MAX_VALUE;
			for (byte[] patch : patches) {
				double currentDistance = getDistanceBetweenPatches(patch, clusterCenter);
				if (currentDistance < shortestDistance) {
					closestPatch = patch;
					shortestDistance = currentDistance;
				}
			}
			// Add the closest patch to the codebook
			codeBook.add(closestPatch);
		}

		// dummy implementation
		return new PixelCodebook() {
			
			@Override
			public byte[] getCode(int codeIndex) {
				return codeBook.get(codeIndex);
			}
			
			@Override
			public int findClosestCode(byte[] featureVector) {
				int count = 0;
				int resultCode = 0;
				double minDistance = Double.MAX_VALUE;

				for (int i = 0; i < codeBook.size(); i++) {
					byte[] code = codeBook.get(i);
					double distance = getDistanceBetweenPatches(featureVector, code);
					if (distance < minDistance) {
						minDistance = distance;
						resultCode = i;
					}
				}

				return resultCode;
			}
			
			@Override
			public float[] computeHistogram(List<byte[]> featureVectors) {
				return new float[0];
			}
		};
	}

	private double getDistanceBetweenPatches(byte[] patch1, byte[] patch2) {
		double result = 0f;

		for(int i = 0; i<patch1.length; i++){
			double p1 = patch1[i];
			double p2 = patch2[i];
			double difference = p2-p1;
			difference = Math.pow(difference,2);
			result += difference;
		}
		return result;
	}

	private byte[] calculateNewClusterCenter(List<byte[]> patchGroup) {
		// Calculate the mean of all patches in the patch group to get the new cluster center
		int patchSize = patchGroup.get(0).length;
		byte[] newCenter = new byte[patchSize];
		for (int i = 0; i < patchSize; i++) {
			int sum = 0;
			for (byte[] patch : patchGroup) {
				sum += patch[i];
			}
			newCenter[i] = (byte) (sum / patchGroup.size());
		}
		return newCenter;
	}

	/**
	 * Converts the feature vectors of a patch into the feature vector of the 
	 * nearest cluster center from the codebook.
	 * 
	 * @param codebook
	 * @param patches
	 * @return
	 */
	@Override
	public List<byte[]> convertPatchToCodes(PixelCodebook codebook, List<byte[]> patches) {
		final List<byte[]> codes = new ArrayList<>(patches.size());
		for (int i = 0; i < patches.size(); i++) {
			final int codeIndex = codebook.findClosestCode(patches.get(i));
			codes.add(codebook.getCode(codeIndex));
		}
		return codes;
	}
	
	/**
	 * Computes the corresponding histogram of the image using the codebook.
	 * First it splits the image into smaller sub-regions (patches).
	 * Then it uses the patches and the codebook to create a histogram of occurrences.
	 * The histogram is stored in the imageData
	 * 
	 * @param codebook
	 * @param ImageData
	 * @param patchSize
	 */
	@Override
	public void computeHistogram(PixelCodebook codebook, ImageData imageData, int patchSize) {
		// create patches
		final List<byte[]> patches = extractPatches(imageData.getImage(), patchSize);


		// compute histogram with patches and codebook
		imageData.setHistogram(codebook.computeHistogram(patches));
	}

	/**
	 * Sorts the elements in the database based on the euclidean distance to the search query.
	 * The distance will be calculated between the histogram of the query and the histogram
	 * of every database entry. A sorted list of all database entries will be returned.
	 * 
	 * @param query
	 * @param database
	 * @return sorted database list
	 */
	@Override
	public List<ImageData> retrieve(ImageData query, Collection<ImageData> database) {
		return new ArrayList<>(database);
	}
}
