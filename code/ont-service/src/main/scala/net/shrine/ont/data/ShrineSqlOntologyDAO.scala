package net.shrine.ont.data

import net.shrine.ont.messaging.Concept
import scala.io.Source
import java.io.FileInputStream
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import java.io.InputStream

/**
 * @author Clint Gilbert
 * @date Feb 8, 2012
 * 
 */
final class ShrineSqlOntologyDAO(val file: InputStream) extends OntologyDAO {
  require(file != null)
  
  override def ontologyEntries: Iterable[Concept] = {
    //Matches VALUES (99, '<full term>', 'synonym', '<is_synonym>'
    //where is_synonym is 'Y' or 'N'
    val pathAndSynonymRegex = """VALUES\s+\(\d+,\s+'(.+?)',\s+'(.+?)',\s+'(\w)'""".r
    
    def toConcept(termMatch: Match): Concept = {
      val synonym = termMatch.group(3) match {
        case "Y" => Option(termMatch.group(2))
        case "N" => None
      }

      val rawPath = termMatch.group(1)
      
      //Need to add '\\SHRINE' that's missing from the SQL file, but needed by i2b2 
      Concept("""\\SHRINE""" + rawPath, synonym)
    }
    
    parseWith(pathAndSynonymRegex, toConcept)
  }
  
  private def parseWith(regex: Regex, parser: Match => Concept): Iterable[Concept] = {
    
    def parseLine(line: String): Option[Concept] = regex.findFirstMatchIn(line).map(parser)
    
    def noEmptyLines(line: String) = line != null && line.trim != ""
      
    def mungeSingleQuotes(line: String) = line.replace("''", "'")
    
    val source = Source.fromInputStream(file)
    
    source.getLines.filter(noEmptyLines).map(mungeSingleQuotes).flatMap(parseLine).toIterable
  }
}
