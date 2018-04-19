//
//  NTESWhiteboardCmdHandler.m
//  NIMEducationDemo
//
//  Created by fenric on 16/10/31.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import "NTESWhiteboardCmdHandler.h"
#import "NTESTimerHolder.h"
#import "NTESMeetingRTSManager.h"
#import "NTESWhiteboardCommand.h"


#define NTESSendCmdIntervalSeconds 0.06
#define NTESSendCmdMaxSize 30000


@interface NTESWhiteboardCmdHandler()<NTESTimerHolderDelegate>

@property (nonatomic, strong) NTESTimerHolder *sendCmdsTimer;

@property (nonatomic, strong) NSMutableString *cmdsSendBuffer;

@property (nonatomic, assign) UInt64 refPacketID;

@property (nonatomic, weak) id<NTESWhiteboardCmdHandlerDelegate> delegate;

@property (nonatomic, strong) NSMutableDictionary *syncPoints;
@end

@implementation NTESWhiteboardCmdHandler

- (instancetype)initWithDelegate:(id<NTESWhiteboardCmdHandlerDelegate>)delegate
{
    if (self = [super init]) {
        _delegate = delegate;
        _sendCmdsTimer = [[NTESTimerHolder alloc] init];
        _cmdsSendBuffer = [[NSMutableString alloc] init];
        _syncPoints = [[NSMutableDictionary alloc] init];
        [_sendCmdsTimer startTimer:NTESSendCmdIntervalSeconds delegate:self repeats:YES];
    }
    return self;
}

- (void)sendDocShareInfo:(NTESDocumentShareInfo *)shareInfo toUser:(NSString*)targetUid
{
    NSString *cmd = [NTESWhiteboardCommand docShareCommand:shareInfo];
    [_cmdsSendBuffer appendString:cmd];
    if (targetUid) {
        [[NTESMeetingRTSManager sharedManager] sendRTSData:[cmd dataUsingEncoding:NSUTF8StringEncoding]
                                                     toUser:targetUid];
    }
    else{
        [self doSendCmds];
    }
    
}
- (void)sendMyPoint:(NTESWhiteboardPoint *)point
{
    NSString *cmd = [NTESWhiteboardCommand pointCommand:point];
    
    [_cmdsSendBuffer appendString:cmd];
    
    if (_cmdsSendBuffer.length > NTESSendCmdMaxSize) {
        [self doSendCmds];
    }
}

- (void)sendPureCmd:(NTESWhiteBoardCmdType)type to:(NSString *)uid
{
    NSString *cmd = [NTESWhiteboardCommand pureCommand:type];
    if (uid == nil) {
        [_cmdsSendBuffer appendString:cmd];
        [self doSendCmds];
    }
    else {
        [[NTESMeetingRTSManager sharedManager] sendRTSData:[cmd dataUsingEncoding:NSUTF8StringEncoding]
                                                     toUser:uid];
    }
}


- (void)sync:(NSDictionary *)allLines toUser:(NSString *)targetUid
{
    for (NSString *uid in allLines.allKeys) {
        
        NSMutableString *pointsCmd = [[NSMutableString alloc] init];
        
        NSDictionary *dict = [allLines objectForKey:uid];
        
        for (int index = 1; index <= 5; index++)
        {
            NSArray *lines = [dict objectForKey:@(index)];
            
            for (NSArray *line in lines)
            {
                for (NTESWhiteboardPoint *point in line)
                {
                    [pointsCmd appendString:[NTESWhiteboardCommand pointCommand:point]];
                }
                
                int end = [line isEqual:lines.lastObject] ? 1 : 0;
                
                if (pointsCmd.length > NTESSendCmdMaxSize || end)
                {
                    NSString *syncHeadCmd = [NTESWhiteboardCommand syncCommand:uid end:end];
                    
                    NSString *syncCmds = [syncHeadCmd stringByAppendingString:pointsCmd];
                    
                    [[NTESMeetingRTSManager sharedManager] sendRTSData:[syncCmds dataUsingEncoding:NSUTF8StringEncoding]
                                                                toUser:targetUid];
                    [pointsCmd setString:@""];
                }
            }
        }
    }
}








- (void)onNTESTimerFired:(NTESTimerHolder *)holder
{
    [self doSendCmds];
}

- (void)doSendCmds
{
    if (_cmdsSendBuffer.length) {
        NSString *cmd =  [NTESWhiteboardCommand packetIdCommand:_refPacketID++];
        [_cmdsSendBuffer appendString:cmd];
        
        [[NTESMeetingRTSManager sharedManager] sendRTSData:[_cmdsSendBuffer dataUsingEncoding:NSUTF8StringEncoding] toUser:nil];
        
//        NSLog(@"send data %@", _cmdsSendBuffer);
        
        [_cmdsSendBuffer setString:@""];
    }
}

- (void)handleReceivedData:(NSData *)data sender:(NSString *)sender
{
    NSString *cmdsString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    
    NSArray *cmdsArray = [cmdsString componentsSeparatedByString:@";"];
    
    for (NSString *cmdString in cmdsArray) {

        if (cmdString.length == 0) {
            continue;
        }
        
        NSArray *cmd = [cmdString componentsSeparatedByCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@":,"]];

        NSInteger type = [cmd[0] integerValue];
        switch (type) {
            case NTESWhiteBoardCmdTypePointStart:
            case NTESWhiteBoardCmdTypePointMove:
            case NTESWhiteBoardCmdTypePointEnd:
            {
                if (cmd.count == 5) {
                    NTESWhiteboardPoint *point = [[NTESWhiteboardPoint alloc] init];
                    point.type = type;
                    point.xScale = [cmd[1] floatValue];
                    point.yScale = [cmd[2] floatValue];
                    point.colorRGB = [cmd[3] intValue];
                    point.pageIndex = [cmd[4] intValue];
                    
                    if (_delegate) {
                        [_delegate onReceivePoint:point from:sender];
                    }
                }
                else {
                    DDLogError(@"Invalid point cmd: %@", cmdString);
                }
                break;
            }
            case NTESWhiteBoardCmdTypeCancelLine:
            case NTESWhiteBoardCmdTypeClearLines:
            case NTESWhiteBoardCmdTypeClearLinesAck:
            case NTESWhiteBoardCmdTypeSyncPrepare:
            {
                if (_delegate) {
                    [_delegate onReceiveCmd:type from:sender];
                }
                break;
            }
            case NTESWhiteBoardCmdTypeSyncRequest:
            {
                if (_delegate) {
                    [_delegate onReceiveSyncRequestFrom:sender];
                }
                break;
            }
            case NTESWhiteBoardCmdTypeSync:
            {
                NSString *linesOwner = cmd[1];
                int end = [cmd[2] intValue];
                [self handleSync:cmdsArray linesOwner:linesOwner end:end sender:sender];
                return;
            }
            case NTESWhiteBoardCmdTypeLaserPenMove:
            {
                NTESWhiteboardPoint *point = [[NTESWhiteboardPoint alloc] init];
                point.type = type;
                point.xScale = [cmd[1] floatValue];
                point.yScale = [cmd[2] floatValue];
                point.colorRGB  = [cmd[3] intValue];
                point.pageIndex = [cmd[4] intValue];
                if (_delegate) {
                    [_delegate onReceiveLaserPoint:point from:sender];
                }
                break;
            }
            case NTESWhiteBoardCmdTypeLaserPenEnd:
            {
                if (_delegate) {
                    [_delegate onReceiveHiddenLaserfrom:sender];
                }
                break;
            }
            case NTESWhiteBoardCmdTypeDocShare:
            {
                [self handleReceivedDocShareData:cmd sender:sender];
                break;
            }

            default:
                break;
        }
    }

}

- (void)handleSync:(NSArray *)cmdsArray linesOwner:(NSString *)linesOwner end:(int)end sender:(NSString*)sender
{
    NSMutableArray *points = [[NSMutableArray alloc] init];
    
    for (int i = 0; i < cmdsArray.count; i ++) {
        NSString *cmdString = cmdsArray[i];
        
        if (cmdString.length == 0) {
            continue;
        }

        NSArray *cmd = [cmdString componentsSeparatedByCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@":,"]];
        NSInteger type = [cmd[0] integerValue];
        switch (type) {
            case NTESWhiteBoardCmdTypePointStart:
            case NTESWhiteBoardCmdTypePointMove:
            case NTESWhiteBoardCmdTypePointEnd:
            {
                if (cmd.count == 5) {
                    NTESWhiteboardPoint *point = [[NTESWhiteboardPoint alloc] init];
                    point.type = [cmd[0] integerValue];
                    point.xScale = [cmd[1] floatValue];
                    point.yScale = [cmd[2] floatValue];
                    point.colorRGB = [cmd[3] intValue];
                    point.pageIndex = [cmd[4] intValue];
                    NSLog(@"point sync : index: %d", point.pageIndex);
                    [points addObject:point];
                }
                else {
                    DDLogError(@"Invalid point cmd in sync: %@", cmdString);
                }
                break;
            }
            case NTESWhiteBoardCmdTypePacketID:
                break;
                
            case NTESWhiteBoardCmdTypeDocShare:
            {
                [self handleReceivedDocShareData:cmd sender:sender];
                break;
            }

            default:
                DDLogError(@"Invalid cmd in sync: %@", cmdString);
                break;
        }
    }
    
    NSMutableDictionary *allPoints = [_syncPoints objectForKey:linesOwner];
    if (!allPoints) {
        allPoints = [[NSMutableDictionary alloc] init];
    }
    
    for (NTESWhiteboardPoint *point in points)
    {
        NSMutableArray *array = [allPoints objectForKey:@(point.pageIndex)];
        if (!array)
        {
            array = [[NSMutableArray alloc] init];
            [allPoints setObject:array forKey:@(point.pageIndex)];
        }
        [array addObject:point];
    }

    
    if (end) {
        if (_delegate) {
            [_delegate onReceiveSyncPoints:allPoints owner:linesOwner];
        }
        
        [_syncPoints removeObjectForKey:linesOwner];
    }
    else {
        [_syncPoints setObject:allPoints forKey:linesOwner];
    }
}

-(void)handleReceivedDocShareData:(NSArray *)cmd sender:(NSString *)sender
{
    NTESDocumentShareInfo *shareInfo = [[NTESDocumentShareInfo alloc]init];
    shareInfo.docId = cmd[1];
    shareInfo.currentPage = [cmd[2]intValue];
    shareInfo.pageCount = [cmd[3]intValue];
    shareInfo.type = [cmd[4]intValue];
    if (_delegate) {
        [_delegate onReceiveDocShareInfo:shareInfo from:sender];
    }
}

@end
