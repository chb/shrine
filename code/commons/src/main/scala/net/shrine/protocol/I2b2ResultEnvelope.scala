package net.shrine.protocol

import scala.xml.NodeSeq
import I2b2ResultEnvelope.Column
import I2b2ResultEnvelope._
import com.sun.org.apache.xalan.internal.xsltc.compiler.ValueOf
import scala.xml.XML

/**
 * @author clint
 * @date Aug 15, 2012
 */
final case class I2b2ResultEnvelope(resultType: ResultOutputType, columns: Seq[Column[_]]) {
  def +[T](column: Column[T]): I2b2ResultEnvelope = this.copy(columns = columns :+ column)

  def +[T: I2b2ColumnType](column: (String, T)): I2b2ResultEnvelope = {
    val (name, value) = column

    val columnType = implicitly[I2b2ColumnType[T]].name
    
    this + Column(columnType, name, value)
  }

  def toMap: Map[String, _] = columns.map(_.toTuple).toMap
  
  //TODO: Produce gross i2b2 semi-escaped form?
  def toI2b2: NodeSeq = {
    <ns10:i2b2_result_envelope xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/hive/msg/result/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/pm/1.1/">
      <body>
        <ns10:result name={ resultType.name }>
          { columns.map(_.toI2b2) }
        </ns10:result>
      </body>
    </ns10:i2b2_result_envelope>
  }
}

object I2b2ResultEnvelope {
  sealed trait I2b2ColumnType[T] {
    def name: String
    
    def fromI2b2(s: String): T
  }

  object I2b2ColumnType {
    implicit object intI2b2ColumnType extends I2b2ColumnType[Int] {
      override def name = "int"
        
      override def fromI2b2(serialized: String): Int = serialized.toInt
    }
    
    def fromI2b2(columnTypeName: String): Option[I2b2ColumnType[_]] = {
      columnTypeName match {
        case "int" => Some(intI2b2ColumnType)
        case _ => None
      }
    }
  }

  final case class Column[T](columnType: String, name: String, value: T) {
    def toI2b2: NodeSeq = {
      <data type={ columnType } column={ name }>{ value }</data>
    }
    
    def toTuple: (String, T) = (name, value)
  }

  def empty(resultType: ResultOutputType) = new I2b2ResultEnvelope(resultType, Seq.empty)
  
  def fromI2b2String(i2b2Xml: String): Option[I2b2ResultEnvelope] = fromI2b2(XML.loadString(I2b2Workarounds.unescape(i2b2Xml)))
  
  def fromI2b2(i2b2Xml: NodeSeq): Option[I2b2ResultEnvelope] = {
    
    val resultBody = i2b2Xml \ "body" \ "result"
    
    val resultTypeOption = tryOrNone(ResultOutputType.valueOf((resultBody \ "@name").text))
    
    val columns = for {
      columnXml <- resultBody \ "data"
      columnType <- I2b2ColumnType.fromI2b2((columnXml \ "@type").text)
      name = (columnXml \ "@column").text
      value = columnXml.text
    } yield Column(columnType.name, name, columnType.fromI2b2(value))

    resultTypeOption.map(I2b2ResultEnvelope(_, columns))
  }
  
  private def tryOrNone[T](f: => T): Option[T] = {
    try {
      Option(f)
    } catch {
      case e: Exception => None
    }
  }
}