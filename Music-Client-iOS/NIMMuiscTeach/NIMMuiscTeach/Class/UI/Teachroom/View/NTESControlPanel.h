//
//  NTESControlPanel.h
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/2.
//  Copyright © 2018 netease. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, NTESVideoMode) {
    NTESVideoModeVoice = 1,
    NTESVideoModeSingle,
    NTESVideoModeDuplex,
};

@class NTESControlPanel;
@protocol NTESControlPanelDelegate <NSObject>

- (void)controlPanel:(NTESControlPanel *)panel selectVideoMode:(NTESVideoMode)mode;

- (void)controlPanel:(NTESControlPanel *)panel drawWtihColor:(NSInteger)selectColor;

- (void)clearAlllineWithControlPanel:(NTESControlPanel *)panel;

- (void)cancelLastLineWithControlPanel:(NTESControlPanel *)panel;

- (void)controlPanel:(NTESControlPanel *)panel muteVoice:(BOOL)isMuted;

- (void)controlPanel:(NTESControlPanel *)panel prePageBtnTapped:(UIButton *)preBtn;

- (void)controlPanel:(NTESControlPanel *)panel nextPageBtnTapped:(UIButton *)nextBtn;

@end

@interface NTESControlPanel : UIView

@property(nonatomic, weak) id<NTESControlPanelDelegate> delegate;

@property(nonatomic, strong) UIButton *paintBtn;
@property(nonatomic, strong) UIButton *prePageBtn;
@property(nonatomic, strong) UIButton *nextPageBtn;
@property(nonatomic, strong) UIView *coverView;

- (instancetype)initWithFrame:(CGRect)frame andCurrentPage:(NSInteger)currPage;

@end
