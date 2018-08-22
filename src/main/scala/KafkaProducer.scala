

import java.util.{Date, Properties}

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer08
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}



object KafkaProducer extends App {

  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val stream: DataStream[String] =env.readTextFile("/home/luoly/test")

  stream.print()

  val myProducer = new FlinkKafkaProducer08[String](
    "localhost:9092",         // broker list
    "topic",               // target topic
    new SimpleStringSchema)   // serialization schema



  stream.addSink(myProducer)



}