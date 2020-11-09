import {
  NativeModules,
  NativeEventEmitter,
  EmitterSubscription,
} from 'react-native';

interface configType {
  host: string;
  port: number;
  username: string;
  password: string;
  virtualhost: string;
  ttl: number;
  ssl: boolean;
}

type RnTinyRbmqType = {
  initialize(config: configType): void;
  connect(): void;
  basicConsume(queue: string): void;
  close(): void;
};

type callbackType = {
  [index: string]: any;
};

const { RnTinyRbmq, RnTinyRbmqEventEmitter } = NativeModules;

export default class RnTinyRabbitMq {
  callbacks: callbackType;
  rnTinyRbmq: RnTinyRbmqType;
  subscriptions: EmitterSubscription;

  constructor(config: configType) {
    this.callbacks = {};
    this.rnTinyRbmq = RnTinyRbmq;
    const rbmqEmitter = new NativeEventEmitter(RnTinyRbmqEventEmitter);
    this.subscriptions = rbmqEmitter.addListener(
      'RnTinyRbmqEvent',
      this.handleEvent
    );
    this.rnTinyRbmq.initialize(config);
  }

  connect() {
    this.rnTinyRbmq.connect();
  }

  close() {
    this.rnTinyRbmq.close();
  }

  clear() {
    this.subscriptions.remove();
  }

  basicConsume(queue: string) {
    this.rnTinyRbmq.basicConsume(queue);
  }

  handleEvent = (event: (name: string, ...args: any[]) => any) => {
    if (this.callbacks.hasOwnProperty(event.name)) {
      this.callbacks[event.name](event);
    }
  };

  on(event: string, callback: Function) {
    this.callbacks[event] = callback;
  }

  removeon(event: string) {
    delete this.callbacks[event];
  }
}
