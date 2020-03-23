package com.americanexpress.jacos.examples;

import com.americanexpress.jacos.service.PartApi2Operations;
import com.americanexpress.jacos.utils.RestUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @since 1.0.0
 *
 * @see com.americanexpress.jacos.part.PartClient
 *
 */
@Component
public class PartClientExamples {
    @Autowired
    private PartApi2Operations partApi2Operations;

    private static final Logger LOGGER = LogManager.getLogger(RestUtils.class);


    public void insert() {

    }

    /**
     * TODO
     */
    public void multiInsert() {
        LOGGER.info("***Executing multiInsert");

    }

    /**
     * TODO
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
