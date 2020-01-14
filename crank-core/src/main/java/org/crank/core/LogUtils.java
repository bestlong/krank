package org.crank.core;

import org.apache.logging.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: richardhightower
 * Date: Apr 29, 2008
 * Time: 12:43:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogUtils {

    public static void debug(Logger logger, String format, Object... args){
        if (logger.isDebugEnabled()){
            logger.debug(String.format(format,args));
        }       
    }

    public static void info(Logger logger, String format, Object... args){
        if (logger.isInfoEnabled()){
            logger.info(String.format(format,args));
        }
    }

}
