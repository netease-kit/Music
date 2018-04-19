//
//  NTESQueryRoomInfoTask.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/3/30.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESNetworkTask.h"
#import "NTESEnum.h"

@interface NTESQueryRoomInfoTask : NSObject<NTESNetworkTask>

- (instancetype)initWithRole:(NTESUserRole)role;

@end
