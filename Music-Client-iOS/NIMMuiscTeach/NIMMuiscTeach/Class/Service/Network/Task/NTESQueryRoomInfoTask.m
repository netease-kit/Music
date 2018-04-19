//
//  NTESQueryRoomInfoTask.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/3/30.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESQueryRoomInfoTask.h"

@interface NTESQueryRoomInfoTask()

@property (nonatomic, assign) NTESUserRole role;

@end

@implementation NTESQueryRoomInfoTask

- (instancetype)initWithRole:(NTESUserRole)role
{
    self = [super init];
    if (self)
    {
        _role = role;
    }
    return self;
}

- (NSString *)requestMethod
{
    if (self.role == NTESUserRoleStudent)
    {
        return @"room/query";
    }
    else
    {
        return @"teacher/room/query";
    }
}

- (NSDictionary *)param
{
    return @{
                @"sid" : [NIMSDK sharedSDK].loginManager.currentAccount?: @""
            };
}


@end
