# rn-tiny-rbmq

Tiny react native library only for `basicConsume` functional from RabbitMQ.

## Installation

```sh
npm install rn-tiny-rbmq
npx pod-install
```


Change some lines of code to make the library work:
```objc
// Pods/RMQClient/RMQValues.h
// line 54 
// @import JKVValue;
#import "JKVValue.h"


// Pods/RMQClient/RMQTCPSocketTransport.h
// line 56
// @import CocoaAsyncSocket;
#import "GCDAsyncSocket.h"


// Pods/RMQClient/RMQTCPSocketConfigurator.h
// line 55
// @import CocoaAsyncSocket;
#import "GCDAsyncSocket.h"
```

## Usage

```js
import RnTinyRbmq from "rn-tiny-rbmq";

// ...

const config = {
    host: '',
    port: 5672,
    virtualhost: '',
    username: '',
    password: '',
    ttl: 10000,
    ssl: false,
};

const rbmq = new RnTinyRbmq(config);
rmmq.connect();

rbmq.on('connected', (event) => {
    rbmq.basicConsume('queue_name');
});
rbmq.on('error', (event) => {
    // ...
});
rbmq.on('message', (data) => {
    // ...
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
