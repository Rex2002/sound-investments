package audio.synth.devgraphing;

public class FFT {

    public static Complex[] fft(short[] vTd){
        Complex[] complexValues = new Complex[(int) Math.pow(2, (int) (Math.log(vTd.length)/Math.log(2)))];
        for(int i = 0 ; i< complexValues.length; i++){
            complexValues[i] = new Complex(vTd[i], 0);
        }
        transform(complexValues);
        return complexValues;
    }

    public static void transform (Complex[] c){
        if (c.length <= 1){
            return;
        }
        Complex[] even = new Complex[c.length/2];
        Complex[] odd  = new Complex[c.length/2];

        for(int i = 0; i < c.length; i++){
            if( i % 2 == 0){
                even[i/2] = c[i];
            }
            else{
                odd[(i-1)/2] = c[i];
            }
        }
        transform(even);
        transform(odd);

        for(int i = 0; i < c.length / 2; i ++){
            double kth = -2 * i * Math.PI / c.length;
            Complex t = new Complex(Math.cos(kth), Math.sin(kth)).multiply(odd[i]);
            c[i] = even[i].add(t);
            c[i + c.length/ 2] = even[i].subtract(t);
        }
    }
}