package com.zjzcn.test;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

public class MqttClientTest {

    private  static Logger logger = LoggerFactory.getLogger(MqttClientTest.class);

    public static void main(String[] args) throws IOException {
        /**
         * 设置当前用户私有的 MQTT 的接入点。例如此处示意使用 XXX，实际使用请替换用户自己的接入点。接入点的获取方法是，在控制台创建 MQTT 实例，每个实例都会分配一个接入点域名。
         */
        final String broker = "tcp://127.0.0.1:8000";
        final String topic = "/sensor/beacon";
        final String clientId = "ClientID_XXXX";
        try {
            final MqttClient sampleClient = new MqttClient(broker, clientId, new MemoryPersistence());
            final MqttConnectOptions connOpts = new MqttConnectOptions();
            System.out.println("Connecting to broker: " + broker);
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(90);
            connOpts.setAutomaticReconnect(true);
            connOpts.setMqttVersion(MQTT_VERSION_3_1_1);
            sampleClient.setCallback(new MqttCallbackExtended() {
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("connect success");
                    try {
                        sampleClient.subscribe(topic, new IMqttMessageListener() {
                            @Override
                            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                logger.info(new String(mqttMessage.getPayload()));
                                System.out.println(topic + "    " + new String(mqttMessage.getPayload()));
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                public void connectionLost(Throwable throwable) {
                    System.out.println("mqtt connection lost");
                }
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    System.out.println("messageArrived:" + topic + "------" + new String(mqttMessage.getPayload()));
                }
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    System.out.println("deliveryComplete:" + iMqttDeliveryToken.getMessageId());
                }
            });
            sampleClient.connect(connOpts);
        } catch (Exception me) {
            me.printStackTrace();
        }
    }
}
