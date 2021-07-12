create database if not exists logs_spark;

use logs_spark;

create external table log (
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
stored as parquet
location '/user/hadoop/dsa/logs-spark/hive/nasa_processed_logs';
