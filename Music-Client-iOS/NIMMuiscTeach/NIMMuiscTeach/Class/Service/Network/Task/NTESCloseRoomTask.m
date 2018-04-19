//
//  NTESCloseRoomTask.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/8.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESCloseRoomTask.h"

@implementation NTESCloseRoomTask

- (NSString *)requestMethod
{
    return @"teacher/room/close";
}

- (NSDictionary *)param
{
    return @{
                @"sid"    : [NIMSDK sharedSDK].loginManager.currentAccount?: @"",
                @"roomId" : self.roomId?: @"",
             };
}


@end
