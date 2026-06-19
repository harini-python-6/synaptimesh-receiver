package com.synaptimesh.receiver

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var mqttClient: MqttAndroidClient

    private val brokerUrl = "tcp://172.18.9.50:1883"
    private val topic = "synaptimesh/commands"

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var confidenceText: TextView
    private lateinit var timeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI references
        statusText = findViewById(R.id.statusText)
        commandText = findViewById(R.id.commandText)
        confidenceText = findViewById(R.id.confidenceText)
        timeText = findViewById(R.id.timeText)

        // Create MQTT client
        val clientId = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(applicationContext, brokerUrl, clientId)

        connectToMQTT()
    }

    private fun connectToMQTT() {
        val options = MqttConnectOptions()
        options.isCleanSession = true

        mqttClient.connect(options, null, object : IMqttActionListener {

            override fun onSuccess(asyncActionToken: IMqttToken?) {
                statusText.text = "Status: ACTIVE"
                subscribeToTopic()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                statusText.text = "Status: DISCONNECTED"
            }
        })
    }

    private fun subscribeToTopic() {
        mqttClient.subscribe(topic, 0, IMqttMessageListener { _, message ->
            val payload = String(message.payload)

            runOnUiThread {
                try {
                    val json = JSONObject(payload)

                    val command = json.getString("command")
                    val confidence = json.getDouble("confidence")
                    val timestamp = json.getString("timestamp")

                    commandText.text = "Command: $command"
                    confidenceText.text = "Confidence: ${(confidence * 100).toInt()}%"
                    timeText.text = "Time: $timestamp"

                } catch (e: Exception) {
                    commandText.text = "Error parsing message"
                }
            }
        })
    }
}
