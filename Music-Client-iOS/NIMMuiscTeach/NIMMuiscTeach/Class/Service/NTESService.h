//
//  NTESService.h
//  NIMDragonClaw
//
//  Created by chris on 2017/11/22.
//  Copyright © 2017年 Netease. All rights reserved.
//


#import "NTESLoginService.h"
#import "NTESTeacherService.h"
#import "NTESStudentService.h"

@interface NTESService : NSObject

+ (instancetype)sharedService;

@property (nonatomic,strong) NTESLoginService *loginService;

@property (nonatomic,strong) NTESTeacherService *teahcerService;

@property (nonatomic,strong) NTESStudentService *studentService;

@end
