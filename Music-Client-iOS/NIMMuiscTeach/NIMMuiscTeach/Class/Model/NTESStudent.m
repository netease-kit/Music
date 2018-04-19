//
//  NTESStudent.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/4.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESStudent.h"
#import "NSDictionary+NTESJson.h"

@implementation NTESStudent

- (instancetype)initWithDictionary:(NSDictionary *)dictionary
{
    self = [super init];
    if (self)
    {
        _userId = [dictionary jsonString:@"studentAccid"];
        _nick   = [dictionary jsonString:@"studentName"];
    }
    return self;
}

@end
