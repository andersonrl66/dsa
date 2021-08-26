create database log_stream_hive_db;

use log_stream_hive_db;

create external table log_stream_hive_tb (
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
	ts_month tinyint,
	ts_day tinyint,
	ts_hour tinyint,
	ts_minute tinyint,
	ts_sec tinyint,
	ts_dayOfWeek tinyint
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
													   log_details_hbase_cf:ts_year,
													   log_details_hbase_cf:ts_month,
													   log_details_hbase_cf:ts_day,
													   log_details_hbase_cf:ts_hour,
													   log_details_hbase_cf:ts_minute,
													   log_details_hbase_cf:ts_sec,
													   log_details_hbase_cf:ts_dayOfWeek")
tblproperties ("hbase.table.name" = "nasa_stream_hbase_ns:log_stream_hbase_tb");

