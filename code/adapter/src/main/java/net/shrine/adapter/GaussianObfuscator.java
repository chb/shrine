package net.shrine.adapter;

import org.apache.log4j.Logger;

import java.util.Random;

/**
 * [ SUMMARY ]
 * <p/>
 * [ Author ] Ricardo De Lima Date: August 18, 2009
 * <p/>
 * Harvard Medical School Center for BioMedical Informatics
 *
 * @link http://cbmi.med.harvard.edu
 * <p/>
 * [ In partnership with ]
 * @link http://chip.org
 * @link http://lcs.mgh.harvard.edu
 * @link http://www.brighamandwomens.org
 * @link http://bidmc.harvard.edu
 * @link http://dfhcc.harvard.edu
 * @link http://spin.nci.nih.gov/
 * <p/>
 * <p/>
 * ------------------------------------------------- [ Licensing ] All
 * works licensed by the Lesser GPL
 * @link http://www.gnu.org/licenses/lgpl.html
 * -------------------------------------------------
 */

public abstract class GaussianObfuscator {
    private static final double STD_DEV = 1.33;
    private static final double MEAN = 0;

    private static final Random generator = new Random();

    private static final Logger log = Logger.getLogger(GaussianObfuscator.class);

    private static final boolean DEBUG = log.isDebugEnabled();

    public static final String SAMPLE_SMALLER_THAN_TEN = "Sample sizes smaller than 10 will not be returned in order to prevent inadvertent identification of the sampled patients.";
    public static final int RANGE = 3;

    //Holder for static methods only

    private GaussianObfuscator() {
        super();
    }

    // return phi(x) = standard Gaussian mean 1.0 standard deviation 1.33

    public static double obfuscate(final double x) {
        return x + Math.round(gaussian(MEAN, STD_DEV));
    }

    public static int determineObfuscationAmount(final long x) {
        return (int) Math.round(gaussian(MEAN, STD_DEV));
    }

    public static long determineObfuscatedSetSize(long setSize, int obfuscationAmount) {
        if(setSize <= 10) {
            return -1;
        }
        else {
            return setSize + obfuscationAmount;
        }
    }

    /**
     * Return a real number from a gaussian distribution with given mean and
     * stddev
     */
    public static double gaussian(final double mean, final double stddev) {
        double v = mean + stddev * gaussian();
        v = limitRange(v);
        return v;
    }

    private static double limitRange(double v) {
        v = (v < -RANGE ? -RANGE : v);
        v = (v > RANGE ? RANGE : v);
        return v;
    }

    private static double gaussian() {
        return generator.nextGaussian();
    }
}
