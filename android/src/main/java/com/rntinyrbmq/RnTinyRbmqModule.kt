package com.rntinyrbmq

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.rabbitmq.client.*

class RnTinyRbmqModule(val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private var connection: Connection? = null
    private var channel: Channel? = null
    private var consumers = ArrayList<DefaultConsumer>()
    private var factory: ConnectionFactory? = null

    override fun getName(): String {
        return "RnTinyRbmq"
    }

    private fun handleDisconnect(message: Throwable?) {
      val event = Arguments.createMap()
      event.putString("name", "error")
      event.putString("type", "disconnected")
      if (message != null) {
        event.putString("message", message.localizedMessage ?: message.message)
      }
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit("RnTinyRbmqEvent", event)
    }

    @ReactMethod
    fun initialize(config: ReadableMap) {
      factory = ConnectionFactory()
      factory!!.host = config.getString("host")
      factory!!.port = config.getInt("port")
      factory!!.virtualHost = config.getString("virtualhost")
      factory!!.username = config.getString("username")
      factory!!.password = config.getString("password")
      factory!!.isAutomaticRecoveryEnabled = false
      factory!!.requestedHeartbeat = 10

      if (config.hasKey("ssl") && config.getBoolean("ssl")) {
        factory!!.useSslProtocol()
      }
      factory!!.exceptionHandler = object:ExceptionHandler {
        override fun handleUnexpectedConnectionDriverException(p0: Connection?, p1: Throwable?) {
          handleDisconnect(p1)
        }

        override fun handleReturnListenerException(p0: Channel?, p1: Throwable?) {
          handleDisconnect(p1)
        }

        override fun handleFlowListenerException(channel: Channel?, exception: Throwable?) {
          handleDisconnect(exception)
        }

        override fun handleConfirmListenerException(p0: Channel?, p1: Throwable?) {
          handleDisconnect(p1)
        }

        override fun handleBlockedListenerException(p0: Connection?, p1: Throwable?) {
          handleDisconnect(p1)
        }

        override fun handleConsumerException(
          p0: Channel?,
          p1: Throwable?,
          p2: Consumer?,
          p3: String?,
          p4: String?
        ) {
          handleDisconnect(p1)
        }

        override fun handleConnectionRecoveryException(p0: Connection?, p1: Throwable?) {
          handleDisconnect(p1)
        }

        override fun handleChannelRecoveryException(p0: Channel?, p1: Throwable?) {
          handleDisconnect(p1)
        }

        override fun handleTopologyRecoveryException(
          p0: Connection?,
          p1: Channel?,
          p2: TopologyRecoveryException?
        ) {}

      }
    }

    @ReactMethod
    fun connect() {
      if (this.connection == null || (this.connection != null && !this.connection!!.isOpen)) {
        try {
          this.connection = this.factory!!.newConnection()
          this.channel = this.connection!!.createChannel()

          val event = Arguments.createMap()
          event.putString("name", "connected")
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("RnTinyRbmqEvent", event)
        } catch (e: Exception) {
          e.printStackTrace()
          val event = Arguments.createMap()
          event.putString("name", "error")
          event.putString("type", "failed connection")
          event.putString("message", e.localizedMessage ?: e.message)
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("RnTinyRbmqEvent", event)
        }
      }
    }

    @ReactMethod
    fun basicConsume(queue: String) {
      if (this.connection != null && this.connection!!.isOpen) {
        val consumer = object : DefaultConsumer(this.channel) {
          override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
            super.handleShutdownSignal(consumerTag, sig)
            handleDisconnect(null)
          }

          override fun handleCancel(consumerTag: String?) {
            super.handleCancel(consumerTag)
            handleDisconnect(null)
          }

          override fun handleDelivery(
            consumerTag: String?,
            envelope: Envelope?,
            properties: AMQP.BasicProperties?,
            body: ByteArray?
          ) {
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

              reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("RnTinyRbmqEvent", event)
            }
          }
        }

        try {
          this.channel?.basicConsume(queue, true, consumer)
          this.consumers.add(consumer)
        } catch (e: Exception) {
          val event = Arguments.createMap()
          event.putString("name", "error")
          event.putString("type", "wrong queue")
          event.putString("message", e.localizedMessage ?: e.message)
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("RnTinyRbmqEvent", event)
        }
      }
    }

    @ReactMethod
    fun close() {
      if (this.connection != null && this.connection!!.isOpen) {
        consumers.forEach {
          try {
            this.channel?.basicCancel(it.consumerTag)
          } catch (e: Exception) {}
        }
        consumers.clear()

        if (this.connection != null && this.connection!!.isOpen) {
          this.connection?.close()
        }

        this.connection = null
        this.channel = null
      }
    }

}
