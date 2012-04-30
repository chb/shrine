package net.shrine.webclient.client.util;

import java.util.Date;

import net.shrine.webclient.client.AbstractWebclientTest;

/**
 * 
 * @author clint
 * @date Apr 30, 2012
 */
public class FormatsTestGwt extends AbstractWebclientTest {
  public void testYearMonthDateFormat() {
	  final String dateString = "2012-04-30";
	  
	  final Date d = Formats.Date.yearMonthDay.parse(dateString);
	  
	  assertEquals(dateString, Formats.Date.yearMonthDay.format(d));
  }
}
