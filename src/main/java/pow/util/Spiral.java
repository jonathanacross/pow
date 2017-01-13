package pow.util;

public class Spiral {

    // Finds coordinates of the number n, working outward
    // in a spiral order, as below. 0 starts at the origin.
    //
    //    12  11  10   9  24
    //    13   2   1   8  23
    //    14   3   0   7  22
    //    15   4   5   6  21
    //    16  17  18  19  20
    public static Point position(int n) {
        int nOffset = n + 1;
        final int k = (int) Math.ceil((Math.sqrt(nOffset) - 1) / 2);
        int t = 2 * k + 1;
        int m = t*t;

        t -= 1;

        if (nOffset >= m - t) {
            return new Point(k - (m - nOffset), -k);
        }

        m -= t;

        if (nOffset >= m - t) {
            return new Point(-k, -k + (m - nOffset));
        }

        m -= t;

        if (nOffset >= m - t) {
            return new Point(-k + (m - nOffset), k);
        }

        return new Point(k, k - (m - nOffset - t));
    }
}
