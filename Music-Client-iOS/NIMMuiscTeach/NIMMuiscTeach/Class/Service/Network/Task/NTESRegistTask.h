//
//  NTESRegistTask.h
//  NIM
//
//  Created by chris on 2017/11/20.
//  Copyright © 2017年 Netease. All rights reserved.
//

#import "NTESNetworkTask.h"

@interface NTESRegistTask : NSObject<NTESNetworkTask>

@property (nonatomic, copy) NSString *accid;

@property (nonatomic, copy) NSString *password;

@property (nonatomic, copy) NSString *nickname;

@end
