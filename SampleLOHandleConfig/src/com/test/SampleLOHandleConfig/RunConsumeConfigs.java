package com.test.SampleLOHandleConfig;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;


/*
 *  
 * Thread that will subscribe to the sTopicName as a device and display the messages to the console
 * 
 */
public class RunConsumeConfigs implements Runnable {

	private String sTopicName;
	private String sAPIKey;
	private String sServerAddress;
    private MqttClient mqttClient = null;
    private String sDeviceUuid;
	/*
	 * Constructor : just keep the topic
	 */
    public RunConsumeConfigs (String sTopicName, String sAPIKey, String sServerAddress, String sDeviceUuid){
		this.sTopicName = sTopicName;
		this.sAPIKey = sAPIKey;
		this.sServerAddress = sServerAddress;
		this.sDeviceUuid = sDeviceUuid;
	}
    
	/*
	 * Make sure we have disconnected
	 */
	public void finalize(){
		
        System.out.println(sTopicName + " - Finalize");
        // close client
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
	            System.out.println(sTopicName + " - Queue Disconnected");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
	}
	
    /**
     * Basic "MqttCallback" that handles messages as JSON device commands,
     * and immediately respond.
     */
    public static class SimpleMqttCallback implements MqttCallback {
        private MqttClient mqttClient;
        private Gson gson = new Gson();
        private Integer counter = 0;

        public SimpleMqttCallback(MqttClient mqttClient) {
            this.mqttClient = mqttClient;
        }

        public void connectionLost(Throwable throwable) {
            System.out.println("Connection lost");
            mqttClient.notifyAll();
        }

        /**
         * Configuration parameter value: must match the parameter type:
         *  str: String
         *  bin: Base64 encoded string
         *  f64: Double
         *  u32 : Long
         *  i32 : Integer
         */

        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            // parse message as command
            DeviceConfig config = gson.fromJson(new String(mqttMessage.getPayload()), DeviceConfig.class);
            System.out.println("received config: " + gson.toJson(config));
            
        	// Strange bug for u32 & i32 values : received as long and integer values but converted as Double 
        	// by gson.fromJson() 10233 becomes 10233.0 !!! => need to rebuild and convert into configResponse
            DeviceConfig configResponse = new DeviceConfig();
            for (Map.Entry<String, DeviceConfig.CfgParameter> entry : config.cfg.entrySet())
            {
            	if ( entry.getValue().t.equals("u32") ) {
                	configResponse.cfg.put(entry.getKey(), new DeviceConfig.CfgParameter(entry.getValue().t, (long)Float.parseFloat(entry.getValue().v.toString()) ));
            	}
            	else if ( entry.getValue().t.equals("i32") ) {
                   	configResponse.cfg.put(entry.getKey(), new DeviceConfig.CfgParameter(entry.getValue().t, (int)Float.parseFloat(entry.getValue().v.toString())));
                    	
                }
            	else {
                	configResponse.cfg.put(entry.getKey(), new DeviceConfig.CfgParameter(entry.getValue().t, entry.getValue().v));
            	}
            }
            configResponse.cid = config.cid;


            // Publish on the config topic : MQTTTopics.MQTT_TOPIC_RESPONSE_CONFIG
            new Thread(new Runnable() {
                public void run() {
                    try {
                    	mqttClient.publish(MQTTTopics.MQTT_TOPIC_RESPONSE_CONFIG, gson.toJson(configResponse).getBytes(), 0, false);
        	            System.out.println("answer to config on: " + MQTTTopics.MQTT_TOPIC_RESPONSE_CONFIG + " : " + gson.toJson(configResponse));
                    } catch (MqttException me) {
                        System.out.println("reason " + me.getReasonCode());
                        System.out.println("msg " + me.getMessage());
                        System.out.println("loc " + me.getLocalizedMessage());
                        System.out.println("cause " + me.getCause());
                        System.out.println("excep " + me);
                        me.printStackTrace();
                    }
                }
            }).start();
            
        }

        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            //System.out.println("Delivery complete");
        }
    }

    
	@Override
	public void run() {
        String APP_ID = sDeviceUuid;

        MqttClient mqttClient = null;
        try {
            mqttClient = new MqttClient(sServerAddress, APP_ID, new MemoryPersistence());

            // register callback (to handle received commands
            mqttClient.setCallback(new SimpleMqttCallback(mqttClient));

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("json+device"); // selecting mode "device"
            connOpts.setPassword(sAPIKey.toCharArray()); // passing API key value as password
            connOpts.setCleanSession(true);

            // Connection
            System.out.printf("Subscribe as a device - Connecting to broker: %s ...\n", sServerAddress);
            mqttClient.connect(connOpts);
            System.out.println("Subscribe as a device ... connected.");

            // Subscribe to data
            System.out.printf("Consuming from device with filter '%s'...\n", sTopicName);
            mqttClient.subscribe(sTopicName);
            System.out.println("... subscribed.");

            synchronized (mqttClient) {
                mqttClient.wait();
            }
        } catch (MqttException | InterruptedException me) {
            me.printStackTrace();

        } finally {
            // close client
            if (mqttClient != null && mqttClient.isConnected()) {
                try {
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
	}

}
