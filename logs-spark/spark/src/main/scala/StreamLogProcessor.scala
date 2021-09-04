package logs

import org.apache.spark.SparkConf
import org.apache.spark.sql.kafka010._
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql.execution.streaming.FileStreamSource.Timestamp
import org.apache.spark.streaming._
import org.apache.avro.SchemaBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.hadoop.hbase.util.Bytes
//import org.apache.hadoop.hbase.client.Put

import java.util.concurrent.ExecutorService

import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.security.User
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.sql.ForeachWriter

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

		val LGREGEXP = "^(.+?)\\s(\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}:\\d{2})([\\+|\\-]\\d{2}:\\d{2})\\s\"(.+?)\\s(.+?)\\s(.+?)\"\\s(.+?)\\s(.+?)$".r
    val jsonFormatSchema = new String(Files.readAllBytes(Paths.get(topicSchema)))

    val lines = spark
                .readStream
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaServers)
                .option("subscribe", kafkaTopic)
                .option("startingOffsets","earliest")
                .load()
                .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
                //.as[(String, String)]
                //.select(         
                      //from_avro('value, jsonFormatSchema) as 'value)
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

    val query = lines
                .writeStream
                .foreach(new HBaseForeachWriter[Row] {
                        override val tableName: String = "nasa_stream_hbase_ns:log_stream_hbase_tb"
                        override val hbaseConfResources: Seq[String] = Seq("/opt/hadoop/etc/hadoop/core-site.xml", "/opt/hbase/conf/hbase-site.xml") 
                    
                        override def toPut(value: Row): Put = {

                          val line = value.getAs[String]("value")

                          line match {
                       				case LGREGEXP(host, ts, tz, method, resource, protocol, responsecode, bytes) => {

                           			val cal  = Calendar.getInstance
                          			val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                val clientAuthId = ""
                                val userId = ""
                                cal.setTime(sdf.parse(ts))
                                val year = cal.get(Calendar.YEAR).toShort
                                val month = cal.get(Calendar.MONTH).toShort
                                val day= cal.get(Calendar.DAY_OF_MONTH).toShort
                                val hour = cal.get(Calendar.HOUR).toShort
                                val min = cal.get(Calendar.MINUTE).toShort
                                val sec = cal.get(Calendar.SECOND).toShort
                                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK).toShort
						
                                val key : String = ts + ":" + host
                                val columnFamilyName : String = "log_details_hbase_cf"

                                val p = new Put(Bytes.toBytes(key))

                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("host"),Bytes.toBytes(host))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("clientAuthId"),Bytes.toBytes(clientAuthId))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("userId"),Bytes.toBytes(userId))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("method"),Bytes.toBytes(method))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("resource"),Bytes.toBytes(resource))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("protocol"),Bytes.toBytes(protocol))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("responsecode"),Bytes.toBytes(responsecode))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("tz"),Bytes.toBytes(tz))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts"),Bytes.toBytes(ts))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts_year"),Bytes.toBytes(year))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts_month"),Bytes.toBytes(month))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts_day"),Bytes.toBytes(day))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts_hour"),Bytes.toBytes(hour))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts_minute"),Bytes.toBytes(min))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts_sec"),Bytes.toBytes(sec))
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("ts_dayOfWeek"),Bytes.toBytes(dayOfWeek))
                                
                                p
					                    }
                          }

                        }
                       
                      }
                  ).start()
    // val query = lines.writeStream
    //             .outputMode("append")
    //             .format("console")
    //             .start() 

    query.awaitTermination()
  }
}
