//
//  NTESTeacher.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESTeacher.h"
#import "NSDictionary+NTESJson.h"

@interface NTESTeacher()

@property (nonatomic, copy) NSString *token;

@end

@implementation NTESTeacher

- (instancetype)initWithDictionary:(NSDictionary *)dictionary
{
    self = [super init];
    if (self)
    {
        _userId = [dictionary jsonString:@"teacherAccid"];
        _nick   = [dictionary jsonString:@"teacherName"];
        _token  = [dictionary jsonString:@"teacherPassword"];
    }
    return self;
}

@end
