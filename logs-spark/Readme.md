Implementção de ingestão e tratamento de web server logs utilizando Apache Flume, HBase, Spark e Kafka

 Utilizamos os arquivos dados-entrada/dataaug95 e dados-entrada/dataaug95-2 para simular dois servidores web em [Apache CLF - Common Log Format](https://httpd.apache.org/docs/2.4/logs.html). Estes arquivos são subconjuntos de logs de web servers da Nasa (http://ita.ee.lbl.gov/html/contrib/NASA-HTTP.html). Fizemos duas simulações : batch e streamig. Na execução batch os dados são colocados no hive, enquanto no streaming os dados são colocados no hbase.

1. Batch

    logs --> flume --> spark --> hive

    1.1 Criação de diretórios do flume no servidor
    ```
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/dados-entrada/client1
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/dados-entrada/client2
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/checkpointDir
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/dataDir
     ```    
         
    1.2 Configuração de diretórios no HDFS para onde os logs serão levados
    
    ``` 
    hdfs dfs -mkdir /user/hadoop/logs-spark
    hdfs dfs -mkdir /user/hadoop/logs-spark/flume
    hdfs dfs -mkdir /user/hadoop/logs-spark/hive
    ``` 
    
    1.3 Execução dos agentes flume

    ``` 
    nohup flume-ng agent --name server --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/server.properties &
    nohup flume-ng agent --name client1 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/client1.properties & 
    nohup flume-ng agent --name client2 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/client2.properties &
    ```    
    
    1.4 Criação de base de tabela no hive com os dados dos logs
    ```
    hive -f /home/hadoop/projetos/dsa/logs-spark/hive/criacaoTabelaLogs.hql
    ```
    
    1.5 Geração de pacote de processador (spark) dos logs 
    ```
    cd /home/hadoop/projetos/dsa/logs-spark/spark
    sbt package
    ```
    1.6 Execução de processamento de logs e gravação no hive
    ```
    spark-submit \
    --class "LogProcessor" \
    --master local[*] \
    /home/hadoop/projetos/dsa/logs-spark/spark/target/scala-2.12/processador-de-logs-web_2.12-1.0.jar \
    /user/hadoop/logs-spark/flume \
    /user/hadoop/logs-spark/hive/nasa_processed_logs
    ```
2. Streaming

    logs --> flume --> kafka --> spark --> hbase

    2.1 Criação de diretórios do flume no servidor
    ```
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/dados-entrada/stream/client1
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/dados-entrada/stream/client2
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/stream/checkpointDir
    mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/stream/dataDir
     ```    
    2.2 Execução do zookeeper e kafka
    ```
    zkServer.sh start
    nohup /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties > kafka.log &
    ```
    2.3 Criação de tópico no kafka
    ```    
    kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic logs-dsa
    ```
    2.4 Execução dos agentes flume
    ``` 
    nohup flume-ng agent --name server --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/stream_server.properties &
    nohup flume-ng agent --name client1 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/stream_client1.properties & 
    nohup flume-ng agent --name client2 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/stream_client2.properties &
    ```    
    
    2.5 Criação de tabela no hbase shell
    ```
    hbase shell
    create_namespace 'nasa_stream_hbase_ns'
    create 'nasa_stream_hbase_ns:log_stream_hbase_tb', 'log_details_hbase_cf'
    ```
    2.6 Criação do esquema da tabela utilizando o hive
    ```
    hive -f /home/hadoop/projetos/dsa/logs-spark/hive/criacaoTabelaLogsStream.hql
    ```
    2.7 Geração de pacote de processador (spark) dos logs 
    ```
    cd /home/hadoop/projetos/dsa/logs-spark/spark
    sbt package
    ```
    2.8 Execução de processamento de logs e gravação no hive
    ```
    spark-submit \
    --class "logs.StreamLogProcessor" \
    --master local[*] \
    --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.1.1,org.apache.spark:spark-avro_2.12:3.1.1,\
    org.apache.hbase:hbase-client:2.4.5,org.apache.hbase:hbase-common:2.4.5\
    /home/hadoop/projetos/dsa/logs-spark/spark/target/scala-2.12/processador-de-logs-web_2.12-1.0.jar \
    hdpmaster:9092 \
    logs-dsa
    ```