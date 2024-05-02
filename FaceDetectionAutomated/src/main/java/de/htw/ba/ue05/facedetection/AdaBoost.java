package de.htw.ba.ue05.facedetection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import de.htw.ba.facedetection.IntegralImage;
import de.htw.ba.facedetection.StrongClassifier;
import de.htw.ba.facedetection.TestImage;
import de.htw.ba.facedetection.WeakClassifier;

public class AdaBoost {

	StrongClassifier trainedStrongClassifier;
	final TestImage testImage;
	IntegralImage integralImage;
	List<WeakClassifier> classifiers;
	int iterations ;
	List<Rectangle> faces;
	List<Double> faceWeights;
	List<Rectangle> nonFaces;
	List<Double> nonFaceWeights;

	public AdaBoost(TestImage testImage, IntegralImage integralImage, List<WeakClassifier> classifiers, int iterations) {
		this.testImage = testImage;
		this.integralImage = integralImage;
		this.classifiers = classifiers;
		this.iterations = iterations;
	}

	public StrongClassifier createTrainedClassifier() {

		//Initialize training data
		faces = testImage.getFaceRectangles();
		nonFaces = testImage.getNonFaceRectangles();
		faceWeights = new ArrayList<>();
		nonFaceWeights = new ArrayList<>();

		//Initialize weights of Faces
		for(int i = 0; i<faces.size(); i++){
			faceWeights.add(1.0/(faces.size()*2));
		}
		for(int i = 0; i<nonFaces.size(); i++){
			nonFaceWeights.add(1.0/(nonFaces.size()*2));
		}

		Dimension dimension = testImage.getAverageFaceDimensions().getSize();
		StrongClassifier strong = new StrongClassifierImpl(dimension);
		//for (WeakClassifier classifier : classifiers) {
		//	strong.addWeakClassifier(classifier);
		//}
		List<WeakClassifier> selectedClassifiers = new ArrayList<>(iterations);

		for(int i= 0; i<iterations; i++){
			System.out.println("NEW ITERATION");
			//All the training code goes here:
			normalizeImageWeights();
			double bestError = Double.MAX_VALUE;
			WeakClassifier bestClassifier = null;
			//In jeder Iteration werden NUR die weights von den Bildern geändert.
			//Die Bilder, die falsch erkannt werden, bekommen laut Funktion X etwas dazu addiert, und werden dann normalized.
			for (WeakClassifier classifier : classifiers) {
				double error = getError(classifier);
				if(error<bestError){
					bestError = error;
					bestClassifier = classifier;
					System.out.println("Best classifier:"+bestClassifier+" with value "+ error);
				}
				// Now find the wk with the lowest error
				// weitere for loop
				// bester weakclassifier
				// die false positive bilder und false negative bilder werden von weights erhöht
				// check welche bilder nicht gut gefunden wurden
				// erhöhe die weights der bilder indem du mit bestem classifier checkst
				// bester classifier
			}
			if(bestClassifier!=null){
				selectedClassifiers.add(bestClassifier);
				recalculateImageWeights(bestClassifier,bestError);
				classifiers.remove(bestClassifier);
			}
		}

		for(WeakClassifier classifier : selectedClassifiers){
			strong.addWeakClassifier(classifier);
		}

		strong.normalizeWeights();
		trainedStrongClassifier = strong;
		return trainedStrongClassifier;
	}

	private void recalculateImageWeights(WeakClassifier classifier, double error) {
		if(error == 0.0){
			error += 0.00000001;
		}
		double beta = error / (1.0 - error);
		for(Rectangle face: faces){
			int faceWeightIndex = 0;
			boolean falseNegative = !classifier.matchingAt(integralImage,face.x,face.y).isDetected;
			if(falseNegative){
				double newValue = faceWeights.get(faceWeightIndex) * beta;
				faceWeights.set(faceWeightIndex,newValue);
			}
			faceWeightIndex++;
		}
		for(Rectangle nonface: nonFaces){
			int nonFaceWeightIndex = 0;
			boolean falsePositive = classifier.matchingAt(integralImage, nonface.x, nonface.y).isDetected;
			if(falsePositive){
				double newValue = nonFaceWeights.get(nonFaceWeightIndex) * beta;
				nonFaceWeights.set(nonFaceWeightIndex,newValue);
			}
			nonFaceWeightIndex++;
		}
	}

	private double getError(WeakClassifier classifier) {
		double error = 0.0;
		for(Rectangle face: faces){
			int faceWeightIndex = 0;
			boolean falseNegative = !classifier.matchingAt(integralImage,face.x,face.y).isDetected;
			if(falseNegative){
				error += faceWeights.get(faceWeightIndex);
			}
			faceWeightIndex++;
		}
		for(Rectangle nonface: nonFaces){
			int nonFaceWeightIndex = 0;
			boolean falsePositive = classifier.matchingAt(integralImage, nonface.x, nonface.y).isDetected;
			if(falsePositive){
				error += nonFaceWeights.get(nonFaceWeightIndex);
			}
			nonFaceWeightIndex++;
		}
		return error;
	}

	private void normalizeImageWeights() {
		double sum = 0;
		for(int i = 0; i<faces.size(); i++){
			sum += faceWeights.get(i);
		}
		for(int i = 0; i<nonFaces.size(); i++){
			sum += nonFaceWeights.get(i);
		}
		for(int i = 0; i<faces.size(); i++){
			double newValue = faceWeights.get(i) / sum;
			faceWeights.set(i,newValue);
		}
		for(int i = 0; i<nonFaces.size(); i++){
			double newValue = nonFaceWeights.get(i) / sum;
			nonFaceWeights.set(i,newValue);
		}
	}

	private void updateWeight(WeakClassifierImpl classifier){
		//TODO: Calculate error of this classifier
		//TODO: Update its weight
	}
}

