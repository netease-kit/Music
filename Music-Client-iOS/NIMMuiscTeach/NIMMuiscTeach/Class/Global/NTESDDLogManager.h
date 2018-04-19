//
//  NTESDDLogManager.h
//  NIMSolutionTemplate
//
//  Created by chris on 2018/1/9.
//  Copyright © 2018年 chris. All rights reserved.
//

#import <CocoaLumberjack/CocoaLumberjack.h>

#ifdef DEBUG
static DDLogLevel ddLogLevel = DDLogLevelVerbose;
#else
static DDLogLevel ddLogLevel = DDLogLevelInfo;
#endif

@interface NTESDDLogManager : NSObject

@end
