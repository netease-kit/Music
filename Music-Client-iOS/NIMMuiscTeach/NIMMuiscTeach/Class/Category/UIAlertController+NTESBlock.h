//
//  UIAlertController+NTESBlock.h
//
//  Created by chris on 18-1-9.
//  Copyright (c) 2018年 Netease. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIAlertController (NTESBlock)

- (nonnull UIAlertController *)addAction:(nullable NSString *)title
                           style:(UIAlertActionStyle)style
                         handler:(void (^ __nullable)(UIAlertAction *action))handler;

- (void)show;

NS_ASSUME_NONNULL_END

@end
