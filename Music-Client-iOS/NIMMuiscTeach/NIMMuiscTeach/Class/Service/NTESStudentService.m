//
//  NTESStudentService.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESStudentService.h"
#import "NTESClassroom.h"
#import "NTESReserveRoomTask.h"
#import "NTESQueryRoomInfoTask.h"
#import "NTESAccountCheckTask.h"
#import "NTESNetwork.h"
#import "NSDictionary+NTESJson.h"

@implementation NTESStudentService

- (void)queryClassroomInfo:(void(^)(NSError *error, NTESClassroom *classroom))completion
{
    DDLogInfo(@"student query classroom info");
    NTESQueryRoomInfoTask *task = [[NTESQueryRoomInfoTask alloc] initWithRole:NTESUserRoleStudent];
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, NSDictionary *jsonObject) {
        DDLogInfo(@"student end query classroom info error: %@",error);
        if (completion)
        {
            if (!error)
            {
                NSArray *list = [jsonObject jsonArray:@"list"];
                //现在是一对一的关系，只需要取第一个教室
                NTESClassroom *classroom = [[NTESClassroom alloc] initWithDictionary:list.firstObject];
                completion(error,classroom);
            }
            else
            {
                completion(error,nil);
            }
        }        
    }];
    
}

- (void)reserveClassroom:(void(^)(NSError *error, NTESClassroom *classroom))completion
{
    DDLogInfo(@"start reserve classroom");
    NTESReserveRoomTask *task = [[NTESReserveRoomTask alloc] init];
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, id jsonObject) {
        DDLogInfo(@"end reserve classroom");
        if (completion)
        {
            if (!error)
            {
                NTESClassroom *classroom = [[NTESClassroom alloc] initWithDictionary:jsonObject];
                completion(error,classroom);
            }
            else
            {
                completion(error,nil);
            }
        }
    }];
}

@end
