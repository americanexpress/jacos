package com.americanexpress.jacos.examples;

import com.americanexpress.jacos.bulkv2.Bulk2ClientJacos;
import com.americanexpress.jacos.bulkv2.request.OperationInfo;
import com.americanexpress.jacos.bulkv2.type.OperationEnumJacos;
import com.americanexpress.jacos.service.BulkApi2Operations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see Bulk2ClientJacos
 * @since 1.0.0
 */
@Component
public class BulkClientExamples {

    private static final Logger LOGGER = LogManager.getLogger(BulkClientExamples.class);
    @Autowired
    private BulkApi2Operations bulkApi2Operations;

    /**
     * Example to insert multiple records in salesforce from csv string
     */
    public void insert() {
        LOGGER.info("***Executing insert");
        String[] args = new String[]{"Lead", "insert", "na"};
        OperationInfo operationInfo = new OperationInfo(args[0], OperationEnumJacos.valueOf(args[1].toUpperCase()), args[2]);
        //For insert
        String text = "Company,LastName,Phone\n" +
                "March28TestComapany_v1,abc,4806696411\n" +
                "March28TestComapany2_v1,def,4806696411\n";

       LOGGER.info("Job Result:{}",bulkApi2Operations.performSFOperation(operationInfo, text).toString());
    }

    /**
     * Example to insert multiple records in salesforce from csv file
     */
    public void insertFromFile() {
        LOGGER.info("***Executing insert");
        String[] args = new String[]{"Vendor__c", "insert", "na"};
        OperationInfo operationInfo = new OperationInfo(args[0], OperationEnumJacos.valueOf(args[1].toUpperCase()), args[2]);
        //For insert
        String text = null;

        LOGGER.info("Job Result:{}",bulkApi2Operations.performSFOperationFromFile(operationInfo, Thread.currentThread().getContextClassLoader().
                getResource("insert.csv").getPath()).toString());
    }

    /**
     * Example to update multiple records in salesforce using id and csv string
     *
     */
    public void update(){
        LOGGER.info("***Executing update");
    }

    /**
     * TODO
     *
     */
    public void multiUpdate(){
        LOGGER.info("***Executing multiUpdate");
    }

    /**
     * TODO
     *
     */

    public void delete(){
        LOGGER.info("***Executing delete");
    }
}
