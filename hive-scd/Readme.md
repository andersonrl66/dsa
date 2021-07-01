# HIVE SCD

Será feito o tratamento de Slow Changing Dimensions (SCD) para a tabela customer do banco de dados adventureworks. Serão criadas duas tabelas no Hive, customer_update_stage, que será carregada com dados vindos do mysql por intervalo de data usando sqoop, e customer que terá os atributos validFrom e validTo para tratar a alteração de valores. A partir de customer_update_stage é feito o merge na tabela customer utilizando a estratégia tipo 2 de SCD.

  1. Criação de stage no Hive (customer_update_stage):
    
    create external table customer_update_stage
    (customerID int, territoryID int, AccountNumber varchar(10), CustomerType  varchar(1),  rowguid varchar(16), ModifiedDate timestamp)
    row format delimited fields terminated by ',' lines terminated by '\n' stored as textfile
    location '/user/hadoop/tmp/customer_update_stage';
    
  2. Criação de tabela customer

    create table customer
    (customerID int, territoryID int, accountNumber varchar(10), customerType  varchar(1),  
    rowguid varchar(16), modifiedDate timestamp, validFrom timestamp, validTo timestamp)
    clustered by (customerID) into 2 buckets stored as orc
    tblproperties("transactional"="true");

  3. Script de carga do sqoop

    sqoop import --connect jdbc:mysql://hdpmaster:3306/adventureworks?serverTimezone=UTC --username root -P \
    --table 'customer' \
    --where 'modifiedDate > STR_TO_DATE("13-10-2004","%d-%m-%Y")'
    -m 1 \
    ‐‐delete‐target‐dir \
    --target-dir '/user/hadoop/tmp/customer_update_stage' \
    --as-textfile \
    --fields-terminated-by ',' \
    --lines-terminated-by '\n'
        
  4. Merge de customer_update_stage em customer no hive. A cláusula using vai gerar dois registros para cada linha a ser atualizada: um será um insert (null join key) e outro um update da coluna valid_to (join key válida).

    merge into customer
    using
    (
    select customer_update_stage.customerid as join_key,
    customer_update_stage.* from customer_update_stage

    union all
    
    select
    null, customer_update_stage.*
    from
    customer_update_stage join customer
    on customer_update_stage.customerid = customer.customerid
    where
    (
    customer_update_stage.territoryid <> customer.territoryid
    or customer_update_stage.accountnumber <> customer.accountnumber 
    or customer_update_stage.customertype  <> customer.customertype
    or customer_update_stage.rowguid <> customer.rowguid
    or customer_update_stage.modifieddate <> customer.modifieddate
    )
    and customer.validto is null
    ) sub
    on sub.join_key = customer.customerid
    
    when matched
    and sub.territoryid <> customer.territoryid
    or sub.accountnumber <> customer.accountnumber 
    or sub.customertype  <> customer.customertype
    or sub.rowguid <> customer.rowguid
    or sub.modifieddate <> customer.modifieddate
    then update set validto = current_date()

    when not matched
    then insert
    values (sub.customerid, sub.territoryid, sub.accountnumber, sub.customertype, sub.rowguid, sub.modifieddate, current_date(), null);
