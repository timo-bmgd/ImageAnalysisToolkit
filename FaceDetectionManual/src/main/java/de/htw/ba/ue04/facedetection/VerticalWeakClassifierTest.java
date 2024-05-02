package de.htw.ba.ue04.facedetection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VerticalWeakClassifierTest {


    @Test
    void matchingAtWithGrayImage() {
        int[] gray = new int[400*600];
        Arrays.fill(gray,0xFF888888);
        gray = EasyIntegralImage.getIntegralImageFromPixels(gray,400,600);
        EasyIntegralImage i = new EasyIntegralImage(gray,400,600);
        double a = i.meanValue(7,10,20,20);
        double b = i.meanValue(27,10,20,20);
        assertEquals(a, b);
    }

    @Test
    void matchingAtWithGrayImageBunch() {
        int[] gray = new int[400*600];
        Arrays.fill(gray,0xFF888888);
        EasyIntegralImage image = new EasyIntegralImage(gray,400,600);
        for(int i = 0; i<1000; i++)

        System.out.println(image.meanValue(7,10,20,20));
    }
}