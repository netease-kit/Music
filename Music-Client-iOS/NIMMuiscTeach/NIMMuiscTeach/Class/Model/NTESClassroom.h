//
//  NTESClassroom.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import <Foundation/Foundation.h>
@class NTESTeacher;
@class NTESStudent;

@interface NTESClassroom : NSObject

@property (nonatomic, copy)   NSString *roomId;

@property (nonatomic, strong) NTESTeacher *teacher;

@property (nonatomic, strong) NTESStudent *student;

@property (nonatomic, strong) NIMNetCallMeeting *meeting;


- (instancetype)initWithDictionary:(NSDictionary *)dictionary;

@end
