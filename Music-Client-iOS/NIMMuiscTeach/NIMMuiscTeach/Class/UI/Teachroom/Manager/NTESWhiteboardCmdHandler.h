//
//  NTESWhiteboardCmdHandler.h
//  NIMEducationDemo
//
//  Created by fenric on 16/10/31.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NTESWhiteboardPoint.h"
#import "NTESWhiteboardCommand.h"
#import "NTESMeetingRTSManager.h"

@protocol NTESWhiteboardCmdHandlerDelegate <NSObject>

@optional

- (void)onReceivePoint:(NTESWhiteboardPoint *)point from:(NSString *)sender;

- (void)onReceiveCmd:(NTESWhiteBoardCmdType)type from:(NSString *)sender;

- (void)onReceiveSyncRequestFrom:(NSString *)sender;

- (void)onReceiveSyncPoints:(NSMutableDictionary *)points owner:(NSString *)owner;

- (void)onReceiveLaserPoint:(NTESWhiteboardPoint *)point from:(NSString *)sender;

- (void)onReceiveHiddenLaserfrom:(NSString *)sender;

- (void)onReceiveDocShareInfo:(NTESDocumentShareInfo *)shareInfo from:(NSString *)sender;



@end

@interface NTESWhiteboardCmdHandler : NSObject<NTESMeetingRTSDataHandler>

- (instancetype)initWithDelegate:(id<NTESWhiteboardCmdHandlerDelegate>)delegate;

- (void)sendMyPoint:(NTESWhiteboardPoint *)point;

- (void)sendPureCmd:(NTESWhiteBoardCmdType)type to:(NSString *)uid;

- (void)sync:(NSDictionary *)allLines toUser:(NSString *)targetUid;

- (void)sendDocShareInfo:(NTESDocumentShareInfo *)shareInfo toUser:(NSString*)targetUid;

@end
