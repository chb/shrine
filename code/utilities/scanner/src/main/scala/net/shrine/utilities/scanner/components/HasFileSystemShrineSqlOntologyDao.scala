package net.shrine.utilities.scanner.components

import net.shrine.utilities.scanner.Scanner
import net.shrine.ont.data.ShrineSqlOntologyDAO
import java.io.FileInputStream

/**
 * @author clint
 * @date Mar 28, 2013
 */
trait HasFileSystemShrineSqlOntologyDao { self: HasScannerConfig with Scanner =>
  override lazy val ontologyDao = new ShrineSqlOntologyDAO(new FileInputStream(config.ontologySqlFile))
}