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
        
  3. Merge de customer_update_stage em customer no hive

    merge into customer
    using (select contacts_update_stage.id as join_key,

contacts_update_stage.* from contacts_update_stage

 union all

— Generate an extra row for changed records.

 — The null join_key forces records down the insert path.

 select

   null, contacts_update_stage.*

 from

   contacts_update_stage join contacts_target

   on contacts_update_stage.id = contacts_target.id

 where

   ( contacts_update_stage.email <> contacts_target.email

     or contacts_update_stage.state <> contacts_target.state )

   and contacts_target.valid_to is null

) sub

on sub.join_key = contacts_target.id

when matched

 and sub.email <> contacts_target.email or sub.state <> contacts_target.state

 then update set valid_to = current_date()

when not matched

 then insert

 values (sub.id, sub.name, sub.email, sub.state, current_date(), null);
