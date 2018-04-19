//
//  NTESRegistTask.m
//  NIM
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESRegistTask.h"

@implementation NTESRegistTask

- (NSString *)requestMethod
{
    return @"user/reg";
}

- (NSDictionary *)param
{
    return @{
             @"accid"    : self.accid?: @"",
             @"nickname" : self.nickname?: @"",
             @"password" : self.password?: @"",
            };
}

@end
