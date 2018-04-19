//
//  NTESTeacherService.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import <Foundation/Foundation.h>
@class NTESClassroom;

@interface NTESTeacherService : NSObject

/*  老师查询是否有学生预定了自己的课程 */
- (void)queryClassroomInfo:(void(^)(NSError *error, NTESClassroom *classroom))completion;

/*  老师下课 */
- (void)closeClass:(NTESClassroom *)classroom
        completion:(void(^)(NSError *error))completion;

@end
