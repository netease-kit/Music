//
//  NTESMeetingRTSManager.h
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/8.
//  Copyright © 2018 netease. All rights reserved.
//

#import <Foundation/Foundation.h>


@protocol NTESMeetingRTSManagerDelegate <NSObject>

- (void)onReserve:(NSString *)name result:(NSError *)result;

- (void)onJoin:(NSString *)name result:(NSError *)result;

- (void)onLeft:(NSString *)name result:(NSError *)error;

- (void)onUserJoined:(NSString *)uid conference:(NSString *)name;

- (void)onUserLeft:(NSString *)uid conference:(NSString *)name;

@end

@protocol NTESMeetingRTSDataHandler <NSObject>

- (void)handleReceivedData:(NSData *)data sender:(NSString *)sender;

@end

@interface NTESMeetingRTSManager : NSObject

@property(nonatomic, weak) id<NTESMeetingRTSManagerDelegate> delegate;

@property(nonatomic, weak) id<NTESMeetingRTSDataHandler> dataHandler;

+ (instancetype)sharedManager;

- (NSError *)reserveConference:(NSString *)name;

- (NSError *)joinConference:(NSString *)name;

- (void)leaveCurrentConference;

- (BOOL)sendRTSData:(NSData *)data toUser:(NSString *)uid;

- (BOOL)isJoined;

@end
