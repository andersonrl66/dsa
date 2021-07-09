Implementção de ingestão e tratamento de web server logs utilizando Apache Flume, HBase, Spark e Kafka

1. Base de dados

   Utilizamos os arquivos dados-entrada/dataaug95 e dados-entrada/dataaug95-2 para simular dois servidores web em [Apache CLF - Common Log Format](https://httpd.apache.org/docs/2.4/logs.html). Estes arquivos são subconjuntos de logs de web servers da Nasa (http://ita.ee.lbl.gov/html/contrib/NASA-HTTP.html)
     
2. Criação de diretórios do flume no servidor

   mkdir -p /home/hadoop/projetos/dsa/logs-spark
   mkdir -p /home/hadoop/projetos/dsa/dados-entrada
   mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume
   mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel
   mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/checkpointDir
   mkdir -p /home/hadoop/projetos/dsa/logs-spark/flume/file-channel/dataDir
   
3. Configuração de diretórios no HDFS para onde os logs serão levados

   hdfs dfs -mkdir /user/hadoop/logs-spark
   
4. Execução dos agentes flume

   nohup flume-ng agent --name server --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/server.properties &
   
   nohup flume-ng agent --name client1 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/client1.properties &
   
   nohup flume-ng agent --name client2 --conf-file /home/hadoop/projetos/dsa/logs-spark/flume/client2.properties &
   
