package net.shrine.adapter

import net.shrine.protocol.QueryResult
import java.util.Random

/**
 * @author Bill Simons
 * @date 4/21/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object Obfuscator {
  def obfuscate(result: QueryResult): QueryResult = {
    result.withSetSize(obfuscate(result.setSize))
  }

  def obfuscate(l: Long): Long = {
    import GaussianObfuscator._

    val obfuscationAmount = determineObfuscationAmount(l)

    determineObfuscatedSetSize(l, obfuscationAmount)
  }

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
  private object GaussianObfuscator {
    private val stdDev = 1.33

    private val mean = 0

    private val rand = new Random

    private val range = 3

    private val lower = (-range).toDouble

    private val upper = range.toDouble

    def determineObfuscationAmount(x: Long): Int = scala.math.round(gaussian(mean, stdDev)).toInt

    def determineObfuscatedSetSize(setSize: Long, obfuscationAmount: Int): Long = {
      if (setSize <= 10) -1L else setSize + obfuscationAmount
    }

    /**
     * Return a real number from a gaussian distribution with given mean and
     * stddev
     */
    def gaussian(mean: Double, stddev: Double): Double = {
      def limitRange(v: Double): Double = {
        val partiallyClamped = if (v < lower) lower else v

        if (partiallyClamped > upper) upper else partiallyClamped
      }

      limitRange(mean + (stddev * rand.nextGaussian))
    }
  }
}