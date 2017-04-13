package utils;

public class CurveFit {
    public static class Params {
        public final double a;
        public final double b;

        public Params(double a, double b) {
            this.a = a;
            this.b = b;
        }
    }

    // fits the function y = a + bx
    public static Params linearFit(double[] x, double[] y) {
        double sumx = 0;
        double sumy = 0;
        double sumxx = 0;
        double sumxy = 0;
        for (int i = 0; i < x.length; i++) {
            sumx += x[i];
            sumy += y[i];
            sumxx += x[i]*x[i];
            sumxy += x[i]*y[i];
        }
        double n = x.length;

        double b = (n * sumxy - sumx * sumy) / (n * sumxx - sumx * sumx);
        double a = (sumy - b * sumx) / n;
        return new Params(a,b);
    }

    // fit the function y = a * exp(b*x)
    public static Params expFit(double[] x, double[] y) {
        double[] logy = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            logy[i] = Math.log(y[i]);
        }
        Params linParams = linearFit(x, logy);
        return new Params(Math.exp(linParams.a), linParams.b);
    }

//    public static void main(String[] args) {
//        double[] x = {1,2,3,4,5,6,7};
//        double[] y = {1,3,6,6,8,9,10};
//        double[] y2 = {1,2,3,5,8,13,21};
//        Params p = linearFit(x,y);
//        System.out.println(p.a + " " + p.b);
//
//        Params p2 = expFit(x,y2);
//        System.out.println(p2.a + " " + p2.b);
//    }
}
