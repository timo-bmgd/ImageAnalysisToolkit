package de.htw.ba.facedetection;

/**
 * Das Integralbild speichert Informationen über die Pixelwerte
 * in einem Bild ab. Mit Hilfe der Informationen kann es Mittelwerte für
 * eine beliebige Fläche im Bild in konstanter Zeit ausrechnen.
 *
 * @author Nico Hezel
 */
public interface IntegralImage {

	/**
	 * Berechnet den Durchschnittswert unter der angegebenen Fläche. 
	 *
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public double meanValue(int x, int y, int width, int height);

	/**
	 * Wandelt das Integralbild in ein ARGB Bild um
	 * und zeichnet dieses in das dstImage Array.
	 *
	 * @param dstImage
	 */
	public void toIntARGB(int[] dstImage);

	/**
	 * Breite des Bildes
	 *
	 * @return
	 */
	public int getWidth();

	/**
	 * Höhe des Bildes
	 * @return
	 */
	public int getHeight();
}
