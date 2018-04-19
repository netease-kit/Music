//
//  NTESSolutionConfig.h
//  NIMSolutionTemplate
//
//  Created by chris on 2018/1/10.
//  Copyright © 2018年 chris. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NTESSolutionConfig : NSObject

@property (nonatomic,copy) NSString *appKey;

@property (nonatomic,copy) NSString *appHost;

+ (instancetype)config;

@end
