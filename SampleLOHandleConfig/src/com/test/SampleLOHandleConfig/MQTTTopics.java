package com.test.SampleLOHandleConfig;

public class MQTTTopics {

    // Mqtt topics
    public static final String MQTT_TOPIC_PUBLISH_STATUS = "dev/info";
    public static final String MQTT_TOPIC_PUBLISH_DATA = "dev/data";

    public static final String MQTT_TOPIC_PUBLISH_RESOURCE = "dev/rsc";
    public static final String MQTT_TOPIC_SUBSCRIBE_RESOURCE = "dev/rsc/upd";
    public static final String MQTT_TOPIC_RESPONSE_RESOURCE = "dev/rsc/upd/res";

    public static final String MQTT_TOPIC_SUBSCRIBE_COMMAND = "dev/cmd";
    public static final String MQTT_TOPIC_RESPONSE_COMMAND = "dev/cmd/res";

    public static final String MQTT_TOPIC_PUBLISH_CONFIG = "dev/cfg";
    public static final String MQTT_TOPIC_SUBSCRIBE_CONFIG = "dev/cfg/upd";
    public static final String MQTT_TOPIC_RESPONSE_CONFIG = "dev/cfg";//"dev/cfg"; //"dev/cfg/upd/res";

}
