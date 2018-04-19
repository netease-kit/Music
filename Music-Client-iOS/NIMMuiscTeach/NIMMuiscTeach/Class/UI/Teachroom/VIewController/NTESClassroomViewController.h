//
//  NTESTeachroomViewController.h
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/1.
//  Copyright © 2018 netease. All rights reserved.
//

#import <UIKit/UIKit.h>

@class NTESClassroom;
@interface NTESClassroomViewController : UIViewController

- (instancetype)initWithClassroom:(NTESClassroom *)classroom andRole:(NTESUserRole)role;

@end
