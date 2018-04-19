//
//  NTESTeacherService.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESTeacherService.h"
#import "NTESQueryRoomInfoTask.h"
#import "NTESNetwork.h"
#import "NSDictionary+NTESJson.h"
#import "NTESClassroom.h"
#import "NTESStudent.h"
#import "NTESCloseRoomTask.h"

@implementation NTESTeacherService

- (void)queryClassroomInfo:(void(^)(NSError *error, NTESClassroom *classroom))completion
{
    DDLogInfo(@"teacher query classroom info");
    NTESQueryRoomInfoTask *task = [[NTESQueryRoomInfoTask alloc] initWithRole:NTESUserRoleTeacher];
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, id jsonObject) {
        DDLogInfo(@"teacher end query classroom info %@",jsonObject);
        if (completion)
        {
            if (!error)
            {
                NSDictionary *info = [jsonObject jsonArray:@"list"].firstObject;
                NTESClassroom *classroom = [[NTESClassroom alloc] initWithDictionary:info];
                completion(error, classroom);
            }
            else
            {
                completion(error,nil);
            }
        }
    }];

}

- (void)closeClass:(NTESClassroom *)classroom
        completion:(void(^)(NSError *error))completion
{
    DDLogInfo(@"teacher start closing classroom ...");
    NTESCloseRoomTask *task = [[NTESCloseRoomTask alloc] init];
    task.roomId = classroom.roomId;
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, id jsonObject) {
        DDLogInfo(@"teacher end closing classroom ... %@",jsonObject);
        if (completion)
        {
            completion(error);
        }
    }];
    
    DDLogInfo(@"teacher start notify student ...");
    NSDictionary *dict = @{@"command":@(NTESCustomNotifcationCommandClassOver),@"data":@{@"roomId":classroom.roomId}};
    NSString *json = [dict toJson];
    NIMCustomSystemNotification *notification = [[NIMCustomSystemNotification alloc] initWithContent:json];
    notification.sendToOnlineUsersOnly = NO;
    
    NIMSession *session = [NIMSession session:classroom.student.userId type:NIMSessionTypeP2P];
    [[NIMSDK sharedSDK].systemNotificationManager sendCustomNotification:notification toSession:session completion:nil];
}

@end
