//
//  NTESAccountCheckTask.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/4.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESAccountCheckTask.h"

@implementation NTESAccountCheckTask

- (NSString *)requestMethod
{
    return @"user/check";
}

- (NSDictionary *)param
{
    return @{
               @"accid"    : [NIMSDK sharedSDK].loginManager.currentAccount?: @"",
            };
}


@end
