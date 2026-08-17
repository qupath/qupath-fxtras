package qupath.fx.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformed axes for JavaFX charts.
 */
public class TransformedAxes {

    /**
     * Create an axis to display values on a log10(x+1) scale.
     * @return an axis with default ticks
     */
    public static TransformedAxis log1p10() {
        return new LogAxis(10);
    }

    /**
     * Create an axis to display values on a log10(x+1) scale.
     * @param nMinorTicks the number of minor ticks
     * @return an axis with a suitable number of minor ticks
     */
    public static TransformedAxis log1p10(int nMinorTicks) {
        return new LogAxis(10, nMinorTicks);
    }

    /**
     * Create an axis to display values on a log2(x+1) scale.
     * @return an axis with default ticks
     */
    public static TransformedAxis log1p2() {
        return new LogAxis(2, 9);
    }

    /**
     * Create an axis to display values on a log2(x+1) scale.
     * @param mMinorTicks the number of minor ticks
     * @return an axis with n minor ticks
     */
    public static TransformedAxis log1p2(int mMinorTicks) {
        return new LogAxis(2, mMinorTicks);
    }

    static class LogAxis extends TransformedAxis {

        private final double base;
        private final int nTicks;

        LogAxis(double base) {
            this(base, 9);
        }

        LogAxis(double base, int nTicks) {
            super((d) -> Math.log1p(d) / Math.log(base),
                    (d) -> Math.expm1(d * Math.log(base))); // change of base for expm1
            this.base = base;
            this.nTicks = nTicks;
        }


        @Override
        protected List<Number> calculateTickValues(double length, Object range) {
            AxisBounds ab = (AxisBounds) range;
            double lowerBound = ab.lower();
            double upperBound = ab.upper();
            List<Number> tickValues = new ArrayList<>();
            double minValue = transform.applyAsDouble(lowerBound);
            double maxValue = transform.applyAsDouble(upperBound);
            minValue = Math.floor(minValue);
            maxValue = Math.ceil(maxValue);
            for (double x = minValue; x <= maxValue; x++) {
                tickValues.add(Math.pow(base, x));
            }
            return tickValues;
        }

        @Override
        protected List<Number> calculateMinorTickMarks() {
            List<Number> minorTicks = new ArrayList<>();
            // called decade even though may be arbitrary base
            double currentDecade = Math.pow(base, Math.floor(transform.applyAsDouble(getLowerBound())));
            double upperBound = getUpperBound();

            while (currentDecade <= upperBound) {
                // add a tick at each of N ticks between current and next decade
                for (int i = 0; i < nTicks; i++) {
                    minorTicks.add(currentDecade * (1 + (i * (base - 1)) / nTicks));
                }
                currentDecade *= base;
            }
            return minorTicks;
        }

    }

}
