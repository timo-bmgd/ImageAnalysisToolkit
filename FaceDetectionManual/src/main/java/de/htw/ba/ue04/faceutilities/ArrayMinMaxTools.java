package de.htw.ba.ue04.faceutilities;

public class ArrayMinMaxTools {

    public static int getMax(int[][] array){
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array is null or empty");
        }
        int max = 0;
        for(int j = 0; j<array.length; j++){
            for(int i = 0; i<array[j].length; i++){
                if(array[j][i]>max){
                    max = array[j][i];
                }
            }
        }
        return max;
    }

    public static int getMin(int[][] array){
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array is null or empty");
        }
        int min = Integer.MAX_VALUE;;
        for(int j = 0; j<array.length; j++){
            for(int i = 0; i<array[j].length; i++){
                if(array[j][i]<min){
                    min = array[j][i];
                }
            }
        }
        return min;
    }
    public static int getMax(int[] array){
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array is null or empty");
        }
        int max = 0;
        for(int j = 0; j<array.length; j++){
            if(array[j]>max) {
                max = array[j];
            }
        }
        return max;
    }

    public static int getMin(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array is null or empty");
        }
        int min = Integer.MAX_VALUE;;
        for (int j = 0; j < array.length; j++) {
            if (array[j] < min) {
                min = array[j];
            }
        }
        return min;
    }

    public static double getMean(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array is null or empty");
        }

        int sum = 0;
        for (int value : array) {
            sum += value;
        }

        return (double) sum / array.length;
    }

}


