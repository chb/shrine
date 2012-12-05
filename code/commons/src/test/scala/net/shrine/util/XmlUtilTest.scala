package net.shrine.util

import junit.framework.TestCase
import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import xml.XML

/**
 * @author Bill Simons
 * @date 2/14/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class XmlUtilTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  @Test
  def testStripWhitespace {
    val node = XML.loadString("<foo>\n\t<bar>  baz     </bar>\n</foo>")
    
    XmlUtil.stripWhitespace(node).toString() should equal("<foo><bar>  baz     </bar></foo>")
  }

  @Test
  def testRenameRootTag {
    val xml = <foo><bar><baz/></bar></foo>
      
    val expected = <blarg><bar><baz/></bar></blarg>
      
    XmlUtil.renameRootTag("blarg")(xml).toString should equal(expected.toString)
  }
}