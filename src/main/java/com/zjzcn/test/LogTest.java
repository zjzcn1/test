package com.zjzcn.test;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

	private static Logger log4j = Logger.getLogger(LogTest.class);
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(LogTest.class);
	
	public static void main(String[] args) {
			log4j.debug("log4j");
			logger.debug("logger");
	}
}
