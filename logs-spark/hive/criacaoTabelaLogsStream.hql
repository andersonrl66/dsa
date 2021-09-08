create database if not exists logs_spark;

use logs_spark;

create external table if not exists log_stream_hive_tb (
	id string,
	host string, 
	clientAuthId string, 
	userId string, 
	method string, 
	resource string, 
	protocol string, 
	responsecode string, 
	bytes string, 
	tz string, 
	ts string,
	ts_year smallint,
	ts_month smallint,
	ts_day smallint,
	ts_hour smallint,
	ts_minute smallint,
	ts_sec smallint,
	ts_dayOfWeek smallint
)
stored by 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
with serdeproperties ("hbase.columns.mapping" = ":key, log_details_hbase_cf:host, 
                                                       log_details_hbase_cf:clientAuthId, 
													   log_details_hbase_cf:userId,
													   log_details_hbase_cf:method,
													   log_details_hbase_cf:resource,
													   log_details_hbase_cf:protocol,
													   log_details_hbase_cf:responsecode,
													   log_details_hbase_cf:bytes,
													   log_details_hbase_cf:tz,
													   log_details_hbase_cf:ts,
													   log_details_hbase_cf:ts_year#b,
													   log_details_hbase_cf:ts_month#b,
													   log_details_hbase_cf:ts_day#b,
													   log_details_hbase_cf:ts_hour#b,
													   log_details_hbase_cf:ts_minute#b,
													   log_details_hbase_cf:ts_sec#b,
													   log_details_hbase_cf:ts_dayOfWeek#b")
tblproperties ("hbase.table.name" = "nasa_stream_hbase_ns:log_stream_hbase_tb");

