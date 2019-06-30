# SampleLOHandleConfig

Sample application for Datavenue Live Objects : https://liveobjects.orange-business.com/#/liveobjects

It is a simple sample that collect/answer configuration updates from Live Objects as a MQTT device ("json+device").

The sample will be visible in the Live Objects Park as the sensor SampleLO001. Select it and send it a config update (menu "Parameters" on the left side). The sample will publish on the dev/cfg topic the following configuration: <br>

	{
	"cfg":
		{
			"logLevel":{"t":"str","v":"LOG"},
			"min temperature":{"t":"i32","v":23},
			"connDelaySec":{"t":"u32","v":10003},
			"trigger":{"t":"f64","v":20.252}
		}
	}

And it will subscribe on the dev/cfg/upd topic

You can update the configuration through the portal (menu "Parameters") : select a value, change it then click on "Send changes" button. <br>
The configuration update API are available through the swagger (https://liveobjects.orange-business.com/#/faq, menu "Developer guide") at the entry "Device management parameter"<br>

In order to validate the update, the sample will reply the new config on the topic dev/cfg. It it doesn't, the platform will try to send the update again.


<h2> Installation notes </h2>

1) Create an account on Live Objects. You can get a free account (10 MQTT devices for 1 year) at : https://liveobjects.orange-business.com/#/request_account <br>
Don't check "Lora" otherwise the account will not be instantly created.

2) Generate your API key : menu Configuration/API Keys click on "Add"

3) Create a MyKey class : <br>


	package com.test.SampleLOSendData; 
	
	public final class MyKey { 
		static String key = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; 
	}


4) You will find into the repository 4 jar files into the /lib. Add them as "external JARs" into you IDE (eg Eclipse).
