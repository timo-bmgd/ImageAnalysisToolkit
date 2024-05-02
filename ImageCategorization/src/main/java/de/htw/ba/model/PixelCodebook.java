package de.htw.ba.model;

import java.util.List;

public interface PixelCodebook {
	
	/**
	 * Finds for a specific feature vector the most similar code in the codebook.
	 * Return the index of the code. The corresponding code get be obtained via {@link #getCode(int)}.
	 * 
	 * @param feature vector
	 * @return index of the most similar code to the given feature vector
	 */
	public int findClosestCode(byte[] featureVector);
	
	/**
	 * The feature of the code associated to the given code index.
	 * 
	 * @param codeIndex
	 * @return feature of the code 
	 */
	public byte[] getCode(int codeIndex);
	
	/**
	 * Each feature vector is assigned to the most similar code. The number of assignments 
	 * is counted and represented in a histogram. The size of the histogram is equal to 
	 * the number of codes in the codebook.
	 * 
	 * @param list of feature vectors to analyze
	 * @return histogram
	 */
	public float[] computeHistogram(List<byte[]> featureVectors);
}
