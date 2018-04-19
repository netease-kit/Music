//
//  NTESReserveRoomTask.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESReserveRoomTask.h"

@implementation NTESReserveRoomTask

- (NSString *)requestMethod
{
    return @"room/create";
}

- (NSDictionary *)param
{
    return @{
             @"sid"    : [NIMSDK sharedSDK].loginManager.currentAccount?: @"",
            };
}

@end
