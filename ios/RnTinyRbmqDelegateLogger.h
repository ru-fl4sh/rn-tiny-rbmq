#import <React/RCTBridgeModule.h>

#import "RMQConnectionDelegate.h"
#import "RnTinyRbmqEventEmitter.h"

@interface RnTinyRbmqDelegateLogger : NSObject <RMQConnectionDelegate>

- (nonnull id) init;

@end
