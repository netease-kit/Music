//
//  NTESAppDelegate.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/3/23.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESAppDelegate.h"
#import <NIMSDK/NIMSDK.h>
#import "NTESLoginViewController.h"
#import "NTESSolutionConfig.h"
#import "NTESLoginService.h"
#import "NTESHallwayViewController.h"

@interface NTESAppDelegate () <NIMLoginManagerDelegate>

@end

@implementation NTESAppDelegate

- (void)applicationDidFinishLaunching:(UIApplication *)application
{
    [self setupNIMSDK];
    [GLobalRealReachability startNotifier];
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
    NTESLoginData *data = [NTESLoginData read];
    
    if (data)
    {
        [self autoLogin:data];
    }
    else
    {
        NTESLoginViewController *vc = [[NTESLoginViewController alloc] initWithNibName:nil bundle:nil];
        UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:vc];
        self.window.rootViewController = nav;
    }
    [self.window makeKeyAndVisible];
}

- (void)dealloc {
    [[NIMSDK sharedSDK].loginManager removeDelegate:self];
}

- (void)setupNIMSDK
{
    [[NIMSDK sharedSDK].loginManager addDelegate:self];
    [[NIMSDK sharedSDK] registerWithAppID:[NTESSolutionConfig config].appKey cerName:nil];
}

- (void)autoLogin:(NTESLoginData *)data
{
    [[NIMSDK sharedSDK].loginManager autoLogin:data.toAutoLoginData];
    NTESHallwayViewController *vc = [[NTESHallwayViewController alloc] initWithRole:data.role];
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:vc];
    self.window.rootViewController = nav;
}

- (void)onKick:(NIMKickReason)code clientType:(NIMLoginClientType)clientType {
    NSString *reason = @"你被踢下线";
    switch (code) {
        case NIMKickReasonByClient:
        case NIMKickReasonByClientManually:{
            reason = @"你的帐号被踢出下线，请注意帐号信息安全";
            break;
        }
        case NIMKickReasonByServer:
            reason = @"你被服务器踢下线";
            break;
        default:
            break;
    }
    [[[NIMSDK sharedSDK] loginManager] logout:^(NSError *error) {
        [NTESLoginData clear];
        [[NSNotificationCenter defaultCenter] postNotificationName:NTESNotificationLogout object:nil];
    }];
}

@end
