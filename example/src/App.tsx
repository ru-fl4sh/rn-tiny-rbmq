import * as React from 'react';
import { StyleSheet, View } from 'react-native';
import RnTinyRbmq from 'rn-tiny-rbmq';

const rbmq = new RnTinyRbmq({
  host: '',
  port: 5672,
  virtualhost: '',
  username: '',
  password: '',
  ttl: 10000,
  ssl: false,
});

export default function App() {
  React.useEffect(() => {
    rbmq.connect();

    rbmq.on('connected', () => {
      console.log('CONNECTED!');
      rbmq.basicConsume('');
    });
    rbmq.on('error', () => {
      console.log('ERROR!');
    });
    rbmq.on('message', (data: any) => {
      console.log(data);
    });

    return function cleanup() {
      rbmq.close();
      rbmq.removeon('connected');
      rbmq.removeon('error');
      rbmq.removeon('message');
    };
  }, []);

  return <View style={styles.container} />;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
