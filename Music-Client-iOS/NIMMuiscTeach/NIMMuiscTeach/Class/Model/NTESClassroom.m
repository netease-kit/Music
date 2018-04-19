//
//  NTESClassroom.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESClassroom.h"
#import "NTESTeacher.h"
#import "NTESStudent.h"
#import "NSDictionary+NTESJson.h"

@implementation NTESClassroom

- (instancetype)initWithDictionary:(NSDictionary *)dictionary
{
    self = [super init];
    if (self)
    {
        _roomId = [dictionary jsonString:@"roomId"];
        _teacher = [[NTESTeacher alloc] initWithDictionary:dictionary];
        _student = [[NTESStudent alloc] initWithDictionary:dictionary];
    }
    return self;
}

@end
