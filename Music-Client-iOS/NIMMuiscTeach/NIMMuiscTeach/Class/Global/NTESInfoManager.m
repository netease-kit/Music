//
//  NTESInfoManager.m
//  NIMDragonClaw
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESInfoManager.h"
#import "NTESService.h"
#import "NSDictionary+NTESJson.h"
#import "NTESDDLogManager.h"

@interface NTESInfoManager()<NIMChatManagerDelegate, NIMSystemNotificationManagerDelegate, NIMChatroomManagerDelegate>

@end

@implementation NTESInfoManager

+ (instancetype)sharedManager
{
    static NTESInfoManager *instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[NTESInfoManager alloc] init];
    });
    return instance;
}

- (instancetype)init
{
    self = [super init];
    if (self)
    {
        [self addListener];
    }
    return self;
}

- (void)dealloc
{
    [self removeListener];
}



- (void)addListener
{
    [[NIMSDK sharedSDK].chatManager addDelegate:self];
    [[NIMSDK sharedSDK].chatroomManager addDelegate:self];
    [[NIMSDK sharedSDK].systemNotificationManager addDelegate:self];
}

- (void)removeListener
{
    [[NIMSDK sharedSDK].chatManager removeDelegate:self];
    [[NIMSDK sharedSDK].chatroomManager removeDelegate:self];
    [[NIMSDK sharedSDK].systemNotificationManager removeDelegate:self];
}


#pragma mark - NIMChatManagerDelegate

- (void)onRecvMessages:(NSArray<NIMMessage *> *)messages
{
    for (NIMMessage *message in messages)
    {
        if (message.messageType == NIMMessageTypeNotification)
        {
            [self dealWithChatroomNotification:(NIMNotificationObject *)message.messageObject];
        }
    }
}

- (void)dealWithChatroomNotification:(NIMNotificationObject *)object
{
    if (object.notificationType == NIMNotificationTypeChatroom)
    {
        NIMChatroomNotificationContent *content = (NIMChatroomNotificationContent *)object.content;
        if (content.eventType == NIMChatroomEventTypeQueueChange)
        {
           
        }
    }
}



#pragma mark - NIMChatroomManagerDelegate

- (void)chatroom:(NSString *)roomId connectionStateChanged:(NIMChatroomConnectionState)state
{
    DDLogInfo(@"room id %@ connect changed to state %zd ",roomId,state);
    if (state == NIMChatroomConnectionStateEnterOK)
    {
        // do something interesting
    }
}


#pragma mark - NIMSystemNotificationManagerDelegate
- (void)onReceiveCustomSystemNotification:(NIMCustomSystemNotification *)notification
{
    NSString *content = notification.content;
    NSDictionary * info = [NSDictionary dictByJsonString:content];
    if (info)
    {

    }
}


@end
