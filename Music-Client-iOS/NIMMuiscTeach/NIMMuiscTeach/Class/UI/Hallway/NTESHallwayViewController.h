//
//  NTESHallwayViewController.h
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "NTESEnum.h"

@interface NTESHallwayViewController : UIViewController

- (instancetype)initWithRole:(NTESUserRole)role;

@property (nonatomic, strong) UIButton *actionButton;

@end
