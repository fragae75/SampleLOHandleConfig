package com.test.SampleLOHandleConfig;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;


public class SampleLOHandleConfig {

    public static String API_KEY = MyKey.key; // <-- REPLACE by your API key !
    public static String SERVER = "tcp://liveobjects.orange-business.com:1883";
    public static String DEVICE_URN = "urn:lo:nsid:sensor:SampleLO001";
    public static int KEEP_ALIVE_INTERVAL = 30;

	/*
	 * doSubscribeDeviceTopics() : create a thread that subscribe to device topics
	 */
	public static void subscribeDeviceTopics(String sTopicName, String sAPIKey, String sServerAddress, String sDeviceUrn)
	{
		Thread t;
		RunConsumeConfigs consumeConfigs = new RunConsumeConfigs(sTopicName, sAPIKey, sServerAddress, sDeviceUrn);

		t = new Thread(consumeConfigs);
		t.start();
        System.out.println("Thread : consume Configs" + sTopicName);
	}
	
	/*
	 * 
	 * Publish the device configuration when it is launched
	 * 
	 */
	public static void publishConfig(String sTopic, String sAPIKey, String sServerAddress, String sDeviceUrn) {
		
        // device configuration to announce
        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.cfg.put("logLevel", new DeviceConfig.CfgParameter("str", "LOG"));
        deviceConfig.cfg.put("trigger", new DeviceConfig.CfgParameter("f64", 20.252));
        deviceConfig.cfg.put("connDelaySec", new DeviceConfig.CfgParameter("u32", 10003));
        deviceConfig.cfg.put("min temperature", new DeviceConfig.CfgParameter("i32", 23));

        // encode to JSON
        String CONTENT = new Gson().toJson(deviceConfig);

        try {
            MqttClient sampleClient = new MqttClient(SERVER, DEVICE_URN, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("json+device"); // selecting mode "Device"
            connOpts.setPassword(API_KEY.toCharArray()); // passing API key value as password
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);

            // Connection
            System.out.println("Connecting to broker: " + SERVER);
            sampleClient.connect(connOpts);
            System.out.println("Connected");

            // Publish data
            System.out.println("Publishing message: " + CONTENT);
            MqttMessage message = new MqttMessage(CONTENT.getBytes());
            message.setQos(0);
            sampleClient.publish("dev/cfg", message);
            System.out.println("Message published");

            // Disconnection
            sampleClient.disconnect();
            System.out.println("Client for publish disconnected");

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }

	}

	public static void main(String[] args) {
        
        // Publish a hard coded default device config
        publishConfig (MQTTTopics.MQTT_TOPIC_PUBLISH_CONFIG, API_KEY, SERVER, DEVICE_URN);
      

        // Subscribe to the router : "dev/cfg/upd" to get config updates
        subscribeDeviceTopics(MQTTTopics.MQTT_TOPIC_SUBSCRIBE_CONFIG, API_KEY, SERVER, DEVICE_URN);
 	}

}
