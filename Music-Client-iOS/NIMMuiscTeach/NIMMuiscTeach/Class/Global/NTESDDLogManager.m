//
//  NTESDDLogManager.m
//  NIMSolutionTemplate
//
//  Created by chris on 2018/1/9.
//  Copyright © 2018年 chris. All rights reserved.
//

#import "NTESDDLogManager.h"

@implementation NTESDDLogManager

static DDFileLogger *fileLogger;

+ (void)load
{
    [DDLog addLogger:[DDTTYLogger sharedInstance]];
    [[DDTTYLogger sharedInstance] setColorsEnabled:YES];
    [[DDTTYLogger sharedInstance] setForegroundColor:[UIColor greenColor] backgroundColor:nil forFlag:DDLogFlagDebug];
    
    fileLogger = [[DDFileLogger alloc] init];
    fileLogger.rollingFrequency = 60 * 60 * 24; // 24 hour rolling
    fileLogger.logFileManager.maximumNumberOfLogFiles = 7;
    [DDLog addLogger:fileLogger];
    
    DDLogInfo(@"setup file loger, current log path is %@",fileLogger.logFileManager.logsDirectory);
}

+ (DDFileLogger *)fileLogger
{
    return fileLogger;
}

@end
