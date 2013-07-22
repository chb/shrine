package edu.harvard.i2b2.crc.datavo.i2b2message;

import java.math.BigDecimal;
import java.math.MathContext;

public class Constants
{
	public static BigDecimal i2b2_version_compatible = getBigDecimal(1.1);
	public static BigDecimal hl7_version_compatible  = getBigDecimal(2.4);
	public static String     facility_name			 = "i2b2 hive";
	
	private static BigDecimal getBigDecimal(double value)
	{
		return getBigDecimal(value, 2);
	}
	private static BigDecimal getBigDecimal(double value, int precision)
	{
		return new BigDecimal(value, new MathContext(precision)); 
	}
}
