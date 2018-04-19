//
//  NTESClassroomViewController.m
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/1.
//  Copyright © 2018 netease. All rights reserved.
//

#import "NTESClassroomViewController.h"
#import "NTESWhiteboardDrawView.h"
#import "NTESWhiteboardPoint.h"
#import "NTESControlPanel.h"
#import "NTESClassroom.h"
#import "NTESLoginService.h"
#import "NTESAvatarView.h"
#import "NTESWhiteboardCmdHandler.h"
#import "NTESWhiteboardLines.h"
#import "NTESVideoView.h"
#import "NTESGLView.h"
#import "NTESTeacher.h"
#import "NTESStudent.h"
#import "NTESService.h"
#import "NTESLoginViewController.h"
#import <Photos/Photos.h>

#define TOTAL_PAEGS_COUNT 5

@interface NTESClassroomViewController () <NTESControlPanelDelegate, UIGestureRecognizerDelegate, NIMNetCallManagerDelegate, NTESWhiteboardCmdHandlerDelegate, NTESMeetingRTSManagerDelegate, UIGestureRecognizerDelegate, NIMSystemNotificationManagerDelegate>
@property(nonatomic, strong) UILabel *pageNumLabel;

@property(nonatomic, strong) NTESWhiteboardDrawView *demonView;

@property(nonatomic, strong) UIImageView *musicPage;

@property(nonatomic, assign) NSInteger currentPage;

@property(nonatomic, strong) NTESControlPanel *controlPanel;

@property(nonatomic, strong) NTESClassroom *classroom;

@property(nonatomic, assign) NTESUserRole currentRole;

@property (nonatomic, strong) NTESWhiteboardCmdHandler *cmdHander;

@property(nonatomic, strong) NSString *myUid;

@property(nonatomic, strong) NTESWhiteboardLines *lines;

@property(nonatomic, assign) NSInteger myDrawColor;

@property(nonatomic, strong) NTESVideoView *smallVideoView;

@property(nonatomic, strong) NTESVideoView *largeVideoView;

@property(nonatomic, strong) NTESDocumentShareInfo *shareDocInfo;

@property(nonatomic, strong) NSString *teachId;

@property(nonatomic, strong) NSString *studentId;

@property(nonatomic, strong) NTESAvatarView *stuStatusView;

@property(nonatomic, strong) NTESAvatarView *selfStatusView;

@property(nonatomic, assign) BOOL isStuJoined;

@property(nonatomic, assign) BOOL isSmallVideoForTeach;

@property(nonatomic, strong) NSString *avatarUrl;

@property(nonatomic, assign) NTESVideoMode videoMode;

@property(nonatomic, assign) BOOL isMuted;

@property(nonatomic, strong) UITapGestureRecognizer *tapGR;

@property(nonatomic, strong) UIPanGestureRecognizer *panGR;

@end

@implementation NTESClassroomViewController

- (void)dealloc {
    [UIApplication sharedApplication].idleTimerDisabled = NO;
    [[NTESMeetingRTSManager sharedManager] leaveCurrentConference];
    [[NIMAVChatSDK sharedSDK].netCallManager removeDelegate:self];
    [[NIMSDK sharedSDK].systemNotificationManager removeDelegate:self];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (instancetype)initWithClassroom:(NTESClassroom *)classroom andRole:(NTESUserRole)role {
    if (self = [super init]) {
        self.classroom = classroom;
        self.currentRole = role;
        self.myUid = [[NIMSDK sharedSDK].loginManager currentAccount];
        self.lines = [NTESWhiteboardLines new];
        self.myDrawColor = 0;
        self.currentPage = 1;
        NTESTeacher *teacher = self.classroom.teacher;
        self.teachId = teacher.userId;
        NTESStudent *student = self.classroom.student;
        self.studentId = student.userId;
        self.isStuJoined = NO;
        self.isSmallVideoForTeach = YES;
        self.avatarUrl = nil;
        self.isMuted = NO;
        self.videoMode = NTESVideoModeVoice;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [UIApplication sharedApplication].idleTimerDisabled = YES;
    [self addDelegation];
    [self configNav];
    self.view.backgroundColor = [UIColor whiteColor];
    __weak typeof(self) wself = self;
    [@[self.pageNumLabel,
       self.musicPage,
       self.demonView,
       self.controlPanel,
       self.largeVideoView,
       self.smallVideoView,] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
          [wself.view addSubview:view];
      }];
    self.musicPage.image = [UIImage imageNamed:self.pageList[self.currentPage - 1]];
}

- (void)viewDidLayoutSubviews {
    self.musicPage.left = self.view.left + 16 * UIScreenWidthScale;
    self.musicPage.width = self.view.width - 32 * UIScreenWidthScale;
    self.musicPage.top = TopHeight + 20 * UIScreenWidthScale;
    self.musicPage.height = 1.25f * self.musicPage.width;
    
    self.demonView.frame = self.musicPage.frame;
    
    self.pageNumLabel.top = self.musicPage.bottom + 6;
    self.pageNumLabel.width = 50 * UIScreenWidthScale;
    self.pageNumLabel.height = 20 * UIScreenWidthScale;
    self.pageNumLabel.centerX = self.view.centerX;
}

- (void)addDelegation {
    [[NIMAVChatSDK sharedSDK].netCallManager addDelegate:self];
    [[NTESMeetingRTSManager sharedManager] setDelegate:self];
    [[NTESMeetingRTSManager sharedManager] setDataHandler:self.cmdHander];
    [[NIMSDK sharedSDK].systemNotificationManager addDelegate:self];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(kickout:) name:NTESNotificationLogout object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(netStatusChanged:) name:kRealReachabilityChangedNotification object:nil];
    
    NSError *error;
    if (self.currentRole == NTESUserRoleTeacher) {
        error = [[NTESMeetingRTSManager sharedManager] reserveConference:self.classroom.roomId];
    }else {
        error = [[NTESMeetingRTSManager sharedManager] joinConference:self.classroom.roomId];
    }
    if (error) {
        DDLogError(@"Error %zd reserve/join rts conference: %@", error.code, self.classroom.roomId);
    }
}

- (void)netStatusChanged:(NSNotification *)notification {
    ReachabilityStatus currentStatus = [[RealReachability sharedInstance] currentReachabilityStatus];
    if (currentStatus == RealStatusViaWWAN || currentStatus == RealStatusViaWiFi ) {
        [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_good"] forState:UIControlStateNormal];
        [self.selfStatusView.netStatusBtn setTitle:@"极好" forState:UIControlStateNormal];
    }
    if (currentStatus == RealStatusUnknown || currentStatus == RealStatusNotReachable) {
        [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_bad"] forState:UIControlStateNormal];
        [self.selfStatusView.netStatusBtn setTitle:@"极差" forState:UIControlStateNormal];
    }
}

#pragma mark - NIMNetCallManagerDelegate

- (void)onNetStatus:(NIMNetCallNetStatus)status user:(NSString *)user {
    if (self.currentRole == NTESUserRoleTeacher) {
        if (self.avatarUrl) {
            NSURL *url = [NSURL URLWithString:self.avatarUrl];
            [self.selfStatusView.avatar sd_setImageWithURL:url];
        }
        switch (status) {
            case NIMNetCallNetStatusVeryGood:
            {
                if ([user isEqualToString:self.teachId]) {
                        [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_good"] forState:UIControlStateNormal];
                        [self.selfStatusView.netStatusBtn setTitle:@"极好" forState:UIControlStateNormal];
                }
                if ([user isEqualToString:self.studentId]) {
                    if (!self.isStuJoined) {
                        [self.stuStatusView.netStatusBtn setTitle:@"离线" forState:UIControlStateNormal];
                    }else {
                        [self.stuStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_good"] forState:UIControlStateNormal];
                        [self.stuStatusView.netStatusBtn setTitle:@"极好" forState:UIControlStateNormal];
                    }
                }
            }
                break;
            case NIMNetCallNetStatusGood:
            {
                if ([user isEqualToString:self.teachId]) {
                    [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_normal"] forState:UIControlStateNormal];
                    [self.selfStatusView.netStatusBtn setTitle:@"一般" forState:UIControlStateNormal];
                }
                if ([user isEqualToString:self.studentId]) {
                    if (!self.isStuJoined) {
                        [self.stuStatusView.netStatusBtn setTitle:@"离线" forState:UIControlStateNormal];
                    }else {
                        [self.stuStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_normal"] forState:UIControlStateNormal];
                        [self.stuStatusView.netStatusBtn setTitle:@"一般" forState:UIControlStateNormal];
                    }
                }
            }
                break;
            case NIMNetCallNetStatusBad:
            case NIMNetCallNetStatusPoor:
            case NIMNetCallNetStatusVeryBad:
            {
                if ([user isEqualToString:self.teachId]) {
                    [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_bad"] forState:UIControlStateNormal];
                    [self.selfStatusView.netStatusBtn setTitle:@"极差" forState:UIControlStateNormal];
                }
                if ([user isEqualToString:self.studentId]) {
                    if (!self.isStuJoined) {
                        [self.stuStatusView.netStatusBtn setTitle:@"离线" forState:UIControlStateNormal];
                    }else {
                        [self.stuStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_bad"] forState:UIControlStateNormal];
                        [self.stuStatusView.netStatusBtn setTitle:@"极差" forState:UIControlStateNormal];
                    }
                }
            }
                break;
            default:
                break;
        }
    }
    else {
        switch (status) {
            case NIMNetCallNetStatusVeryGood:
            {
                if ([user isEqualToString:self.myUid]) {
                    [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_good"] forState:UIControlStateNormal];
                    [self.selfStatusView.netStatusBtn setTitle:@"极好" forState:UIControlStateNormal];
                }
            }
                break;
            case NIMNetCallNetStatusGood:
            {
                if ([user isEqualToString:self.myUid]) {
                    [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_normal"] forState:UIControlStateNormal];
                    [self.selfStatusView.netStatusBtn setTitle:@"一般" forState:UIControlStateNormal];
                }
            }
                break;
            case NIMNetCallNetStatusBad:
            case NIMNetCallNetStatusPoor:
            case NIMNetCallNetStatusVeryBad:
            {
                if ([user isEqualToString:self.myUid]) {
                    [self.selfStatusView.netStatusBtn setImage:[UIImage imageNamed:@"room_net_status_bad"] forState:UIControlStateNormal];
                    [self.selfStatusView.netStatusBtn setTitle:@"极差" forState:UIControlStateNormal];
                }
            }
                break;
            default:
                break;
        }
    }
}

- (void)onRemoteYUVReady:(NSData *)yuvData width:(NSUInteger)width height:(NSUInteger)height from:(NSString *)user {
    if (self.isSmallVideoForTeach) {
        [self.smallVideoView renderWhenYUVReady:yuvData width:width height:height from:user];
    }
    if (!self.isSmallVideoForTeach) {
        [self.largeVideoView renderWhenYUVReady:yuvData width:width height:height from:user];
    }
}

- (void)onControl:(UInt64)callID from:(NSString *)user type:(NIMNetCallControlType)control {
    if ([user isEqualToString:self.teachId] && self.currentRole == NTESUserRoleStudent) {
        switch (control) {
            case NIMNetCallControlTypeOpenVideo:
            {
                [self bothVideoViewisShow:YES];
            }
                break;
            case NIMNetCallControlTypeCloseVideo:
            {
                [self bothVideoViewisShow:NO];
            }
                break;
            default:
                break;
        }
    }
}

- (void)startLocalPreview {
    UIView *preview = [[NIMAVChatSDK sharedSDK].netCallManager localPreview];
    preview.userInteractionEnabled = YES;
    if (self.isSmallVideoForTeach) {
        [self.largeVideoView addSubview:preview];
        [self layoutLocalPreview:preview inVideoView:self.largeVideoView];
    }
    else {
        [self.smallVideoView addSubview:preview];
        [self layoutLocalPreview:preview inVideoView:self.smallVideoView];
    }
}

- (void)layoutLocalPreview:(UIView *)preview inVideoView:(NTESVideoView *)view {
    preview.frame = view.bounds;
}

#pragma mark  - UIResponder

- (BOOL)rect:(CGRect)rect containPoint:(CGPoint)point
{
    return (point.x > rect.origin.x
            && point.x < rect.origin.x + rect.size.width
            && point.y > rect.origin.y
            && point.y < rect.origin.y + rect.size.height);
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    if (!self.controlPanel.coverView.hidden) return;
    CGPoint p = [[touches anyObject] locationInView:self.demonView];
    for (UITouch *touch in touches) {
        if ([touch.view isKindOfClass:[NTESGLView class]] || self.currentRole == NTESUserRoleStudent)
            return ;
    }
    if ([self rect:self.musicPage.bounds containPoint:p] && [touches anyObject]) {
        [self onPointCollected:p type:NTESWhiteboardPointTypeStart];
    }
    else {
        DDLogInfo(@"current touch point %lf, %lf %ld, %lf, %lf", p.x, p.y, [touches count], self.musicPage.bounds.size.width, self.musicPage.bounds.size.height);
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    if (!self.controlPanel.coverView.hidden) return;
    CGPoint p = [[touches anyObject] locationInView:self.demonView];
    for (UITouch *touch in touches) {
        if ([touch.view isKindOfClass:[NTESGLView class]] || self.currentRole == NTESUserRoleStudent)
            return ;
    }
    if ([self rect:self.musicPage.bounds containPoint:p]) {
        [self onPointCollected:p type:NTESWhiteboardPointTypeMove];
    }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    if (!self.controlPanel.coverView.hidden) return;
    CGPoint p = [[touches anyObject] locationInView:self.demonView];
    for (UITouch *touch in touches) {
        if ([touch.view isKindOfClass:[NTESGLView class]] || self.currentRole == NTESUserRoleStudent)
            return ;
    }
    if ([self rect:self.musicPage.bounds containPoint:p]) {
        [self onPointCollected:p type:NTESWhiteboardPointTypeEnd];
    }
}

- (void)onPointCollected:(CGPoint)p type:(NTESWhiteboardPointType)type {
    NTESWhiteboardPoint *point = [[NTESWhiteboardPoint alloc] init];
    point.pageIndex = (int)self.currentPage;
    point.type = type;
    point.xScale = p.x/self.demonView.frame.size.width;
    point.yScale = p.y/self.demonView.frame.size.height;
    point.colorRGB = [self.colorList[self.myDrawColor] intValue];
    [self.cmdHander sendMyPoint:point];
    [self.lines addPoint:point uid:self.myUid];
}

# pragma mark - NTESMeetingRTSManagerDelegate

- (void)onReserve:(NSString *)name result:(NSError *)result
{
    if (result == nil) {
        NSError *result = [[NTESMeetingRTSManager sharedManager] joinConference:self.classroom.roomId];
        DDLogError(@"join rts conference: %@ result %zd", self.classroom.roomId, result.code);
    }
    else {
        [self.view makeToast:[NSString stringWithFormat:@"预订白板出错:%zd", result.code]];
    }
}

- (void)onJoin:(NSString *)name result:(NSError *)result
{
    if (self.currentRole == NTESUserRoleTeacher) {
        [self.cmdHander sendPureCmd:NTESWhiteBoardCmdTypeSyncPrepare to:nil];
        [self onSendDocShareInfoToUser:nil];
        [self.cmdHander sync:[self.lines allLines] toUser:nil];
        [self syncVideoMode:self.videoMode];
        [[NIMAVChatSDK sharedSDK].netCallManager setMute:self.isMuted];
    }
    if (self.currentRole == NTESUserRoleStudent) {
        [self.lines clear];
        [self.cmdHander sendPureCmd:NTESWhiteBoardCmdTypeSyncRequest to:self.teachId];
    }
}

- (void)onLeft:(NSString *)name result:(NSError *)error
{
    NSError *result = [[NTESMeetingRTSManager sharedManager] joinConference:self.classroom.roomId];
    DDLogError(@"Rejoin rts conference: %@ result %zd", self.classroom.roomId, result.code);
}

- (void)onUserJoined:(NSString *)uid conference:(NSString *)name {
    if (self.currentRole == NTESUserRoleTeacher && [uid isEqualToString:self.studentId]) {
        _isStuJoined = YES;
        [self syncVideoMode:self.videoMode];
        [[NIMAVChatSDK sharedSDK].netCallManager setMute:self.isMuted];
    }
}

- (void)onUserLeft:(NSString *)uid conference:(NSString *)name {
    if ([uid isEqualToString:self.studentId]) {
        _isStuJoined = NO;
        if (self.currentRole == NTESUserRoleTeacher) {
            [self.stuStatusView.netStatusBtn setTitle:@"离线" forState:UIControlStateNormal];
            [self.stuStatusView.netStatusBtn setImage:nil forState:UIControlStateNormal];
        }
    }
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
                __weak typeof(self) wself = self;
                UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:nil message:@"本节课已结束了哦" preferredStyle:UIAlertControllerStyleAlert];
                [alertVC addAction:@"我知道了哦" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                    [wself doExit];
                }];
                [alertVC show];
            }
        }
    }
}

#pragma mark - NTESWhiteboardCmdHandlerDelegate
- (void)onReceivePoint:(NTESWhiteboardPoint *)point from:(NSString *)sender
{
    [_lines addPoint:point uid:sender];
}

- (void)onReceiveCmd:(NTESWhiteBoardCmdType)type from:(NSString *)sender
{
    if (type == NTESWhiteBoardCmdTypeCancelLine) {
        [_lines cancelLastLine:sender inPage:(int)self.currentPage];
    }
    else if (type == NTESWhiteBoardCmdTypeClearLines) {
        [_lines clearUser:self.teachId currentPagelines:self.currentPage];
        [_cmdHander sendPureCmd:NTESWhiteBoardCmdTypeClearLinesAck to:nil];
    }
    else if (type == NTESWhiteBoardCmdTypeClearLinesAck) {
        [_lines clearUser:sender currentPagelines:self.currentPage];
    }
    else if (type == NTESWhiteBoardCmdTypeSyncPrepare) {
        [_lines clear];
        [_cmdHander sendPureCmd:NTESWhiteBoardCmdTypeSyncPrepareAck to:sender];
    }
}

- (void)onReceiveSyncRequestFrom:(NSString *)sender
{
    if (self.currentRole == NTESUserRoleTeacher) {
        _isStuJoined = YES;
        [self onSendDocShareInfoToUser:sender];
        [_cmdHander sync:[_lines allLines] toUser:sender];
    }
}

- (void)onReceiveSyncPoints:(NSMutableDictionary *)points owner:(NSString *)owner
{
    for (int i = 1; i <= 5; ++i) {
        NSArray *array = [points objectForKey:@(i)];
        for (NTESWhiteboardPoint *point in array) {
            [_lines addPoint:point uid:owner];
        }
    }
}

-(void)onReceiveDocShareInfo:(NTESDocumentShareInfo *)shareInfo from:(NSString *)sender
{
    if (sender == self.teachId) {//判断一下是否是自己老师
        if (self.currentPage < shareInfo.currentPage) {
            [self musicPageSwipeWithDirection:kCATransitionFromRight];
        }
        else if (self.currentPage > shareInfo.currentPage) {
            [self musicPageSwipeWithDirection:kCATransitionFromLeft];
        }
        else return;
        self.currentPage = shareInfo.currentPage;
        if (self.currentPage >= 1 && self.currentPage <= 5) {
            [self.musicPage setImage:[UIImage imageNamed:self.pageList[self.currentPage -1]]];
            self.pageNumLabel.text = [NSString stringWithFormat:@"%ld/5 页", self.currentPage];
        }
        [self.lines changeCurrentPage:self.currentPage];
    }
}

- (void)onSendDocShareInfoToUser:(NSString *)sender {
    NTESDocumentShareInfo *shareInfo = [[NTESDocumentShareInfo alloc]init];
    shareInfo.docId = self.shareDocInfo.docId;
    shareInfo.currentPage = (int)self.currentPage;
    shareInfo.pageCount = (int)self.shareDocInfo.pageCount;
    shareInfo.type = NTESDocShareTypeTurnThePage;
        
    [_cmdHander sendDocShareInfo:shareInfo toUser:sender];
}

#pragma mark - Delegate

- (void)controlPanel:(NTESControlPanel *)panel prePageBtnTapped:(UIButton *)preBtn {
    if (self.currentPage > 1) {
        preBtn.enabled = YES;
        self.currentPage--;
        [self musicPageSwipeWithDirection:kCATransitionFromLeft];
        self.pageNumLabel.text = [NSString stringWithFormat:@"%ld/%d 页", self.currentPage, TOTAL_PAEGS_COUNT];
        self.musicPage.image = [UIImage imageNamed:self.pageList[self.currentPage - 1]];
        [self onSendDocShareInfoToUser:self.studentId];
    }
    else {
        self.currentPage = 1;
        [self.view makeToast:@"已经是第一页了哦" duration:2. position:CSToastPositionCenter];
    }
    if (self.currentPage == 1) {
        preBtn.enabled = NO;
    }
    [self.lines changeCurrentPage:self.currentPage];
}

- (void)controlPanel:(NTESControlPanel *)panel nextPageBtnTapped:(UIButton *)nextBtn {
    if (self.currentPage < TOTAL_PAEGS_COUNT) {
        nextBtn.enabled = YES;
        self.currentPage++;
        self.pageNumLabel.text = [NSString stringWithFormat:@"%ld/%d 页", self.currentPage, TOTAL_PAEGS_COUNT];
        self.musicPage.image = [UIImage imageNamed:self.pageList[self.currentPage - 1]];
        [self musicPageSwipeWithDirection:kCATransitionFromRight];
        [self onSendDocShareInfoToUser:self.studentId];
    }
    else {
        self.currentPage = TOTAL_PAEGS_COUNT;
        [self.view makeToast:@"已经是最后一页，翻不动啦(˶‾᷄ ⁻̫ ‾᷅˵)" duration:2 position:CSToastPositionCenter];
    }
    if (self.currentPage == TOTAL_PAEGS_COUNT) {
        nextBtn.enabled = NO;
    }
    [self.lines changeCurrentPage:self.currentPage];
}

- (void)musicPageSwipeWithDirection:(NSString *)direction {
    CATransition *animation = [CATransition animation];
    animation.duration = 0.3f;
    animation.type = @"pageCurl";
    if (direction) {
        animation.subtype = direction;
    }
    [self.demonView.layer addAnimation:animation forKey:@"animation"];
    [self.musicPage.layer addAnimation:animation forKey:@"animation"];
}

- (void)controlPanel:(NTESControlPanel *)panel drawWtihColor:(NSInteger)selectColor {
    self.myDrawColor = selectColor - 1;
}

- (void)controlPanel:(NTESControlPanel *)panel muteVoice:(BOOL)isMuted {
    self.isMuted = isMuted;
    [[NIMAVChatSDK sharedSDK].netCallManager setMute:self.isMuted];
}

- (void)controlPanel:(NTESControlPanel *)panel selectVideoMode:(NTESVideoMode)mode {
    [self syncVideoMode:mode];
}

- (void)syncVideoMode:(NTESVideoMode)mode {
    Uint64 callID = [[NIMAVChatSDK sharedSDK].netCallManager currentCallID];
    switch (mode) {
        case NTESVideoModeVoice:
        {
            self.controlPanel.paintBtn.enabled = YES;
            self.controlPanel.prePageBtn.enabled = self.currentPage != 1;
            self.controlPanel.nextPageBtn.enabled = self.currentPage != TOTAL_PAEGS_COUNT;
            [self smallVideoViewisShow:NO];
            [self bothVideoViewisShow:NO];
            [[NIMAVChatSDK sharedSDK].netCallManager control:callID type:NIMNetCallControlTypeCloseVideo];
            self.videoMode = NTESVideoModeVoice;
        }
            break;
        case NTESVideoModeDuplex:
        {
            self.controlPanel.paintBtn.enabled = NO;
            self.controlPanel.prePageBtn.enabled = NO;
            self.controlPanel.nextPageBtn.enabled = NO;
            [self smallVideoViewisShow:YES];
            [self bothVideoViewisShow:YES];
            [[NIMAVChatSDK sharedSDK].netCallManager control:callID type:NIMNetCallControlTypeOpenVideo];
            self.videoMode = NTESVideoModeDuplex;
        }
            break;
        case NTESVideoModeSingle:
        {
            self.controlPanel.paintBtn.enabled = YES;
            self.controlPanel.prePageBtn.enabled = self.currentPage != 1;
            self.controlPanel.nextPageBtn.enabled = self.currentPage != TOTAL_PAEGS_COUNT;
            self.isSmallVideoForTeach = YES;
            [self bothVideoViewisShow:NO];
            [self smallVideoViewisShow:YES];
            [[NIMAVChatSDK sharedSDK].netCallManager control:callID type:NIMNetCallControlTypeCloseVideo];
            self.videoMode = NTESVideoModeSingle;
        }
            break;
        default:
            break;
    }
}

- (void)cancelLastLineWithControlPanel:(NTESControlPanel *)panel {
    if (self.currentRole == NTESUserRoleTeacher) {
        [self.lines cancelLastLine:self.myUid inPage:self.currentPage];
        [self.cmdHander sendPureCmd:NTESWhiteBoardCmdTypeCancelLine to:nil];
    }
}

- (void)clearAlllineWithControlPanel:(NTESControlPanel *)panel {
    if (self.currentRole == NTESUserRoleTeacher) {
        [self.lines clearUser:self.teachId currentPagelines:self.currentPage];
        [self.cmdHander sendPureCmd:NTESWhiteBoardCmdTypeClearLines to:nil];
    }
}

#pragma mark - Action

- (void)kickout:(NSNotification *)notification {
    __weak typeof(self) wself = self;
    UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:nil message:@"账号已经在其他端被登录" preferredStyle:UIAlertControllerStyleAlert];
    [alertVC addAction:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [wself doExitToLogin];
    }];
    [alertVC show];
}

- (void)onExitClassroom:(UIButton *)sender {
    __weak typeof(self) wself = self;
    if (self.currentRole == NTESUserRoleTeacher) {
        UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:nil message:@"下课后无法再进入该房间，确认下课吗？" preferredStyle:UIAlertControllerStyleAlert];
        [[alertVC addAction:@"确认" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [wself doExit];
        }]addAction:@"取消" style:UIAlertActionStyleCancel handler:nil];
        [alertVC show];
    }
    else {
        [self leaveConference];
        [self.navigationController popViewControllerAnimated:YES];
    }
}

- (void)doExit {
    if (self.currentRole == NTESUserRoleTeacher)
    {
        [[NTESService sharedService].teahcerService closeClass:self.classroom completion:nil];
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:NTESNotificationClassover object:nil];

    [self leaveConference];
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)doExitToLogin {
    [NTESLoginData clear];
    [self leaveConference];
    for (UIViewController *vc in self.navigationController.viewControllers) {
        if ([vc isKindOfClass:[NTESLoginViewController class]]) {
            [self.navigationController popToViewController:vc animated:YES];
            break;
        }
    }
}

- (void)panGesDetect:(UIPanGestureRecognizer *)recognizer {
    UIGestureRecognizerState state = [recognizer state];
    if (state == UIGestureRecognizerStateEnded || state == UIGestureRecognizerStateChanged) {
        CGPoint transLation = [recognizer translationInView:self.largeVideoView];
        UIView *dragView = [self.view viewWithTag:100];
        
        if (dragView) {
            CGPoint newCenter = CGPointMake(dragView.center.x + transLation.x, dragView.center.y + transLation.y);
            //限制不能拖出界面
            CGRect bounds = CGRectMake(0, TopHeight, self.view.width, self.view.height - TopHeight);
            CGFloat halfx = CGRectGetMinX(bounds) + dragView.width/2;
            newCenter.x = MAX(halfx, newCenter.x);
            CGFloat width = self.view.width;
            newCenter.x = MIN(width - halfx, newCenter.x);
            CGFloat halfy = CGRectGetMinY(bounds) + dragView.height/2;
            newCenter.y = MAX(halfy, newCenter.y);
            CGFloat height = self.view.height - TopHeight;
            newCenter.y = MIN(height - halfy, newCenter.y);
            [dragView setCenter:newCenter];
            [recognizer setTranslation:CGPointZero inView:self.largeVideoView];
        }
    }
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(nonnull UIGestureRecognizer *)otherGestureRecognizer {
    return YES;
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(nonnull UITouch *)touch {
    CGPoint point = [touch locationInView:self.view];
    if ([self rect:self.smallVideoView.frame containPoint:point]) {
        return YES;
    }
    return NO;
}

- (void)tapGestureDetect:(UITapGestureRecognizer *)recognizer {
    if (self.largeVideoView.hidden == NO && self.smallVideoView.alpha == 1) {
        self.isSmallVideoForTeach = !self.isSmallVideoForTeach;
        [self startLocalPreview];
    }
}

#pragma mark - Private

- (void)leaveConference
{
    [[NIMAVChatSDK sharedSDK].netCallManager leaveMeeting:self.classroom.meeting];
    [[NTESMeetingRTSManager sharedManager] leaveCurrentConference];
}

- (void)smallVideoViewisShow:(BOOL)show {
    __weak typeof(self) wself = self;
    if (show) {
        AVAuthorizationStatus videoAuth = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
        if (videoAuth == AVAuthorizationStatusAuthorized) {
            [UIView animateWithDuration:0.2 animations:^{
                [self startLocalPreview];
                wself.smallVideoView.alpha = 1;
            }];
        }
        else {
            NSString *msg = @"你没有开启相机权限哦～";
            [self.view makeToast:msg duration:2 position:CSToastPositionCenter];
        }
    }
    else {
        [UIView animateWithDuration:0.2 animations:^{
            wself.smallVideoView.alpha = 0;
        }];
    }
}

- (void)bothVideoViewisShow:(BOOL)show {
    __weak typeof(self) wself = self;
    if (show) {
        AVAuthorizationStatus videoAuth = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
        if (videoAuth == AVAuthorizationStatusAuthorized) {
            if (show) {
                [UIView animateWithDuration:0.2 animations:^{
                    wself.largeVideoView.hidden = NO;
                    wself.smallVideoView.alpha = 1.;
                    [wself startLocalPreview];
                }];
            }
        }
        else {
            NSString *msg = @"你没有开启相机权限哦～";
            [self.view makeToast:msg duration:2 position:CSToastPositionCenter];
            self.largeVideoView.hidden = YES;
            if (self.currentRole == NTESUserRoleTeacher) {
                Uint64 callID = [[NIMAVChatSDK sharedSDK].netCallManager currentCallID];
                [[NIMAVChatSDK sharedSDK].netCallManager control:callID type:NIMNetCallControlTypeCloseVideo];
            }
        }
    }
    else {
        [UIView animateWithDuration:0.2 animations:^{
            wself.largeVideoView.hidden = YES;
            wself.smallVideoView.alpha = 0;
        }];
    }
}

- (void)configNav {
    NSString *userId = [[NTESLoginData read] userId];
    __weak typeof(self) wself = self;
    [[NIMSDK sharedSDK].userManager fetchUserInfos:@[userId] completion:^(NSArray<NIMUser *> * _Nullable users, NSError * _Nullable error) {
        if (!error) {
            NIMUser *user = users.firstObject;
            if (user.userId == userId) {
                NIMUserInfo *info = user.userInfo;
                wself.avatarUrl = info.avatarUrl;
            }
        }
    }];
    NSString *titleStr = nil;
    self.selfStatusView = [[NTESAvatarView alloc] initWithFrame:CGRectMake(0, 0, 40, 14) Role:NTESUserRoleStudent andImageURL:nil];
    if (self.currentRole == NTESUserRoleStudent) {
        titleStr = @"退出";
        UIBarButtonItem *leftItem = [[UIBarButtonItem alloc] initWithCustomView:self.selfStatusView];
        self.navigationItem.leftBarButtonItems = @[leftItem];
    }
    else {
        titleStr = @"下课";
        self.stuStatusView = [[NTESAvatarView alloc] initWithFrame:CGRectMake(0, 0, 80, 38) Role:NTESUserRoleTeacher andImageURL:self.avatarUrl];
        UIBarButtonItem *leftStuItem = [[UIBarButtonItem alloc] initWithCustomView:self.stuStatusView];
        UIBarButtonItem *leftItem = [[UIBarButtonItem alloc] initWithCustomView:self.selfStatusView];
        self.navigationItem.leftBarButtonItems = @[leftStuItem, leftItem];
    }
    UIButton *rightBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    rightBtn.size = CGSizeMake(50, 28);
    rightBtn.layer.cornerRadius = 3;
    rightBtn.clipsToBounds = YES;
    rightBtn.titleLabel.font = font(13);
    [rightBtn setBackgroundImage:[UIImage imageNamed:@"login_enter"] forState:UIControlStateNormal];
    [rightBtn setBackgroundImage:[UIImage imageNamed:@"login_enter_pressed"] forState:UIControlStateHighlighted];
    [rightBtn setTitle:titleStr forState:UIControlStateNormal];
    [rightBtn addTarget:self action:@selector(onExitClassroom:) forControlEvents:UIControlEventTouchUpInside];
    if (@available(iOS 11, *)) {
        NSLayoutConstraint *widthConstraint = [rightBtn.widthAnchor constraintEqualToConstant:50];
        NSLayoutConstraint *heightConstraint = [rightBtn.heightAnchor constraintEqualToConstant:28];
        [widthConstraint setActive:YES];
        [heightConstraint setActive:YES];
    }
    UIBarButtonItem *rightItem = [[UIBarButtonItem alloc] initWithCustomView:rightBtn];
    self.navigationItem.rightBarButtonItems = @[rightItem];
}

#pragma mark - Getter

- (UIImageView *)musicPage {
    if (!_musicPage) {
        _musicPage = ({
            UIImageView *page = [UIImageView new];
            page.backgroundColor = [UIColor clearColor];
            page.userInteractionEnabled = NO;
            page.contentMode = UIViewContentModeScaleAspectFill;
            page.layer.shadowColor = [UIColor lightGrayColor].CGColor;
            page.layer.shadowOffset = CGSizeMake(0, 0);
            page.layer.shadowOpacity = 0.5;
            page.layer.shadowRadius = 6.0;
            page;
        });
    }
    return _musicPage;
}

- (NTESWhiteboardDrawView *)demonView {
    if (!_demonView) {
        _demonView = ({
            NTESWhiteboardDrawView *view = [NTESWhiteboardDrawView new];
            view.backgroundColor = [UIColor clearColor];
            view.dataSource = self.lines;
            view;
        });
    }
    return _demonView;
}

- (UILabel *)pageNumLabel {
    if (!_pageNumLabel) {
        _pageNumLabel = ({
            UILabel *label = [UILabel new];
            label.textColor = [UIColor lightGrayColor];
            label.textAlignment = NSTextAlignmentCenter;
            label.adjustsFontSizeToFitWidth = YES;
            label.text = [NSString stringWithFormat:@"%ld/%d 页", self.currentPage, TOTAL_PAEGS_COUNT];
            label;
        });
    }
    return _pageNumLabel;
}

- (NTESControlPanel *)controlPanel {
    if (!_controlPanel) {
        _controlPanel = ({
            NTESControlPanel *panel = [[NTESControlPanel alloc] initWithFrame:CGRectMake(0, TopHeight, self.view.width, self.view.height - TopHeight) andCurrentPage:self.currentPage];
            panel.delegate = self;
            if (self.currentRole == NTESUserRoleStudent) {
                panel.hidden = YES;
            }
            panel;
        });
    }
    return _controlPanel;
}

- (NSArray *)pageList {
    return @[@"music_page_1", @"music_page_2", @"music_page_3", @"music_page_4", @"music_page_5"];
}

- (NTESWhiteboardCmdHandler *)cmdHander {
    if (!_cmdHander) {
        _cmdHander = [[NTESWhiteboardCmdHandler alloc] initWithDelegate:self];
    }
    return _cmdHander;
}

- (NSArray *)colorList {
    return @[@(0xFF0000), @(0xEDB400), @(0x62A515), @(0xB322FB), @(0x4691E8)];
}

- (NTESVideoView *)smallVideoView {
    if (!_smallVideoView) {
        _smallVideoView = ({
            NTESVideoView *view = [[NTESVideoView alloc] initWithFrame:CGRectMake(16 * UIScreenWidthScale, TopHeight + 20 * UIScreenWidthScale, 100 * UIScreenWidthScale, 100 * UIScreenWidthScale * 16 / 9)];
            view.alpha = 0;
            [view addGestureRecognizer:self.panGR];
            [view addGestureRecognizer:self.tapGR];
            view.tag = 100;
            view;
        });
    }
    return _smallVideoView;
}

- (NTESVideoView *)largeVideoView {
    if (!_largeVideoView) {
        _largeVideoView = ({
            NTESVideoView *view = [[NTESVideoView alloc] initWithFrame:CGRectMake(0, TopHeight, self.view.width, self.view.height - 100 * UIScreenWidthScale - TopHeight)];
            view.hidden = YES;
            view;
        });
    }
    return _largeVideoView;
}

- (NTESDocumentShareInfo *)shareDocInfo {
    if (!_shareDocInfo) {
        _shareDocInfo = ({
            NTESDocumentShareInfo *info = [NTESDocumentShareInfo new];
            info.docId = @"123";
            info.currentPage = (int)self.currentPage;
            info.pageCount = 5;
            info.type = NTESDocShareTypeTurnThePage;
            info;
        });
    }
    return _shareDocInfo;
}

- (UIPanGestureRecognizer *)panGR {
    if (!_panGR) {
        _panGR = ({
            UIPanGestureRecognizer *panGR = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panGesDetect:)];
            panGR.delegate = self;
            panGR.maximumNumberOfTouches = 1;
            panGR.minimumNumberOfTouches = 1;
            panGR;
        });
    }
    return _panGR;
}

- (UITapGestureRecognizer *)tapGR {
    if (!_tapGR) {
        _tapGR = ({
            UITapGestureRecognizer *tapGR = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapGestureDetect:)];
            tapGR.delegate = self;
            tapGR;
        });
    }
    return _tapGR;
}

@end
