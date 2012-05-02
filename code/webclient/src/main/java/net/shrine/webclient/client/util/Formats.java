package net.shrine.webclient.client.util;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

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
		
		public static final DateTimeFormat iso8601 = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	}
}
