Implementção de ingestão e tratamento de web server logs utilizando Apache Flume, HBase, Spark e Kafka

1. Base de dados de entrada

   Utilizamos os arquivos dados-entrada/dataaug95 e dados-entrada/dataaug95-2 para simular dois servidores web em [Apache CLF - Common Log Format](https://httpd.apache.org/docs/2.4/logs.html). Estes arquivos são subconjuntos de logs de web servers da Nasa (http://ita.ee.lbl.gov/html/contrib/NASA-HTTP.html)
     
2. Criação de diretórios do flume no servidor
```
mkdir -p /home/hadoop/projetos/dsa/logs-spark
mkdir -p /home/hadoop/projetos/dsa/dados-entrada
mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume
mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel
mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/checkpointDir
mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/dataDir
 ```    
     
3. Configuração de diretórios no HDFS para onde os logs serão levados

``` 
hdfs dfs -mkdir /user/hadoop/logs-spark
hdfs dfs -mkdir /user/hadoop/logs-spark/flume
hdfs dfs -mkdir /user/hadoop/logs-spark/hive
``` 

4. Execução dos agentes flume

``` 
nohup flume-ng agent --name server --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/server.properties &
nohup flume-ng agent --name client1 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/client1.properties & 
nohup flume-ng agent --name client2 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/client2.properties &
```    

5. Criação de base de tabela no hive com os dados dos logs
```
hive -f /home/hadoop/projetos/dsa/logs-spark/hive/criacaoTabelaLogs.hql
```

6. Geração de pacote de processador (spark) dos logs 
```
cd /home/hadoop/projetos/dsa/logs-spark/spark
sbt package
```
7. Execução de processamento de logs e gravação no hive
```
spark-submit \
--class "LogProcessor" \
--master local[*] \
/home/hadoop/projetos/dsa/logs-spark/spark/target/scala-2.12/processador-de-logs-web_2.12-1.0.jar \
/user/hadoop/logs-spark/flume \
/user/hadoop/logs-spark/hive/nasa_processed_logs
```
