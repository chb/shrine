package net.shrine.webclient.client;

import junit.framework.Test;
import net.shrine.webclient.client.controllers.QueryBuildingControllerTestGwt;
import net.shrine.webclient.client.controllers.QueryConstraintControllerTestGwt;
import net.shrine.webclient.client.controllers.QueryControllerTestGwt;
import net.shrine.webclient.client.domain.AndTestGwt;
import net.shrine.webclient.client.domain.OrTestGwt;
import net.shrine.webclient.client.domain.TermTestGwt;
import net.shrine.webclient.client.state.ExpressionXmlTestGwt;
import net.shrine.webclient.client.state.QueryGroupTestGwt;
import net.shrine.webclient.client.state.QuerySummarizerTestGwt;
import net.shrine.webclient.client.state.StateTestGwt;
import net.shrine.webclient.client.util.FormatsTestGwt;
import net.shrine.webclient.client.util.ObservableTestGwt;
import net.shrine.webclient.client.util.QueryGroupNamesIteratorTestGwt;
import net.shrine.webclient.client.util.UtilTestGwt;
import net.shrine.webclient.client.widgets.QueryRowTestGwt;
import net.shrine.webclient.client.widgets.QueryTermTestGwt;
import net.shrine.webclient.client.widgets.suggest.ForwardSuggestionEventsTestGwt;
import net.shrine.webclient.client.widgets.suggest.RichSuggestBoxTestGwt;
import net.shrine.webclient.client.widgets.suggest.RichSuggestResponseTestGwt;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionEventTestGwt;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionRowTestGwt;

import com.google.gwt.junit.tools.GWTTestSuite;

/**
 * 
 * @author clint
 * @date Apr 11, 2012
 * 
 * @see http://mojo.codehaus.org/gwt-maven-plugin/user-guide/testing.html
 * 
 * NB: TestSuites are atrocious and error-prone, but using one here drastically cuts down build times,
 * as firing up the GWT environment (expensive) only needs to happen once for the suite, not for each
 * test. :/ 
 * 
 * Named GwtTestSuite so the regular Maven surefire plugin won't pick this up, but the 
 * GWT test-running code will. 
 */
public final class GwtTestSuite extends GWTTestSuite {
	public GwtTestSuite(final String name) {
		super(name);
	}

	public static Test suite() {
		final GwtTestSuite suite = new GwtTestSuite("Shrine Webclient Tests");

		//NB: all actual tests are named *TestGwt, so the regular Maven surefire plugin
		//won't run them.  (It looks for classes with names beginning or ending with 'Test') 
		suite.addTestSuite(ForwardSuggestionEventsTestGwt.class);
		suite.addTestSuite(RichSuggestionEventTestGwt.class);
		suite.addTestSuite(RichSuggestionRowTestGwt.class);
		suite.addTestSuite(RichSuggestResponseTestGwt.class);
		suite.addTestSuite(RichSuggestBoxTestGwt.class);
		
		suite.addTestSuite(UtilTestGwt.class);
		suite.addTestSuite(ObservableTestGwt.class);
		suite.addTestSuite(FormatsTestGwt.class);
		suite.addTestSuite(QuerySummarizerTestGwt.class);
		
		suite.addTestSuite(StateTestGwt.class);
		suite.addTestSuite(QueryBuildingControllerTestGwt.class);
		suite.addTestSuite(QueryConstraintControllerTestGwt.class);
		suite.addTestSuite(QueryGroupNamesIteratorTestGwt.class);
		suite.addTestSuite(QueryControllerTestGwt.class);

		suite.addTestSuite(TermTestGwt.class);
		suite.addTestSuite(AndTestGwt.class);
		suite.addTestSuite(ExpressionXmlTestGwt.class);
		suite.addTestSuite(QueryGroupTestGwt.class);
		suite.addTestSuite(OrTestGwt.class);
		
		suite.addTestSuite(QueryRowTestGwt.class);
		suite.addTestSuite(QueryTermTestGwt.class);
		
		return suite;
	}
}
