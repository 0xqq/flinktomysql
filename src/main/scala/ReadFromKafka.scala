
import java.util.{Collections, Properties}

import org.apache.flink._
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSink
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer08, FlinkKafkaProducer08}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat
import java.sql.{DriverManager, Types}

import org.apache.flink.streaming.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.table.api.{Table, TableEnvironment}
import org.apache.flink.table.sinks.{AppendStreamTableSink, CsvTableSink, TableSink}
import org.apache.flink.table.sources.CsvTableSource


object ReadFromKafka extends Object{

@Deprecated
  def tst(): Unit ={

      val driver = "com.mysql.jdbc.Driver"
      val database = "jdbc:mysql://47.52.238.90:3306/wuxh?characterEncoding=utf8"
      val username = "root"
      val pwd = "123456"
      val coon = DriverManager.getConnection(database, username, pwd)
      val stmt = coon.createStatement
      val res = stmt.executeQuery("select * from songer;")
      if (res != null){
        println("not null")
        var index:Int = 0
        while (res.next()){
          index += 1
          print(index)
          res
        }
      }

    }



  def JDBCOut()={

    val JDBC = JDBCOutputFormat
      .buildJDBCOutputFormat
      .setDBUrl(String.format("jdbc:mysql//47.52.238.90/luoly/songer"))
      .setDrivername("org.apache.derby.jdbc")
      .setUsername("mysql")
      .setPassword("123456")
      .setQuery(String.format("insert into songer(songerID) values %s", "testdata"))
      .setSqlTypes(Array[Int](Types.VARCHAR))
      //.setBatchInterval(JDBC_BATCH_SIZE)
      .finish

    JDBC
  }



  def main(args:Array[String]) {

   // implicit val typeInfo = TypeInformation.of(classOf[(String)])

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val batchEnv=ExecutionEnvironment.getExecutionEnvironment      //batch

    val tableEnv = TableEnvironment.getTableEnvironment(env)

    env.enableCheckpointing(500)

    val stream =env.readTextFile("/home/luoly/test")

    stream.print()

    val myProducer = new FlinkKafkaProducer08[String](
      "localhost:9092",         // broker list
      "topic",               // target topic
      new SimpleStringSchema)   // serialization schema

    stream.addSink(myProducer)

    val props = new Properties()
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.setProperty("zookeeper.connect", "localhost:2181")
    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "groupid")
    props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")



    val message= env.addSource(new FlinkKafkaConsumer08("topic", new SimpleStringSchema(), props))
        .flatMap(_.toLowerCase())
/*
      方法二
      .setParallelism(2)
      .map((_,1))
      .writeUsingOutputFormat(JDBCOut())
    //(JDBCOut())

*/

    // register the DataStream message as table "songer" with fields derived from the datastream
    tableEnv.registerDataStream("songer", message)

    val sink: JDBCAppendTableSink = JDBCAppendTableSink.builder()
      .setDrivername("org.apache.derby.jdbc")
      .setDBUrl("jdbc:mysql//47.52.238.90/luoly/songer")
      .setUsername("mysql")
      .setPassword("123456")
      .setQuery("insert into songer(songerID) values(?)")
      .setParameterTypes(2)
      .build()

    val table: Table = tableEnv.fromDataStream(stream)
        table.writeToSink(sink)







    println(table)

    env.execute("Stock stream")

  }

}