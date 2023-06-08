package audio.synth.devgraphing;

public class Complex {
    public final double real;
    public final double imaginary;

    public Complex(double r, double i){
        real = r;
        imaginary = i;
    }

    public static Complex polar(double rho, double theta){
        return new Complex(rho * Math.cos(theta), rho * Math.sin(theta));
    }

    public Complex conjugate(){
        return new Complex(real, -imaginary);
    }

    public Complex multiply(Complex t){
        return new Complex(real * t.real - imaginary * t.imaginary, real * t.imaginary + imaginary * t.real);
    }

    public Complex add(Complex t) {
        return new Complex(real + t.real, imaginary + t.imaginary);
    }

    public Complex subtract(Complex t) {
        return new Complex(real - t.real, imaginary - t.imaginary);
    }

    @Override
    public String toString(){
        return "Real: " + real + ", Imaginary: " + imaginary;
    }
}