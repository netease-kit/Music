//
//  NTESWhiteboardCommand.h
//  NIMEducationDemo
//
//  Created by fenric on 16/10/26.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NTESWhiteboardPoint.h"
#import "NTESDocumentShareInfo.h"


typedef NS_ENUM(NSUInteger, NTESWhiteBoardCmdType){
    NTESWhiteBoardCmdTypePointStart    = 1,
    NTESWhiteBoardCmdTypePointMove     = 2,
    NTESWhiteBoardCmdTypePointEnd      = 3,
    
    NTESWhiteBoardCmdTypeCancelLine    = 4,
    NTESWhiteBoardCmdTypePacketID      = 5,
    NTESWhiteBoardCmdTypeClearLines    = 6,
    NTESWhiteBoardCmdTypeClearLinesAck = 7,
    
    NTESWhiteBoardCmdTypeSyncRequest   = 8,
    NTESWhiteBoardCmdTypeSync          = 9,
    
    NTESWhiteBoardCmdTypeSyncPrepare    = 10,
    NTESWhiteBoardCmdTypeSyncPrepareAck = 11,
    
    NTESWhiteBoardCmdTypeLaserPenMove  = 12,
    NTESWhiteBoardCmdTypeLaserPenEnd   = 13,
    
    NTESWhiteBoardCmdTypeDocShare      = 14,

};


@interface NTESWhiteboardCommand : NSObject

+ (NSString *)pointCommand:(NTESWhiteboardPoint *)point;

+ (NSString *)pureCommand:(NTESWhiteBoardCmdType)type;

+ (NSString *)syncCommand:(NSString *)uid end:(int)end;

+ (NSString *)packetIdCommand:(UInt64)packetId;

+ (NSString *)docShareCommand:(NTESDocumentShareInfo *)shareInfo;

@end
