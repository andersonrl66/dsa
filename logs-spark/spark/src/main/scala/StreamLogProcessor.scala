import org.apache.spark.SparkConf
import org.apache.spark.sql.kafka010._
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.avro.functions._
import org.apache.spark.streaming._

import org.apache.avro.SchemaBuilder
import java.nio.file.Files;
import java.nio.file.Paths;

//case class StreamLog (host: String, clientAuthId: String, userId: String, method: String, resource: String, protocol:String, responsecode:String, bytes:String, tz: String, ts: String, year: Short, month: Short, day: Short, hour: Short, minute: Short, sec: Short, dayOfWeek: Short)

object StreamLogProcessor  {

	def main(args : Array[String]) : Unit = {
		if (args.length != 3){
			println("Sem argumentos!!! Use <kafkaServers> <kafkaTopic> <topicSchema>")
			return; 
		}

		val kafkaServers = args(0)
		val kafkaTopic = args(1)
    val topicSchema = args(2)

		val spark = SparkSession.builder()
			          .master("local[*]")
                .appName("StreamLogProcessor")
		          	.getOrCreate();
    import spark.implicits._

    val jsonFormatSchema = new String(Files.readAllBytes(Paths.get(topicSchema)))
    val lines = spark
                .readStream
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaServers)
                .option("subscribe", kafkaTopic)
                .load()
                .select(
                      from_avro('value, jsonFormatSchema) as 'value)
                      //from_avro($"key", SchemaBuilder.builder().stringType()).as("key"),
                      //from_avro($"value", SchemaBuilder.builder().intType()).as("value"))
                // from_avro($"host", SchemaBuilder.builder().stringType()).as("host"),
                // from_avro($"clientAuthId", SchemaBuilder.builder().stringType()).as("clientAuthId"),
                // from_avro($"userId", SchemaBuilder.builder().stringType()).as("userId"),
                // from_avro($"method", SchemaBuilder.builder().stringType()).as("method"),
                // from_avro($"resource", SchemaBuilder.builder().stringType()).as("resource"),
                // from_avro($"protocol", SchemaBuilder.builder().stringType()).as("protocol"),
                // from_avro($"responsecode", SchemaBuilder.builder().stringType()).as("responsecode"),
                // from_avro($"bytes", SchemaBuilder.builder().stringType()).as("bytes"),
                // from_avro($"tz", SchemaBuilder.builder().stringType()).as("tz"),
                // from_avro($"ts", SchemaBuilder.builder().stringType()).as("ts"),
                // from_avro($"year", SchemaBuilder.builder().shortType()).as("year"),
                // from_avro($"month", SchemaBuilder.builder().shortType()).as("month"),
                // from_avro($"day", SchemaBuilder.builder().shortType()).as("day"),
                // from_avro($"hour", SchemaBuilder.builder().shortType()).as("hour"),
                // from_avro($"minute", SchemaBuilder.builder().shortType()).as("minute"),
                // from_avro($"sec", SchemaBuilder.builder().shortType()).as("sec"),
                // from_avro($"dayOfWeek", SchemaBuilder.builder().shortType()).as("dayOfWeek"))  

    // val query = lines
    //             .writeStream
    //             .foreach(new HBaseForeachWriter[Row] {
    //                     override val tableName: String = "nasa_stream_hbase_ns:log_stream_hbase_tb"
    //                     override val hbaseConfResources: Seq[String] = Seq("/opt/hadoop/etc/hadoop/core-site.xml", "/opt/hbase/conf/hbase-site.xml") 
                    
    //                     override def toPut(record: Row): Put = {
    //                       val key = Bytes.toBytes(value.getAs[Timestamp]("visit_time").toString.split(" ")(0) + ":" +
    //                                 value.getAs[String]("host"))
    //                       val columnFamaliyName : String = ....
    //                       val columnName : String = ....
    //                       val columnValue = ....
                          
    //                       val p = new Put(Bytes.toBytes(key))


    //                       p.addColumn(Bytes.toBytes(columnFamaliyName),
    //                        Bytes.toBytes(columnName), 
    //                        Bytes.toBytes(columnValue))
                           
    //                       p


    //                       row.addColumn(Bytes.toBytes("cf"),
    //                       Bytes.toBytes(value.getAs[Timestamp]("visit_time").toString),
    //                       Bytes.toBytes(value.getAs[Long]("department_count").toString))
    //                     }
                        
    //                   }
    //               ).start()
    val query = lines.writeStream
                .outputMode("complete")
                .format("console")
                .start() 

    query.awaitTermination()
  }
}
