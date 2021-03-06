package apache.kafka.scala.producers

import java.util.Properties

import com.org.apache.constants.Kafka
import com.org.apache.models.Employee
import com.org.apache.utils.{AvroMessage, ProducerCallback}
import org.apache.avro.generic.GenericData
import org.apache.avro.io.EncoderFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.slf4j.LoggerFactory

import scala.io.Source

object ProducerForAvroschema extends App {

  implicit val logger = LoggerFactory.getLogger(ProducerForAvroschema.getClass)

  //Create Properties
  val properties = new Properties
  properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Kafka.BOOTSTRAPSERVERS)
  properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Kafka.STRING_KEY_SERIALIZER)
  properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Kafka.BYTEARRAY_VALUE_SERIALIZER)
  properties.put(ProducerConfig.RETRIES_CONFIG,"3")

  // Create Producer
  val producer
  = new KafkaProducer[String, Array[Byte]](properties)

  //Prepare data
  val employee = Employee("Edward",101,"321234321",25000)
  val url = getClass.getResource("/avroSchema/employee.avsc")
  val avroJsonSchema = Source.fromURL(url).getLines.mkString
  val avroMessage = new AvroMessage(avroJsonSchema)
  val avroRecord = new GenericData.Record(avroMessage.schema)

  avroRecord.put("name", employee.name)
  avroRecord.put("id", employee.id)
  avroRecord.put("mobileNumber", employee.mobileNumber)
  avroRecord.put("salary", employee.salary)

  avroMessage.gdw.write(avroRecord, EncoderFactory.get().binaryEncoder(avroMessage.baos, null))
  avroMessage.dfw.append(avroRecord)

  avroMessage.dfw.close()
  val bytes = avroMessage.baos.toByteArray

  //send data
  producer.send(new ProducerRecord[String, Array[Byte]](Kafka.EMPLOYEE_TOPIC, bytes), new ProducerCallback)

  //flush data
  producer.flush()
  //flush and close producer
  producer.close()

}
