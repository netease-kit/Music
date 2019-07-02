//
//  NTESSolutionConfig.m
//  NIMSolutionTemplate
//
//  Created by chris on 2018/1/10.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESSolutionConfig.h"

@implementation NTESSolutionConfig

+ (instancetype)config
{
    static NTESSolutionConfig *config;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        config = [[NTESSolutionConfig alloc] init];
    });
    return config;
}

- (instancetype)init
{
    self = [super init];
    if(self)
    {
        NSAssert(NO, @"请去官网获取APP appkey");
        _appKey  = @"";
        _appHost = @"https://app.netease.im/appdemo/music/";
    }
    return self;
}

@end
