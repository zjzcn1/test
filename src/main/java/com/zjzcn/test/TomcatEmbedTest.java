package com.zjzcn.test;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
/** 
 * Created by nil on 2014/8/1. 
 */  
public class TomcatEmbedTest {  
    private Tomcat tomcat;  
    private void startTomcat(int port,String contextPath,String baseDir) throws ServletException, LifecycleException {  
        tomcat = new Tomcat();  
        tomcat.setPort(port);  
        tomcat.setBaseDir(".");  
        StandardServer server = (StandardServer) tomcat.getServer();  
        AprLifecycleListener listener = new AprLifecycleListener();  
        server.addLifecycleListener(listener);  
        tomcat.addWebapp(contextPath, baseDir);  
        tomcat.start();  
        
    }  
    private void stopTomcat() throws LifecycleException {  
        tomcat.stop();  
    }  
    public static void main(String[] args) {  
        try {  
            int port=8080;  
            String contextPath = "/test";  
            String baseDir = "F:/java/workspace3/test";  
            TomcatEmbedTest tomcat = new TomcatEmbedTest();  
            tomcat.startTomcat(port, contextPath, baseDir);  
            //由于Tomcat的start方法为非阻塞方法,加一个线程睡眠模拟线程阻塞.  
            Thread.sleep(10000000);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}  