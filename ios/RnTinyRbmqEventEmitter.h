#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RnTinyRbmqEventEmitter : RCTEventEmitter <RCTBridgeModule>

+ (void)emitEventWithName:(NSString *)name body:(NSDictionary *)body;

@end
