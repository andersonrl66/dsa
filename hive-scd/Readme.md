# HIVE SCD

Será feito o tratamento de Slow Changing Dimensions (SCD) para a tabela customer do banco de dados adventureworks. Serão criadas duas tabelas no Hive, customer_update_stage, que será carregada com dados vindos do mysql por intervalo de data usando sqoop, e customer que terá os atributos validFrom e validTo para tratar a alteração de valores. A partir de customer_update_stage é feito o merge na tabela customer utilizando a estratégia tipo 2 de SCD.

1. Criação de tabelas no Hive:
    - customer_update_stage
    
      create external table customer_update_stage
      (customerID int, territoryID int, AccountNumber varchar(10), CustomerType  varchar(1),  rowguid varchar(16), ModifiedDate timestamp)
      row format delimited fields terminated by ',' lines terminated by '\n' stored as textfile
      location '/user/hadoop/tmp/customer_update_stage';
    
    - customer

      create table customer
      (customerID int, territoryID int, accountNumber varchar(10), customerType  varchar(1),  rowguid varchar(16), modifiedDate timestamp, validFrom timestamp, validTo timestamp)
      clustered by (customerID) into 2 buckets stored as orc
      tblproperties("transactional"="true");

2. Script de carga do sqoop

    - Listagem de databases
    
        sqoop list-databases --connect jdbc:mysql://hdpmaster:3306/?serverTimezone=UTC --username root -P
    
    - Importação de dados do mysql para o hive


        sqoop import --connect jdbc:mysql://hdpmaster:3306/adventureworks?serverTimezone=UTC --username root -P \
        --table 'customer' \
        --where 'modifiedDate > STR_TO_DATE("13-10-2004","%d-%m-%Y")'
        -m 1 \
        ‐‐delete‐target‐dir \
        --target-dir '/user/hadoop/tmp/customer_update_stage' \
        --as-textfile \
        --fields-terminated-by ',' \
        --lines-terminated-by '\n'
