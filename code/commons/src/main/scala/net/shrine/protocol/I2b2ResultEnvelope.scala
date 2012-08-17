package net.shrine.protocol

import scala.xml.NodeSeq
import I2b2ResultEnvelope.Column
import com.sun.org.apache.xalan.internal.xsltc.compiler.ValueOf
import scala.xml.XML
import net.shrine.serialization.XmlMarshaller
import net.shrine.serialization.XmlMarshaller
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.util.XmlUtil
import net.shrine.serialization.I2b2Marshaller
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.serialization.XmlMarshaller

/**
 * @author clint
 * @date Aug 15, 2012
 */
final case class I2b2ResultEnvelope(resultType: ResultOutputType, columns: Seq[Column[_]]) extends I2b2Marshaller with XmlMarshaller {
  import I2b2ResultEnvelope._
  
  def +[T](column: Column[T]): I2b2ResultEnvelope = this.copy(columns = columns :+ column)

  def +[T: I2b2ColumnType](column: (String, T)): I2b2ResultEnvelope = {
    val (name, value) = column

    val columnType = implicitly[I2b2ColumnType[T]].name

    this + Column(columnType, name, value)
  }

  def toMap: Map[String, _] = columns.map(_.toTuple).toMap

  override def toI2b2: NodeSeq = XmlUtil.stripWhitespace(
    <ns10:i2b2_result_envelope xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/hive/msg/result/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/pm/1.1/">
      <body>
        <ns10:result name={ resultType.name }>
          { columns.map(_.toI2b2) }
        </ns10:result>
      </body>
    </ns10:i2b2_result_envelope>)

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(
    <resultEnvelope>
      <resultType>{ resultType }</resultType>
      { columns.map(_.toXml) }
    </resultEnvelope>)
}

object I2b2ResultEnvelope extends I2b2Unmarshaller[Option[I2b2ResultEnvelope]] with XmlUnmarshaller[Option[I2b2ResultEnvelope]] {
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

  final case class Column[T](columnType: String, name: String, value: T) extends I2b2Marshaller with XmlMarshaller {
    def toI2b2: NodeSeq = {
      <data type={ columnType } column={ name }>{ value }</data>
    }

    def toXml: NodeSeq = {
      <column>
        <type>{ columnType }</type>
        <name>{ name }</name>
        <value>{ value }</value>
      </column>
    }

    def toTuple: (String, T) = (name, value)
  }
  
  object Column extends XmlUnmarshaller[Option[Column[_]]] with I2b2Unmarshaller[Option[Column[_]]] {
    private def unmarshal[T](xml: NodeSeq, columnType: NodeSeq => String, name: NodeSeq => String, value: NodeSeq => T): Option[Column[T]] = {
      for(columnType <- I2b2ColumnType.fromI2b2(columnType(xml))) yield Column(columnType.name, name(xml), value(xml))
    }
    
    private def from(attr: String): NodeSeq => String = xml => (xml \ attr).text
    
    override def fromI2b2(xml: NodeSeq): Option[Column[_]] = unmarshal(xml, from("@type"), from("@column"), _.text.toInt)
    
    override def fromXml(xml: NodeSeq): Option[Column[_]] = unmarshal(xml, from("type"), from("name"), from("value") andThen (_.toInt))
  }

  def empty(resultType: ResultOutputType) = new I2b2ResultEnvelope(resultType, Seq.empty)

  override def fromXml(xml: NodeSeq): Option[I2b2ResultEnvelope] = {
    unmarshal(xml, 
              x => tryOrNone(ResultOutputType.valueOf((x \ "resultType").text)), 
              _ \ "column", 
              Column.fromXml)
  }
  
  override def fromI2b2(i2b2Xml: NodeSeq): Option[I2b2ResultEnvelope] = {
    unmarshal(i2b2Xml \ "body" \ "result", 
              x => tryOrNone(ResultOutputType.valueOf((x \ "@name").text)), 
              _ \ "data", 
              Column.fromI2b2)
  }
  
  private def unmarshal(xml: NodeSeq, getResultType: NodeSeq => Option[ResultOutputType], columnXmls: NodeSeq => NodeSeq, toColumn: NodeSeq => Option[Column[_]]): Option[I2b2ResultEnvelope] = {
    for {
      resultType <- getResultType(xml)
      columns = columnXmls(xml).flatMap(toColumn(_))
    } yield I2b2ResultEnvelope(resultType, columns)
  }

  private def tryOrNone[T](f: => T): Option[T] = {
    try {
      Option(f)
    } catch {
      case e: Exception => None
    }
  }
}