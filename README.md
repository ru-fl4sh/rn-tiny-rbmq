# rn-tiny-rbmq

Tiny react native library only for `basicConsume` functional from RabbitMQ.

## Installation

```sh
npm install rn-tiny-rbmq
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
rbmq.basicConsume('queue_name');

rbmq.on('connected', (event) => {
    // ...
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
