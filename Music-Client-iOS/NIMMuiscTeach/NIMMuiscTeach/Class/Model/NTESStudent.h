//
//  NTESStudent.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/4.
//  Copyright © 2018年 netease. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NTESStudent : NSObject

@property (nonatomic, copy) NSString *userId;

@property (nonatomic, copy) NSString *nick;

- (instancetype)initWithDictionary:(NSDictionary *)dictionary;

@end
