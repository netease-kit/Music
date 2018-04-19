//
//  NTESLoginViewController.m
//  NIMDemo
//
//  Created by ght on 15-1-26.
//  Copyright (c) 2015年 Netease. All rights reserved.
//

#import "NTESLoginViewController.h"
#import "UIView+Toast.h"
#import "SVProgressHUD.h"
#import "NTESService.h"
#import "UIView+NTES.h"
#import "NTESRegisterViewController.h"
#import "NTESBasicDefine.h"
#import "NSString+NTES.h"
#import "NTESHallwayViewController.h"

@interface NTESLoginViewController ()<NTESRegisterViewControllerDelegate>
@property (weak, nonatomic) IBOutlet UIButton *registerButton;
@property (strong, nonatomic) IBOutlet UITextField *usernameTextField;
@property (strong, nonatomic) IBOutlet UITextField *passwordTextField;
@property (strong, nonatomic) IBOutlet UIImageView *logo;
@property(nonatomic, strong) UILabel *nameLabel;
@property(nonatomic, strong) UIButton *loginBtn;
@property(nonatomic, strong) UILabel *hintLabel;
@end

@implementation NTESLoginViewController

- (instancetype)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(textFieldDidChange:) name:UITextFieldTextDidChangeNotification object:nil];
    }
    return self;
}

- (void)dealloc{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


NTES_USE_CLEAR_BAR
- (void)viewDidLoad {
    [super viewDidLoad];
    self.usernameTextField.tintColor = [UIColor whiteColor];
    [self.usernameTextField setValue:UIColorFromRGBA(0xffffff, .6f) forKeyPath:@"_placeholderLabel.textColor"];
    self.passwordTextField.tintColor = [UIColor whiteColor];
    [self.passwordTextField setValue:UIColorFromRGBA(0xffffff, .6f) forKeyPath:@"_placeholderLabel.textColor"];
    UIButton *pwdClearButton = [self.passwordTextField valueForKey:@"_clearButton"];
    [pwdClearButton setImage:[UIImage imageNamed:@"login_icon_clear"] forState:UIControlStateNormal];
    UIButton *userNameClearButton = [self.usernameTextField valueForKey:@"_clearButton"];
    [userNameClearButton setImage:[UIImage imageNamed:@"login_icon_clear"] forState:UIControlStateNormal];
    [self.view addSubview:self.loginBtn];
    [self.view addSubview:self.hintLabel];
    self.navigationItem.rightBarButtonItem.enabled = NO;
    
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [self configNav];
}

- (void)viewDidLayoutSubviews {
    self.loginBtn.width = self.view.width - 48 * UIScreenWidthScale;
    self.loginBtn.height = 42 * UIScreenWidthScale;
    self.loginBtn.top = self.view.centerY + 55 * UIScreenWidthScale;
    self.loginBtn.centerX = self.view.centerX;
    self.registerButton.top = self.loginBtn.bottom + 10;
    self.hintLabel.top = self.loginBtn.bottom + 86 * UIScreenWidthScale;
    self.hintLabel.width = self.view.width - 48 * UIScreenWidthScale;
    self.hintLabel.centerX = self.view.centerX;
    self.hintLabel.height = 50;
}


- (void)configNav{
    self.navigationItem.title = @"";
    NSShadow *shadow = [[NSShadow alloc]init];
    shadow.shadowOffset = CGSizeMake(0, 0);
    self.navigationController.navigationBar.titleTextAttributes =@{NSFontAttributeName:[UIFont boldSystemFontOfSize:17],                                                                   NSForegroundColorAttributeName:[UIColor whiteColor]};
}

- (void)doLogin
{
    [_usernameTextField resignFirstResponder];
    [_passwordTextField resignFirstResponder];
    
    NSString *username = [_usernameTextField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    NSString *password = _passwordTextField.text;
    [SVProgressHUD show];
    
    NSString *loginAccount = username;
    NSString *loginToken   = [password tokenByPassword];
    
    NTESLoginData *data = [[NTESLoginData alloc] init];
    data.userId = loginAccount;
    data.token  = loginToken;
    
    __weak typeof(self) weakSelf = self;
    
    [[NTESService sharedService].loginService login:data completion:^(NSError *error, NTESUserRole role) {
        [SVProgressHUD dismiss];
        if (error == nil)
        {
            NTESHallwayViewController *vc = [[NTESHallwayViewController alloc] initWithRole:role];
            [weakSelf.navigationController pushViewController:vc animated:YES];
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.25 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                weakSelf.navigationController.viewControllers = @[vc];
            });
        }
        else
        {
            NSString *toast = [NSString stringWithFormat:@"登录失败 code: %zd",error.code];
            [self.view makeToast:toast duration:2.0 position:CSToastPositionCenter];
        }
    }];

}

- (void)onTouchLogin:(id)sender
{
    UIButton *btn = (UIButton *)sender;
    btn.selected = !btn.selected;
    [self doLogin];
}

#pragma mark - Notification
- (void)keyboardWillShow:(NSNotification*)notification{
    NSDictionary* userInfo = [notification userInfo];
    NSTimeInterval animationDuration;
    UIViewAnimationCurve animationCurve;
    CGRect keyboardFrame;
    [[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] getValue:&animationCurve];
    [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] getValue:&animationDuration];
    [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] getValue:&keyboardFrame];
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:animationDuration];
    [UIView setAnimationCurve:animationCurve];
    CGFloat bottomSpacing = 10.f;
    UIView *inputView = self.passwordTextField.superview;
    if (inputView.bottom + bottomSpacing > CGRectGetMinY(keyboardFrame)) {
        CGFloat delta = inputView.bottom + bottomSpacing - CGRectGetMinY(keyboardFrame);
        inputView.bottom -= delta;
    }
//    if (self.logo.bottom > self.navigationController.navigationBar.bottom) {
//        self.logo.bottom = self.navigationController.navigationBar.bottom;
//        self.logo.alpha  = 0;
//        self.navigationItem.title = @"登录";
//    }
    [UIView commitAnimations];
}

#pragma mark - UITextFieldDelegate
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string{
    if ([string isEqualToString:@"\n"]) {
        [self doLogin];
        return NO;
    }
    return YES;
}

- (void)textFieldDidChange:(NSNotification*)notification{
    if ([self.usernameTextField.text length] && [self.passwordTextField.text length])
    {
        self.loginBtn.enabled = YES;
    }else{
        self.loginBtn.enabled = NO;
    }
}

- (void)textFieldDidBeginEditing:(UITextField *)textField{
    if ([self.usernameTextField.text length] && [self.passwordTextField.text length])
    {
        self.loginBtn.enabled = YES;
    }else{
        self.loginBtn.enabled = NO;
    }
}

#pragma mark - NTESRegisterViewControllerDelegate
- (void)registDidComplete:(NSString *)account password:(NSString *)password{
    if (account.length) {
        self.usernameTextField.text = account;
        self.passwordTextField.text = password;
        self.loginBtn.enabled = YES;
    }
}

#pragma mark - Private

- (IBAction)onTouchRegister:(id)sender
{
    NTESRegisterViewController *vc = [NTESRegisterViewController new];
    vc.delegate = self;
    [self.navigationController pushViewController:vc animated:YES];
}


- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    [super touchesBegan:touches withEvent:event];
    [_usernameTextField resignFirstResponder];
    [_passwordTextField resignFirstResponder];
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}


- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskPortrait;
}

- (UIButton *)loginBtn {
    if (!_loginBtn) {
        _loginBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            [btn setBackgroundImage:[UIImage imageNamed:@"hallway_enter_normal"] forState:UIControlStateNormal];
            [btn setBackgroundImage:[UIImage imageNamed:@"hallway_enter_disabled"] forState:UIControlStateDisabled];
            [btn setBackgroundImage:[UIImage imageNamed:@"hallway_enter_pressed"] forState:UIControlStateSelected];
            [btn setTitle:@"登录" forState:UIControlStateNormal];
            [btn setTitleColor:UIColorFromRGB(0x5294ed) forState:UIControlStateNormal];
            btn.enabled = NO;
            [btn addTarget:self action:@selector(onTouchLogin:) forControlEvents:UIControlEventTouchUpInside];
            btn;
        });
    }
    return _loginBtn;
}

- (UILabel *)hintLabel {
    if (!_hintLabel) {
        _hintLabel = ({
            UILabel *label = [UILabel new];
            label.textAlignment = NSTextAlignmentCenter;
            label.textColor = [UIColor whiteColor];
            label.numberOfLines = 0;
            label.adjustsFontSizeToFitWidth = YES;
            label.text = @"老师端无需注册，老师端账号可在学生端预约课程后获取";
            label.font = font(14.f);
            label;
        });
    }
    return _hintLabel;
}

@end
