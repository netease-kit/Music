//
//  NTESStudentService.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import <Foundation/Foundation.h>
@class NTESClassroom;

@interface NTESStudentService : NSObject

/* 学生查询是否有预定课程，返回课程和对应的老师账号信息 */
- (void)queryClassroomInfo:(void(^)(NSError *error, NTESClassroom *classroom))completion;

/* 学生预定课程，返回课程和对应的老师账号信息 */
- (void)reserveClassroom:(void(^)(NSError *error, NTESClassroom *classroom))completion;


@end
