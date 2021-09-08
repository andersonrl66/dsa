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

object StreamLogProcessor  {

	def main(args : Array[String]) : Unit = {
		if (args.length != 2){
			println("Sem argumentos!!! Use <kafkaServers> <kafkaTopic>")
			return; 
		}

		val kafkaServers = args(0)
		val kafkaTopic = args(1)

		val spark = SparkSession.builder()
			          .master("local[*]")
                .appName("StreamLogProcessor")
		          	.getOrCreate();
    import spark.implicits._

		val LGREGEXP = "^(.+?)\\s(\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}:\\d{2})([\\+|\\-]\\d{2}:\\d{2})\\s\"(.+?)\\s(.+?)\\s(.+?)\"\\s(.+?)\\s(.+?)$".r

    val lines = spark
                .readStream
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaServers)
                .option("subscribe", kafkaTopic)
                .option("startingOffsets","earliest")
                .load()
                .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")


    val query = lines
                .writeStream
                .foreach(new HBaseForeachWriter[Row] {
                        override val tableName: String = "nasa_stream_hbase_ns:log_stream_hbase_tb"
                        override val hbaseConfResources: Seq[String] = Seq("core-site.xml", "hbase-site.xml") 
                    
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
                                p.addColumn(Bytes.toBytes(columnFamilyName),Bytes.toBytes("bytes"),Bytes.toBytes(bytes))
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
    query.awaitTermination()
  }
}
