//
//  NTESVideoView.h
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/9.
//  Copyright © 2018 netease. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface NTESVideoView : UIView

- (void)renderWhenYUVReady:(NSData *)yuvData
                     width:(NSUInteger)width
                    height:(NSUInteger)height
                      from:(NSString *)user;

@end
