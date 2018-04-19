//
//  NTESInfoManager.h
//  NIMDragonClaw
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol NTESInfoManagerDelegate<NSObject>

@optional

@end

@interface NTESInfoManager : NSObject

+ (instancetype)sharedManager;

@property (nonatomic,weak) id<NTESInfoManagerDelegate> delegate;

@end
