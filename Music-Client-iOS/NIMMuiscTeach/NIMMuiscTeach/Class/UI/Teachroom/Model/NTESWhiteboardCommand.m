//
//  NTESWhiteboardCommand.m
//  NIMEducationDemo
//
//  Created by fenric on 16/10/26.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import "NTESWhiteboardCommand.h"

#define NTESWhiteboardCmdFormatPoint @"%zd:%.4f,%.4f,%d,%d;"
#define NTESWhiteboardCmdFormatPacketID @"%zd:%llu;"
#define NTESWhiteboardCmdFormatSync @"%zd:%@,%d;"
#define NTESWhiteboardCmdFormatPureCmd @"%zd:;"
#define NTESWhiteboardCmdFormatDocShareCmd @"%zd:%@,%d,%d,%zd;"


@implementation NTESWhiteboardCommand

+ (NSString *)pointCommand:(NTESWhiteboardPoint *)point
{
    return [NSString stringWithFormat:NTESWhiteboardCmdFormatPoint, point.type, point.xScale, point.yScale, point.colorRGB, point.pageIndex];
}

+ (NSString *)pureCommand:(NTESWhiteBoardCmdType)type
{
    return [NSString stringWithFormat:NTESWhiteboardCmdFormatPureCmd, type];
}

+ (NSString *)syncCommand:(NSString *)uid end:(int)end
{
    return [NSString stringWithFormat:NTESWhiteboardCmdFormatSync, NTESWhiteBoardCmdTypeSync, uid, end];
}

+ (NSString *)packetIdCommand:(UInt64)packetId
{
    return [NSString stringWithFormat:NTESWhiteboardCmdFormatPacketID, NTESWhiteBoardCmdTypePacketID, packetId];
}

+ (NSString *)docShareCommand:(NTESDocumentShareInfo *)shareInfo
{
    return [NSString stringWithFormat:NTESWhiteboardCmdFormatDocShareCmd, NTESWhiteBoardCmdTypeDocShare, shareInfo.docId, shareInfo.currentPage, shareInfo.pageCount,shareInfo.type];
}




@end
