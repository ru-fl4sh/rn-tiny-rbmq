#import "RnTinyRbmq.h"

@interface RnTinyRbmq ()
    @property (nonatomic, readwrite) RMQConnection *connection;
    @property (nonatomic, readwrite) id<RMQChannel> channel;
    @property (nonatomic, readwrite) RMQConsumer *consumer;
@end

@implementation RnTinyRbmq

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initialize:(NSDictionary *) config) {
    RnTinyRbmqDelegateLogger *delegate = [[RnTinyRbmqDelegateLogger alloc] init];
    NSString *protocol = @"amqp";
    if ([config objectForKey:@"ssl"] != nil && [[config objectForKey:@"ssl"] boolValue]){
        protocol = @"amqps";
    }
    
    NSString *uri = [NSString stringWithFormat:@"%@://%@:%@@%@:%@/%@", protocol, config[@"username"], config[@"password"], config[@"host"], config[@"port"], config[@"virtualhost"]];

    self.connection = [[RMQConnection alloc] initWithUri:uri channelMax:@65535 frameMax:@(RMQFrameMax) heartbeat:@10 connectTimeout:@15 readTimeout:@30 writeTimeout:@30 syncTimeout:@10 delegate:delegate delegateQueue:dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)];
    
    [self.connection start:^{
        self.channel = [self.connection createChannel];
        [RnTinyRbmqEventEmitter emitEventWithName:@"RnTinyRbmqEvent" body:@{@"name": @"connected"}];
    }];
}

RCT_EXPORT_METHOD(basicConsume:(NSString *) queue) {
    self.consumer = [self.channel basicConsume:queue acknowledgementMode:(RMQBasicConsumeAcknowledgementModeAuto) handler:^(RMQMessage * _Nonnull message) {
        NSString *body = [[NSString alloc] initWithData:message.body encoding:NSUTF8StringEncoding];
               
        [RnTinyRbmqEventEmitter emitEventWithName:@"RnTinyRbmqEvent"
            body:@{
                @"name": @"message",
                @"queue_name": queue,
                @"message": body,
                @"routing_key": message.routingKey,
                @"exchange": message.exchangeName,
                @"consumer_tag": message.consumerTag,
                @"delivery_tag": message.deliveryTag
            }
        ];
    }];
}

RCT_EXPORT_METHOD(close) {
    [self.connection close];
    self.connection = nil;
}

@end
