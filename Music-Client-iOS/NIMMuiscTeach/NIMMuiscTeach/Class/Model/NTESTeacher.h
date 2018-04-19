//
//  NTESTeacher.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NTESTeacher : NSObject

@property (nonatomic, copy) NSString *userId;

@property (nonatomic, copy) NSString *nick;


- (instancetype)initWithDictionary:(NSDictionary *)dictionary;

/* 解决方案演示使用，正常需要走老师注册流程。这里简单一点直接由应用服务器下发已注册的老师账号 */
- (NSString *)token;

@end
