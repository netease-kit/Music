//
//  NTESLoginService.h
//  NIMDragonClaw
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NTESEnum.h"

@interface NTESRegisterData : NSObject

@property (nonatomic,copy) NSString *userId;

@property (nonatomic,copy) NSString *token;

@property (nonatomic,copy) NSString *nickname;

@end

@interface NTESLoginData : NSObject

@property (nonatomic, assign) NTESUserRole role;

@property (nonatomic, copy) NSString *userId;

@property (nonatomic, copy) NSString *token;

+ (NTESLoginData *)read;

+ (void)clear;

- (void)synchronize;

- (NIMAutoLoginData *)toAutoLoginData;

@end


@interface NTESLoginService : NSObject

- (void)registerUser:(NTESRegisterData *)data
          completion:(void(^)(NSError *error))completion;

- (void)login:(NTESLoginData *)data
   completion:(void(^)(NSError *error, NTESUserRole role))completion;


@end
