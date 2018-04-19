//
//  NTESHallwayViewController.m
//  NIMMuiscTeach
//
//  Created by chris on 2018/4/3.
//  Copyright © 2018年 netease. All rights reserved.
//

#import "NTESHallwayViewController.h"
#import "NTESService.h"
#import "NTESClassroom.h"
#import "NTESTeacher.h"
#import "NTESStudent.h"
#import "NTESLoginViewController.h"
#import "NTESClassroomViewController.h"

@interface NTESHallwayViewController () <NIMSystemNotificationManagerDelegate>
{
    NTESUserRole _role;
}

@property (nonatomic, strong) NTESClassroom *classroom;

@property (nonatomic, strong) IBOutlet UIView  *studentGuideBeforeReserve;

@property (nonatomic, strong) IBOutlet UIView  *studentGuideAfterReserve;

@property (nonatomic, strong) IBOutlet UIView  *teacherGuideBeforeReserve;

@property (nonatomic, strong) IBOutlet UIView  *teacherGuideAfterReserve;

@property (nonatomic, strong) IBOutlet UILabel *reserveInfoLabel;

@property(nonatomic, strong) UIButton *refreshBtn;

@property(nonatomic, strong) UIButton *netstatusBtn;

@property(nonatomic, assign) SEL selector;

@end

@implementation NTESHallwayViewController

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [[NIMSDK sharedSDK].systemNotificationManager removeDelegate:self];
}

- (instancetype)initWithRole:(NTESUserRole)role
{
    self = [super initWithNibName:nil bundle:nil];
    if (self)
    {
        _role = role;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self refresh];
    NSDictionary *attributes=[NSDictionary dictionaryWithObjectsAndKeys:[UIColor blackColor],NSForegroundColorAttributeName, nil];
    [self.navigationController.navigationBar setTitleTextAttributes:attributes];
    UIButton *rightBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    rightBtn.size = CGSizeMake(50, 28);
    rightBtn.layer.cornerRadius = 3;
    rightBtn.clipsToBounds = YES;
    rightBtn.titleLabel.font = font(13);
    [rightBtn setBackgroundImage:[UIImage imageNamed:@"login_enter"] forState:UIControlStateNormal];
    [rightBtn setBackgroundImage:[UIImage imageNamed:@"login_enter_pressed"] forState:UIControlStateHighlighted];
    [rightBtn setTitle:@"注销" forState:UIControlStateNormal];
    [rightBtn addTarget:self action:@selector(logout:) forControlEvents:UIControlEventTouchUpInside];
    if (@available(iOS 11, *)) {
        NSLayoutConstraint *widthConstraint = [rightBtn.widthAnchor constraintEqualToConstant:50];
        NSLayoutConstraint *heightConstraint = [rightBtn.heightAnchor constraintEqualToConstant:28];
        [widthConstraint setActive:YES];
        [heightConstraint setActive:YES];
    }
    UIBarButtonItem *rightItem = [[UIBarButtonItem alloc] initWithCustomView:rightBtn];
    self.navigationItem.rightBarButtonItems = @[rightItem];
    [self.view addSubview:self.refreshBtn];
    [self.view addSubview:self.actionButton];
    [self.view addSubview:self.netstatusBtn];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(kickout:) name:NTESNotificationLogout object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(classOver:) name:NTESNotificationClassover object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(netStatusChanged:) name:kRealReachabilityChangedNotification object:nil];
    [[NIMSDK sharedSDK].systemNotificationManager addDelegate:self];
}

- (void)viewDidLayoutSubviews {
    self.refreshBtn.width = self.view.width / 2;
    self.refreshBtn.height = 45;
    self.refreshBtn.center = self.view.center;
    self.netstatusBtn.width = 110;
    self.netstatusBtn.height = 15;
    self.netstatusBtn.centerX = self.view.centerX;
    self.netstatusBtn.bottom = self.view.height - 30 * UIScreenWidthScale;
    self.actionButton.width = self.view.width * 0.84;
    self.actionButton.height = 45;
    self.actionButton.centerX = self.view.centerX;
    self.actionButton.bottom = self.view.height - 70 * UIScreenWidthScale;
}

#pragma mark - NIMSystenNotficationDelegate
- (void)onReceiveCustomSystemNotification:(NIMCustomSystemNotification *)notification
{
    NSString *content = notification.content;
    NSDictionary *dict = [NSDictionary dictByJsonString:content];
    if (dict)
    {
        NTESCustomNotifcationCommand command = [dict jsonInteger:@"command"];
        if (command ==  NTESCustomNotifcationCommandClassOver)
        {
            NSDictionary *data = [dict jsonDict:@"data"];
            NSString *roomId = [data jsonString:@"roomId"];
            if ([roomId isEqualToString:self.classroom.roomId])
            {
                [self refresh];
            }
        }
    }
}


- (void)refresh
{
    __weak typeof(self) weakSelf = self;
    [SVProgressHUD show];
    if (_role == NTESUserRoleStudent)
    {
        
        self.navigationItem.title = @"音乐教学 学生端";
        [[NTESService sharedService].studentService queryClassroomInfo:^(NSError *error, NTESClassroom *classroom) {
            [SVProgressHUD dismiss];
            weakSelf.classroom = classroom;
            [weakSelf setupAfterRefresh];
            weakSelf.refreshBtn.hidden = YES;
        }];
    }
    else
    {
        self.navigationItem.title = @"音乐教学 老师端";
        [[NTESService sharedService].teahcerService queryClassroomInfo:^(NSError *error, NTESClassroom *classroom) {
            [SVProgressHUD dismiss];
            weakSelf.classroom = classroom;
            [weakSelf setupAfterRefresh];
            weakSelf.refreshBtn.hidden = YES;
        }];
    }
}

- (void)setupAfterRefresh
{
    if (self.classroom)
    {
        NSString *title = @"";
        self.selector = nil;
        if (_role == NTESUserRoleStudent)
        {
            //学生
            if (!self.classroom.roomId.length)
            {
                //没有课程预约
                title = @"预约上课";
                self.selector = @selector(reserveClassroom);
                self.studentGuideBeforeReserve.hidden = NO;
                self.studentGuideAfterReserve.hidden = YES;
                self.actionButton.hidden = NO;
            }
            else
            {
                //有课程预约
                title = @"进入教室";
                self.selector = @selector(enterClassroom);
                self.studentGuideAfterReserve.hidden = NO;
                self.actionButton.hidden = NO;
            }
        }
        else
        {
            //老师
            if (!self.classroom.roomId.length)
            {
                //没有人预约老师的课程
                self.actionButton.hidden = NO;
                self.teacherGuideBeforeReserve.hidden = NO;
                self.teacherGuideAfterReserve.hidden = YES;
                self.actionButton.hidden = YES;
            }
            else
            {
                //有课程预约
                self.reserveInfoLabel.text = [NSString stringWithFormat:@"学生（%@）预约了您的课程\n点击进入教室开始上课",self.classroom.student.nick];
                title = @"进入教室";
                self.selector = @selector(enterClassroom);
                self.teacherGuideAfterReserve.hidden = NO;
                self.actionButton.hidden = NO;
            }
        }
        [self.actionButton setTitle:title forState:UIControlStateNormal];
        [self.actionButton setTitle:title forState:UIControlStateHighlighted];
        [self.actionButton removeTarget:self action:nil forControlEvents:UIControlEventTouchUpInside];
        [self.actionButton addTarget:self action:self.selector forControlEvents:UIControlEventTouchUpInside];
    }
    else
    {
        //刷新数据失败后重试
        self.refreshBtn.hidden = NO;
        self.actionButton.hidden = YES;
    }
    
}


- (void)reserveClassroom
{
    __weak typeof(self)weakSelf = self;
    [SVProgressHUD show];
    [[NTESService sharedService].studentService reserveClassroom:^(NSError *error, NTESClassroom *classroom) {
        [SVProgressHUD dismiss];
        if (error)
        {
            [weakSelf.view makeToast:@"预约失败请重试" duration:2.0 position:CSToastPositionCenter];
        }
        else
        {
            weakSelf.classroom = classroom;
            weakSelf.studentGuideBeforeReserve.hidden = YES;
            weakSelf.studentGuideAfterReserve.hidden = NO;
            NSString *title = @"进入教室";
            [weakSelf.actionButton setTitle:title forState:UIControlStateNormal];
            [weakSelf.actionButton setTitle:title forState:UIControlStateHighlighted];
            [weakSelf.actionButton removeTarget:self action:self.selector forControlEvents:UIControlEventTouchUpInside];
            [weakSelf.actionButton addTarget:weakSelf action:@selector(enterClassroom) forControlEvents:UIControlEventTouchUpInside];
        }
    }];
}

- (void)enterClassroom
{
    __weak typeof(self)weakSelf = self;
    if (_role == NTESUserRoleTeacher)
    {
        //老师的话进入房间需要预定一下
        NIMNetCallMeeting *meeting = [self makeMeeting];
        meeting.name = self.classroom.roomId;
        [[NIMAVChatSDK sharedSDK].netCallManager reserveMeeting:meeting completion:^(NIMNetCallMeeting * _Nonnull meeting, NSError * _Nullable error) {
            if (!error || error.code == NIMRemoteErrorCodeExist)
            {
                //房间已存在也算预定成功，直接进入
                [[NIMAVChatSDK sharedSDK].netCallManager joinMeeting:meeting completion:^(NIMNetCallMeeting * _Nonnull meeting, NSError * _Nullable error) {
                    if (!error)
                    {
                        [weakSelf.view makeToast:@"加入音视频房间成功，在新页面启动白板" duration:2.0 position:CSToastPositionCenter];
                        weakSelf.classroom.meeting = meeting;
                        NTESClassroomViewController *classroomVC = [[NTESClassroomViewController alloc] initWithClassroom:weakSelf.classroom andRole:_role];
                        [weakSelf.navigationController pushViewController:classroomVC animated:YES];
                    }
                    else
                    {
                        [weakSelf showErrorTip:error];
                    }
                    
                }];
            }
            else
            {
                [weakSelf showErrorTip:error];
            }
        }];
    }
    else
    {
        //学生进入的话直接加入，注意 404 判断
        NIMNetCallMeeting *meeting = [self makeMeeting];
        meeting.name = self.classroom.roomId;
        [[NIMAVChatSDK sharedSDK].netCallManager joinMeeting:meeting completion:^(NIMNetCallMeeting * _Nonnull meeting, NSError * _Nullable error) {
            if (!error)
            {
                [weakSelf.view makeToast:@"加入音视频房间成功，在新页面启动白板" duration:2.0 position:CSToastPositionCenter];
                weakSelf.classroom.meeting = meeting;
                NTESClassroomViewController *classroomVC = [[NTESClassroomViewController alloc] initWithClassroom:weakSelf.classroom andRole:_role];
                [weakSelf.navigationController pushViewController:classroomVC animated:YES];
            }
            else if (error.code == NIMRemoteErrorCodeNotExist)
            {
                [weakSelf.view makeToast:@"请等老师先进入教室哦" duration:2.0 position:CSToastPositionCenter];
            }
            else
            {
                [weakSelf showErrorTip:error];
            }
        }];
    }
}

- (IBAction)teacherLoginTip:(id)sender
{
    NTESTeacher *teacher = self.classroom.teacher;
    NSString *message = [NSString stringWithFormat:@"账号：%@\n密码：%@",teacher.userId,teacher.token];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"请在另一台设备上登录老师账号：" message:message preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:@"我知道了" style:UIAlertActionStyleDefault handler:nil];
    [alert show];
}

- (void)showErrorTip:(NSError *)error
{
    NSString *tip = [NSString stringWithFormat:@"进入教室失败 code %zd",error.code];
    [self.view makeToast:tip duration:2.0 position:CSToastPositionCenter];
}

- (void)logout:(id)sender
{
    __weak typeof(self) wself = self;
    
    UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:nil message:@"确认要注销吗？" preferredStyle:UIAlertControllerStyleAlert];
    [[alertVC addAction:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [wself doLogout];
    }]addAction:@"取消" style:UIAlertActionStyleCancel handler:nil];
    
    [alertVC show];
}

- (void)kickout:(NSNotification *)notification {
    __weak typeof(self) wself = self;
    UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:nil message:@"账号已经在其他端被登录" preferredStyle:UIAlertControllerStyleAlert];
    [alertVC addAction:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [wself doLogout];
    }];
    [alertVC show];
}

- (void)classOver:(NSNotification *)notification {
    [self refresh];
}

- (void)netStatusChanged:(NSNotification *)notification {
    ReachabilityStatus currentStatus = [[RealReachability sharedInstance] currentReachabilityStatus];
    if (currentStatus == RealStatusViaWWAN || currentStatus == RealStatusViaWiFi ) {
        [self.netstatusBtn setImage:[UIImage imageNamed:@"room_net_status_good"] forState:UIControlStateNormal];
        [self.netstatusBtn setTitle:@"当前网络状况：极好" forState:UIControlStateNormal];
    }
    if (currentStatus == RealStatusUnknown || currentStatus == RealStatusNotReachable) {
        [self.netstatusBtn setImage:[UIImage imageNamed:@"room_net_status_bad"] forState:UIControlStateNormal];
        [self.netstatusBtn setTitle:@"当前网络状况：极差" forState:UIControlStateNormal];
    }
}

- (void)doLogout {
    [SVProgressHUD show];
    DDLogInfo(@"log out...");
    [[NIMSDK sharedSDK].loginManager logout:^(NSError * _Nullable error) {
        [SVProgressHUD dismiss];
        [NTESLoginData clear];
        NTESLoginViewController *vc = [[NTESLoginViewController alloc] initWithNibName:nil bundle:nil];
        UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:vc];
        [UIApplication sharedApplication].keyWindow.rootViewController = nav;
    }];
}

- (void)onRefreshTap:(UIButton *)sender {
    [self refresh];
}

- (UIButton *)refreshBtn {
    if (!_refreshBtn) {
        _refreshBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            [btn setBackgroundImage:[UIImage imageNamed:@"login_enter"] forState:UIControlStateNormal];
            [btn setBackgroundImage:[UIImage imageNamed:@"login_enter_pressed"] forState:UIControlStateSelected];
            [btn setTitle:@"重试刷新" forState:UIControlStateNormal];
            [btn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
            btn.hidden = YES;
            [btn addTarget:self action:@selector(onRefreshTap:) forControlEvents:UIControlEventTouchUpInside];
            btn;
        });
    }
    return _refreshBtn;
}

- (NIMNetCallMeeting *)makeMeeting
{
    NIMNetCallMeeting *meeting = [[NIMNetCallMeeting alloc] init];
    meeting.type = NIMNetCallMediaTypeVideo;
    meeting.actor = YES;
    NIMNetCallOption *option = [[NIMNetCallOption alloc] init];
    [self fillUserSetting:option];
    option.videoCaptureParam.videoCrop = NIMNetCallVideoCrop16x9;
    meeting.option = option;
    
    return meeting;
}


- (void)fillUserSetting:(NIMNetCallOption *)option
{
    option.autoRotateRemoteVideo = YES;
    option.serverRecordAudio     = NO;
    option.serverRecordVideo     = NO;
    option.acousticEchoCanceler = NIMAVChatAcousticEchoCancelerDefault;
    option.preferredVideoEncoder = NIMNetCallVideoCodecHardware;
    option.videoCaptureParam.videoCaptureOrientation = NIMVideoOrientationPortrait;
    option.audioHowlingSuppress = YES;
    option.preferHDAudio =  YES;
    option.scene = NIMAVChatSceneHighQualityMusic;//高清音乐场景
    
    NIMNetCallVideoCaptureParam *param = [[NIMNetCallVideoCaptureParam alloc] init];
    param.preferredVideoQuality = NIMNetCallVideoQualityDefault;
    param.startWithBackCamera = NO;
    option.videoCaptureParam = param;
}

- (UIButton *)netstatusBtn {
    if (!_netstatusBtn) {
        _netstatusBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            [btn setTitleColor:UIColorFromRGB(0x666666) forState:UIControlStateNormal];
            btn.titleLabel.font = font(10);
            [btn setImage:[UIImage imageNamed:@"room_net_status_good"] forState:UIControlStateNormal];
            [btn setTitle:@"当前网络状况：极好" forState:UIControlStateNormal];
            btn.titleLabel.adjustsFontSizeToFitWidth = YES;
            btn;
        });
    }
    return _netstatusBtn;
}

- (UIButton *)actionButton {
    if (!_actionButton) {
        _actionButton = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            [btn setBackgroundImage:[UIImage imageNamed:@"login_enter"] forState:UIControlStateNormal];
            [btn setBackgroundImage:[UIImage imageNamed:@"login_enter_disable"] forState:UIControlStateDisabled];
            [btn setBackgroundImage:[UIImage imageNamed:@"login_enter_pressed"] forState:UIControlStateSelected];
            [btn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
            btn;
        });
    }
    return _actionButton;
}

@end
