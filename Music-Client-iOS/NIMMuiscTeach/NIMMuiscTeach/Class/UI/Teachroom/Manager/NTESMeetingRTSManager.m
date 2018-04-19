//
//  NTESMeetingRTSManager.m
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/8.
//  Copyright © 2018 netease. All rights reserved.
//

#import "NTESMeetingRTSManager.h"



@interface NTESMeetingRTSManager() <NIMRTSConferenceManagerDelegate>

@property(nonatomic, strong) NIMRTSConference *currentConference;

@end

@implementation NTESMeetingRTSManager

+ (instancetype)sharedManager {
    static NTESMeetingRTSManager *instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[NTESMeetingRTSManager alloc] init];
    });
    return instance;
}

- (instancetype)init {
    if (self = [super init]) {
        [[NIMAVChatSDK sharedSDK].rtsConferenceManager addDelegate:self];
    }
    return self;
}

- (void)dealloc {
    [self leaveCurrentConference];
    [[NIMAVChatSDK sharedSDK].rtsConferenceManager removeDelegate:self];
}

- (NSError *)reserveConference:(NSString *)name {
    NIMRTSConference *conference = [NIMRTSConference new];
    conference.name = name;
    return [[NIMAVChatSDK sharedSDK].rtsConferenceManager reserveConference:conference];
}

- (NSError *)joinConference:(NSString *)name {
    [self leaveCurrentConference];
    
    NIMRTSConference *conference = [NIMRTSConference new];
    conference.name = name;
    conference.serverRecording = NO;
    __weak typeof(self) wself = self;
    conference.dataHandler = ^(NIMRTSConferenceData *data) {
        [wself handleReceivedData:data];
    };
    NSError *result = [[NIMAVChatSDK sharedSDK].rtsConferenceManager joinConference:conference];
    return result;
}

- (void)leaveCurrentConference
{
    if (_currentConference) {
        NSError *result = [[[NIMAVChatSDK sharedSDK] rtsConferenceManager] leaveConference:_currentConference];
        DDLogInfo(@"leave current conference %@ result %@", _currentConference.name, result);
        _currentConference = nil;
    }
}

- (BOOL)sendRTSData:(NSData *)data toUser:(NSString *)uid
{
    BOOL accepted = NO;
    
    if (_currentConference) {
        NIMRTSConferenceData *conferenceData = [[NIMRTSConferenceData alloc] init];
        conferenceData.conference = _currentConference;
        conferenceData.data = data;
        conferenceData.uid = uid;
        accepted = [[[NIMAVChatSDK sharedSDK] rtsConferenceManager] sendRTSData:conferenceData];
    }
    
    return accepted;
}

- (BOOL)isJoined
{
    return _currentConference != nil;
}


- (void)handleReceivedData:(NIMRTSConferenceData *)data
{
    if (_dataHandler) {
        [_dataHandler handleReceivedData:data.data sender:data.uid];
    }
}


#pragma mark - NIMRTSConferenceManagerDelegate

- (void)onReserveConference:(NIMRTSConference *)conference
                     result:(NSError *)result
{
    DDLogInfo(@"Reserve conference %@ result:%@", conference.name, result);
    
    //本demo使用聊天室id作为了多人实时会话的名称，保证了其唯一性，如果分配时发现已经存在了，认为是该聊天室的主播之前分配的，可以直接使用
    if (result.code == NIMRemoteErrorCodeExist) {
        result = nil;
    }
    
    if (_delegate) {
        [_delegate onReserve:conference.name result:result];
    }
    
}

- (void)onJoinConference:(NIMRTSConference *)conference
                  result:(NSError *)result
{
    DDLogInfo(@"Join conference %@ result:%@", conference.name, result);
    
    if (nil == result || nil == _currentConference) {
        _currentConference = conference;
    }
    
    if (_delegate) {
        [_delegate onJoin:conference.name result:result];
    }
    
}

- (void)onLeftConference:(NIMRTSConference *)conference
                   error:(NSError *)error
{
    DDLogInfo(@"Left conference %@ error:%@", conference.name, error);
    if ([_currentConference.name isEqualToString:conference.name]) {
        _currentConference = nil;
        
        if (_delegate) {
            [_delegate onLeft:conference.name result:error];
        }
    }
}

- (void)onUserJoined:(NSString *)uid
          conference:(NIMRTSConference *)conference
{
    DDLogInfo(@"User %@ joined conference %@", uid, conference.name);
    if ([_currentConference.name isEqualToString:conference.name]) {
        
        if (_delegate) {
            [_delegate onUserJoined:uid conference:conference.name];
        }
    }
    
}

- (void)onUserLeft:(NSString *)uid
        conference:(NIMRTSConference *)conference
            reason:(NIMRTSConferenceUserLeaveReason)reason
{
    DDLogInfo(@"User %@ left conference %@ for %zd", uid, conference.name, reason);
    
    if ([_currentConference.name isEqualToString:conference.name]) {
        if (_delegate) {
            [_delegate onUserLeft:uid conference:conference.name];
        }
    }
}

@end
