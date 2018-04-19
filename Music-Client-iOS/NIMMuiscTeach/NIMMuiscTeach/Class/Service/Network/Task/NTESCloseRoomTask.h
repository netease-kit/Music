//
//  NTESCloseRoomTask.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/8.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESNetworkTask.h"

@interface NTESCloseRoomTask : NSObject<NTESNetworkTask>

@property (nonatomic, copy) NSString *roomId;

@end
