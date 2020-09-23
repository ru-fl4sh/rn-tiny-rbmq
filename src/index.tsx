import { NativeModules } from 'react-native';

type RnTinyRbmqType = {
  multiply(a: number, b: number): Promise<number>;
};

const { RnTinyRbmq } = NativeModules;

export default RnTinyRbmq as RnTinyRbmqType;
