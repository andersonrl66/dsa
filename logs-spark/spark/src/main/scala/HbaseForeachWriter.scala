package logs

import java.util.concurrent.ExecutorService

import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.security.User
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.sql.ForeachWriter

import org.apache.log4j.{Level, LogManager, PropertyConfigurator}

trait HBaseForeachWriter[RECORD] extends ForeachWriter[RECORD] {

  val tableName: String
  val hbaseConfResources: Seq[String]

  def pool: Option[ExecutorService] = None

  def user: Option[User] = None

  private var hTable: Table = _
  private var connection: Connection = _


  override def open(partitionId: Long, version: Long): Boolean = {
    connection = createConnection()
    hTable = getHTable(connection)
    true
  }

  def createConnection(): Connection = {
    val log = LogManager.getRootLogger 
    val hbaseConfig = HBaseConfiguration.create()
    //hbaseConfResources.foreach(hbaseConfig.addResource)
    hbaseConfig.set("hbase.zookeeper.quorum", "hdpmaster,hdpslv1,hdpslv2")
    hbaseConfig.set("hbase.zookeeper.property.clientPort", "2181")
    //hbaseConfig.set("zookeeper.znode.parent", "/hbase-unsecure")
    hbaseConfig.set("hbase.cluster.distributed", "true")
    log.warn(hbaseConfig.toString())
    //ConnectionFactory.createConnection(hbaseConfig, pool.orNull, user.orNull)
    ConnectionFactory.createConnection(hbaseConfig)
  }

  def getHTable(connection: Connection): Table = {
    connection.getTable(TableName.valueOf(tableName))
  }

  override def process(record: RECORD): Unit = {
    val put = toPut(record)
    hTable.put(put)
  }

  override def close(errorOrNull: Throwable): Unit = {
    hTable.close()
    connection.close()
  }

  def toPut(record: RECORD): Put

}