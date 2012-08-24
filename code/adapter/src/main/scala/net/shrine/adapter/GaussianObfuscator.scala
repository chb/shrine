package net.shrine.adapter

import org.apache.log4j.Logger

import java.util.Random

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
object GaussianObfuscator {
  private val STD_DEV = 1.33

  private val MEAN = 0

  private val generator = new Random

  private val log = Logger.getLogger(getClass)

  private val DEBUG = log.isDebugEnabled

  val SAMPLE_SMALLER_THAN_TEN = "Sample sizes smaller than 10 will not be returned in order to prevent inadvertent identification of the sampled patients."

  val RANGE = 3
  
  private val lower = (-RANGE).toDouble
  
  private val upper = RANGE.toDouble

  def determineObfuscationAmount(x: Long): Int = scala.math.round(gaussian(MEAN, STD_DEV)).toInt

  def determineObfuscatedSetSize(setSize: Long, obfuscationAmount: Int): Long = {
    if (setSize <= 10) -1L else setSize + obfuscationAmount
  }

  /**
   * Return a real number from a gaussian distribution with given mean and
   * stddev
   */
  def gaussian(mean: Double, stddev: Double): Double = {
    limitRange(mean + (stddev * gaussian))
  }

  private def limitRange(v: Double): Double = {
    val partiallyClamped = if (v < lower) lower else v

    if (partiallyClamped > upper) upper else partiallyClamped
  }

  private def gaussian: Double = generator.nextGaussian
}
