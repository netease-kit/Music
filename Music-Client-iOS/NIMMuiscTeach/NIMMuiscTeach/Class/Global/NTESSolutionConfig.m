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
        _appKey  = @"c2b388726a789f58857501e9bafec3f5";
        _appHost = @"https://app.netease.im/appdemo/music/";
    }
    return self;
}

@end
