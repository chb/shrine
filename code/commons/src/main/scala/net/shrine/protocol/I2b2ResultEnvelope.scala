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
import net.shrine.serialization.JsonMarshaller
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.shrine.serialization.JsonUnmarshaller
import net.shrine.util.Try


/**
 * @author clint
 * @date Aug 15, 2012
 */
final case class I2b2ResultEnvelope(resultType: ResultOutputType, data: Map[String, Long]) extends I2b2Marshaller with XmlMarshaller with JsonMarshaller {
  import I2b2ResultEnvelope._
  
  //Extra parameter list with dummy int value needed to disambiguate this constructor and the class-level one, which without
  //the extra param list have the same signature after erasure. :/  Making the dummy param implicit lets us omit the second 
  //param list entirely when calling this constructor.
  def this(resultType: ResultOutputType, cols: (String, Long)*)(implicit dummy: Int = 42) = this(resultType, cols.toMap)
  
  def this(resultType: ResultOutputType, columns: Seq[Column]) = this(resultType, columns.map(_.toTuple).toMap)
  
  def +(column: Column): I2b2ResultEnvelope = this + column.toTuple

  def +(column: (String, Long)): I2b2ResultEnvelope = {
    this.copy(data = data + column)
  }

  def ++(columns: Iterable[(String, Long)]): I2b2ResultEnvelope = {
    columns.foldLeft(this)(_ + _)
  }
  
  def toMap: Map[String, Long] = data.mapValues(_.toLong)
  
  def columns: Seq[Column] = data.map(Column.fromTuple).toSeq

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
    
  override def toJson: JValue = {
    (resultType.name -> columns.map(_.toTuple).toMap)
  } 
}

object I2b2ResultEnvelope extends I2b2Unmarshaller[Option[I2b2ResultEnvelope]] with XmlUnmarshaller[Option[I2b2ResultEnvelope]] {

  final case class Column(name: String, value: Long) extends I2b2Marshaller with XmlMarshaller with JsonMarshaller {
    def toI2b2: NodeSeq = {
      <data type="int" column={ name }>{ value }</data>
    }

    def toXml: NodeSeq = {
      <column>
        <name>{ name }</name>
        <value>{ value }</value>
      </column>
    }

    def toTuple: (String, Long) = (name, value)
    
    override def toJson: JValue = toTuple 
  }
  
  object Column extends XmlUnmarshaller[Option[Column]] with I2b2Unmarshaller[Option[Column]] with JsonUnmarshaller[Option[Column]] {
    def fromTuple(tuple: (String, Long)): Column = {
      val (name, value) = tuple
      
      Column(name, value)
    }
    
    private def unmarshal(xml: NodeSeq, name: NodeSeq => String, value: NodeSeq => Long): Option[Column] = {
      Some(Column(name(xml), value(xml)))
    }
    
    private def from(attr: String): NodeSeq => String = xml => (xml \ attr).text
    
    override def fromI2b2(xml: NodeSeq): Option[Column] = unmarshal(xml, from("@column"), _.text.toInt)
    
    override def fromXml(xml: NodeSeq): Option[Column] = unmarshal(xml, from("name"), from("value") andThen (_.toInt))
    
    override def fromJson(json: JValue): Option[Column] = json match {
      case JObject(List(JField(name, JInt(value)))) => Some(Column(name, value.toInt))
      case _ => None
    }
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
  
  private def unmarshal(xml: NodeSeq, getResultType: NodeSeq => Option[ResultOutputType], columnXmls: NodeSeq => NodeSeq, toColumn: NodeSeq => Option[Column]): Option[I2b2ResultEnvelope] = {
    for {
      resultType <- getResultType(xml)
      columns = columnXmls(xml).flatMap(toColumn(_))
    } yield new I2b2ResultEnvelope(resultType, columns)
  }

  private def tryOrNone[T](f: => T): Option[T] = {
    try {
      Option(f)
    } catch {
      case e: Exception => None
    }
  }
}