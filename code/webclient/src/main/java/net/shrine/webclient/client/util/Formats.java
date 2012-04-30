package net.shrine.webclient.client.util;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * 
 * @author clint
 * @date Apr 30, 2012
 */
public final class Formats {
	private Formats() {
		super();
	}
	
	public static final class Date {
		private Date() {
			super();
		}
		
		public static final DateTimeFormat yearMonthDay = DateTimeFormat.getFormat("yyyy-MM-dd");
	}
}
