package com.rntinyrbmq

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.rabbitmq.client.*

class RnTinyRbmqModule(val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private var isConnected = false
    private var connection: Connection? = null
    private var channel: Channel? = null
    private var consumer: DefaultConsumer? = null
    private val consumerTags = ArrayList<String>()
    private var factory: ConnectionFactory? = null

    override fun getName(): String {
        return "RnTinyRbmq"
    }

    @ReactMethod
    fun initialize(config: ReadableMap) {
      factory = ConnectionFactory()
      factory!!.host = config.getString("host")
      factory!!.port = config.getInt("port")
      factory!!.virtualHost = config.getString("virtualhost")
      factory!!.username = config.getString("username")
      factory!!.password = config.getString("password")
      factory!!.isAutomaticRecoveryEnabled = true
      factory!!.requestedHeartbeat = 10

      if (config.hasKey("ssl") && config.getBoolean("ssl")) {
        factory!!.useSslProtocol()
      }
    }

    @ReactMethod
    fun connect() {
      if (!this.isConnected) {
        this.connection = this.factory!!.newConnection()
        this.channel = this.connection!!.createChannel()
        this.consumer = object : DefaultConsumer(this.channel) {
          override fun handleConsumeOk(consumerTag: String?) {
            super.handleConsumeOk(consumerTag)
            if (consumerTag != null) {
              consumerTags.add(consumerTag)
            }

            this@RnTinyRbmqModule.isConnected = true
            val event = Arguments.createMap()
            event.putString("name", "connected")
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("RnTinyRbmqEvent", event)
          }

          override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
            super.handleShutdownSignal(consumerTag, sig)
            this@RnTinyRbmqModule.isConnected = false
            val event = Arguments.createMap()
            event.putString("name", "error")
            event.putString("type", "disconnected")
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("RnTinyRbmqEvent", event)

            if (consumerTag != null) {
              consumerTags.remove(consumerTag)
            }
          }

          override fun handleCancel(consumerTag: String?) {
            super.handleCancel(consumerTag)
            this@RnTinyRbmqModule.isConnected = false
            val event = Arguments.createMap()
            event.putString("name", "error")
            event.putString("type", "disconnected")
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("RnTinyRbmqEvent", event)

            if (consumerTag != null) {
              consumerTags.remove(consumerTag)
            }
          }

          override fun handleDelivery(consumerTag: String?, envelope: Envelope?, properties: AMQP.BasicProperties?, body: ByteArray?) {
            val event = Arguments.createMap()

            if (body != null) {
              event.putString("name", "message")
              event.putString("message", String(body, Charsets.UTF_8))
              event.putString("routing_key", envelope?.routingKey)
              event.putString("exchange", envelope?.exchange)
              event.putString("consumer_tag", consumerTag)
              if (envelope?.deliveryTag != null) {
                event.putInt("delivery_tag", envelope.deliveryTag.toInt())
              }

              reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("RnTinyRbmqEvent", event)
            }
          }
        }
      }
    }

    @ReactMethod
    fun basicConsume(queue: String) {
      if (this.isConnected) {
        this.channel?.basicConsume(queue, true, this.consumer)
      }
    }

    @ReactMethod
    fun close() {
      if (this.isConnected) {
        consumerTags.forEach {
          this.channel?.basicCancel(it)
        }
        consumerTags.clear()

        this.connection?.close()

        this.connection = null
        this.channel = null
        this.consumer = null
      }
    }

}
