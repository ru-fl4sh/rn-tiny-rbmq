#import "RnTinyRbmqDelegateLogger.h"

@implementation RnTinyRbmqDelegateLogger

-(id)init {
    self = [super init];
    return self;
}

- (void)channel:(id<RMQChannel>)channel error:(NSError *)error {
    NSLog(@"RabbitMq Received channel: %@ error: %@", channel, error);
    [RnTinyRbmqEventEmitter emitEventWithName:@"RnTinyRbmqEvent" body:@{@"name": @"error", @"type": @"channel",  @"code": [NSString stringWithFormat:@"%ld", error.code], @"description": [error localizedDescription]}];
}

- (void)connection:(RMQConnection *)connection disconnectedWithError:(NSError *)error {
    NSLog(@"RabbitMq Received connection: %@ disconnectedWithError: %@", connection, error);
    [RnTinyRbmqEventEmitter emitEventWithName:@"RnTinyRbmqEvent" body:@{@"name": @"error", @"type": @"disconnected",  @"code": [NSString stringWithFormat:@"%ld", error.code], @"description": [error localizedDescription]}];
}

- (void)connection:(RMQConnection *)connection failedToConnectWithError:(NSError *)error {
    NSLog(@"RabbitMq Received connection: %@ failedToConnectWithError: %@", connection, error);
    [RnTinyRbmqEventEmitter emitEventWithName:@"RnTinyRbmqEvent" body:@{@"name": @"error", @"type": @"failedtoconnect", @"code": [NSString stringWithFormat:@"%ld", error.code], @"description": [error localizedDescription]}];

}

- (void)recoveredConnection:(RMQConnection *)connection {
    [RnTinyRbmqEventEmitter emitEventWithName:@"RnTinyRbmqEvent" body:@{@"name": @"connected"}];
}

- (void)startingRecoveryWithConnection:(RMQConnection *)connection {}

- (void)willStartRecoveryWithConnection:(RMQConnection *)connection {}

@end
