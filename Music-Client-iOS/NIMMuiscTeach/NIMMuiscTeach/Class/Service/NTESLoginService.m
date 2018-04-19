//
//  NTESLoginService.m
//  NIMDragonClaw
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESLoginService.h"
#import "NTESInfoManager.h"
#import "NTESRegistTask.h"
#import "NTESAccountCheckTask.h"
#import "NTESNetwork.h"
#import "NSDictionary+NTESJson.h"
#import "NTESDDLogManager.h"
#import <NIMSDK/NIMSDK.h>

@interface NTESInfoManager(User)

@end

@implementation NTESLoginService

- (void)registerUser:(NTESRegisterData *)data
          completion:(void(^)(NSError *error))completion
{
    NTESRegistTask *task = [[NTESRegistTask alloc] init];
    task.accid = data.userId;
    task.nickname = data.nickname;
    task.password = data.token;
    DDLogInfo(@"start regist");
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, id jsonObject) {
        DDLogInfo(@"regist complete %@ , error %@",jsonObject,error);
        if (completion)
        {
            completion(error);
        }
    }];
}

- (void)login:(NTESLoginData *)data
   completion:(void(^)(NSError *error, NTESUserRole role))completion
{
    DDLogInfo(@"start login ... accid %@",data.userId);
    [[NIMSDK sharedSDK].loginManager login:data.userId token:data.token completion:^(NSError * _Nullable error) {
        DDLogInfo(@"end login ... %@",error);
        if (!error)
        {
            [self checkRole:^(NSError *error, NTESUserRole role) {
                data.role = role;
                [data synchronize];
                if (completion)
                {
                    completion(error,role);
                }
            }];
            
        }
        else if (completion)
        {
            completion(error,NTESUserRoleStudent);
        }
    }];
}

- (void)checkRole:(void(^)(NSError *error, NTESUserRole role))completion
{
    DDLogInfo(@"start check role");
    NTESAccountCheckTask *task = [[NTESAccountCheckTask alloc] init];
    [[NTESNetwork sharedNetwork] postNetworkTask:task completion:^(NSError *error, NSDictionary *jsonObject) {
        DDLogInfo(@"end check role %@",error);
        if (completion)
        {
            if (!error)
            {
                NTESUserRole role = [jsonObject jsonInteger:@"userType"];
                completion(nil,role);
            }
            else
            {
                completion(error,NTESUserRoleUndefine);
            }
        }
        
    }];
}


@end


@implementation NTESRegisterData
@end

#define NIMRole         @"role"
#define NIMUserId       @"userId"
#define NIMToken        @"token"

@implementation NTESLoginData

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    if (self = [super init]) {
        _userId = [aDecoder decodeObjectForKey:NIMUserId];
        _token  = [aDecoder decodeObjectForKey:NIMToken];
        _role   = [[aDecoder decodeObjectForKey:NIMRole] integerValue];
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)encoder
{
    if ([_userId length]) {
        [encoder encodeObject:_userId forKey:NIMUserId];
    }
    if ([_token length]) {
        [encoder encodeObject:_token forKey:NIMToken];
    }
    [encoder encodeObject:@(_role) forKey:NIMRole];
}

- (void)synchronize
{
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:self];
    [data writeToFile:[NTESLoginData path] atomically:YES];
}

- (NIMAutoLoginData *)toAutoLoginData
{
    NIMAutoLoginData *data = [[NIMAutoLoginData alloc] init];
    data.account = self.userId;
    data.token   = self.token;
    return data;
}

+ (void)clear {
    NSString *path = [NTESLoginData path];
    [[NSFileManager defaultManager] removeItemAtPath:path error:nil];
}

+ (NTESLoginData *)read
{
    NSString *path = [NTESLoginData path];
    if ([[NSFileManager defaultManager] fileExistsAtPath:path])
    {
        id object = [NSKeyedUnarchiver unarchiveObjectWithFile:path];
        NTESLoginData *data = [object isKindOfClass:[NTESLoginData class]] ? object : nil;
        return data;
    }
    return nil;
}

+ (NSString *)path
{
    static NSString *syncPath = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSString *appkey = [NIMSDK sharedSDK].appKey;
        NSArray  *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        syncPath = [[NSString alloc]initWithFormat:@"%@/%@/",paths.firstObject,appkey];
        
        if (![[NSFileManager defaultManager] fileExistsAtPath:syncPath])
        {
            [[NSFileManager defaultManager] createDirectoryAtPath:syncPath
                                      withIntermediateDirectories:NO
                                                       attributes:nil
                                                            error:nil];
        }
        
        syncPath = [syncPath stringByAppendingPathComponent:@"nim_sdk_ntes_login_data"];
    });
    return syncPath;
}


@end
