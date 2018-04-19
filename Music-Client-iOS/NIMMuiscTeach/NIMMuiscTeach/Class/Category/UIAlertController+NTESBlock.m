//
//  UIAlertController+NTESBlock.m
//
//  Created by chris on 18-1-9.
//  Copyright (c) 2018年 Netease. All rights reserved.
//

#import "UIAlertController+NTESBlock.h"

@implementation UIAlertController (NTESBlock)
- (UIAlertController *)addAction:(NSString *)title
                           style:(UIAlertActionStyle)style
                         handler:(void (^ __nullable)(UIAlertAction *action))handler
{
    UIAlertAction *action = [UIAlertAction actionWithTitle:title style:style handler:handler];
    [self addAction:action];
    return self;
}

- (void)show
{
    UIViewController *rootVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    UIViewController *topVC = [self topViewControllerWithRootViewController:rootVC];
    [topVC presentViewController:self animated:YES completion:nil];
}

- (UIViewController *)topViewControllerWithRootViewController:(UIViewController *)rootVC {
    if ([rootVC isKindOfClass:[UITabBarController class]]) {
        UITabBarController *tabBarController = (UITabBarController *)rootVC;
        return [self topViewControllerWithRootViewController:tabBarController.selectedViewController];
    } else if ([rootVC isKindOfClass:[UINavigationController class]]) {
        UINavigationController* navigationController = (UINavigationController*)rootVC;
        return [self topViewControllerWithRootViewController:navigationController.visibleViewController];
    } else if (rootVC.presentedViewController) {
        UIViewController* presentedViewController = rootVC.presentedViewController;
        return [self topViewControllerWithRootViewController:presentedViewController];
    } else {
        return rootVC;
    }
}


@end
