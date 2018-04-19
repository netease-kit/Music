//
//  NTESRegisterViewController.m
//  NIM
//
//  Created by amao on 8/10/15.
//  Copyright (c) 2015 Netease. All rights reserved.
//

#import "NTESRegisterViewController.h"
#import "NTESService.h"
#import "UIView+Toast.h"
#import "UIView+NTES.h"
#import "SVProgressHUD.h"
#import "NTESBasicDefine.h"
#import "NSString+NTES.h"

@interface NTESRegisterViewController ()

@property(nonatomic, strong) UIButton *registerBtn;

@end

@implementation NTESRegisterViewController

NTES_USE_CLEAR_BAR
- (void)viewDidLoad {
    [super viewDidLoad];
    [self.view addSubview:self.registerBtn];
    [self resetTextField:self.accountTextfield];
    [self resetTextField:self.nicknameTextfield];
    [self resetTextField:self.passwordTextfield];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillChangeFrame:) name:UIKeyboardWillChangeFrameNotification object:nil];
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [self setupNav];
}

- (void)viewDidLayoutSubviews {
    self.registerBtn.top = self.view.centerY + 120 * UIScreenWidthScale;
    self.registerBtn.height = 40 * UIScreenWidthScale;
    self.registerBtn.width = self.view.width - 48 * UIScreenWidthScale;
    self.registerBtn.centerX = self.view.centerX;
    self.existedButton.top = self.registerBtn.bottom + 15;
}

- (void)dealloc{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)setupNav
{
    UIImage *image = [UIImage imageNamed:@"icon_back_normal.png"];
    [self.navigationController.navigationBar setBackIndicatorImage:image];
    [self.navigationController.navigationBar setBackIndicatorTransitionMaskImage:image];
    UIBarButtonItem *backItem = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:nil];
    [self.navigationController.navigationBar setTintColor:UIColorFromRGB(0xffffff)];
    self.navigationItem.backBarButtonItem = backItem;
    _containView.backgroundColor = [UIColor clearColor];
}

- (IBAction)onChanged:(id)sender {
    BOOL enabled = [[_accountTextfield text] length] &&
    [[_nicknameTextfield text] length] &&
    [[_passwordTextfield text] length];
    self.registerBtn.enabled = enabled;
}

- (void)onRegister:(UIButton *)sender
{
    sender.selected = !sender.selected;
    NTESRegisterData *data = [[NTESRegisterData alloc] init];
    data.userId   = [_accountTextfield text];
    data.nickname = [_nicknameTextfield text];
    data.token    = [[_passwordTextfield text] tokenByPassword];
    if (![self check]) {
        return;
    }
    [SVProgressHUD show];
    __weak typeof(self) weakSelf = self;
    
    [[NTESService sharedService].loginService registerUser:data
                                                completion:^(NSError *error) {
                                           [SVProgressHUD dismiss];
                                           if (error == nil) {
                                               [weakSelf.navigationController.view makeToast:@"注册成功"
                                                                                    duration:2
                                                                                    position:CSToastPositionCenter];
                                               if ([weakSelf.delegate respondsToSelector:@selector(registDidComplete:password:)]) {
                                                   [weakSelf.delegate registDidComplete:data.userId password:[_passwordTextfield text]];
                                               }
                                               [weakSelf.navigationController popViewControllerAnimated:YES];
                                           }
                                           else
                                           {
                                               if ([weakSelf.delegate respondsToSelector:@selector(registDidComplete:password:)]) {
                                                   [weakSelf.delegate registDidComplete:nil password:nil];
                                               }
                                               
                                               NSString *toast = [NSString stringWithFormat:@"注册失败 code : %zd",error.code];
                                               [weakSelf.view makeToast:toast
                                                               duration:2
                                                               position:CSToastPositionCenter];
                                               
                                           }
                                       }];
}


- (IBAction)exist:(id)sender{
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)keyboardWillChangeFrame:(NSNotification *)notification{
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
    CGFloat bottomSpacing = -5.f;
    UIView *inputView = self.passwordTextfield.superview;
    if (inputView.bottom + bottomSpacing > CGRectGetMinY(keyboardFrame)) {
        CGFloat delta;
        if ([UIScreen mainScreen].bounds.size.height >= 568) {
            delta = self.existedButton.bottom + bottomSpacing - CGRectGetMinY(keyboardFrame);
            self.existedButton.bottom -= delta;
        }else{
            delta = inputView.bottom + bottomSpacing - CGRectGetMinY(keyboardFrame);
        }
        inputView.bottom -= delta;
    }
    if (self.logo.bottom > self.navigationController.navigationBar.bottom) {
        self.logo.bottom = self.navigationController.navigationBar.bottom;
        self.logo.alpha  = 0;
        self.navigationItem.title = @"注册";
    }
    [UIView commitAnimations];
    
}

#pragma mark - UITextFieldDelegate
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string{
    if ([string isEqualToString:@"\n"]) {
        [self onRegister:nil];
        return NO;
    }
    return YES;
}

#pragma mark - Private
- (void)resetTextField:(UITextField *)textField{
    textField.tintColor = [UIColor whiteColor];
    [textField setValue:UIColorFromRGBA(0xffffff, .6f) forKeyPath:@"_placeholderLabel.textColor"];
    textField.tintColor = [UIColor whiteColor];
    UIButton *clearButton = [textField valueForKey:@"_clearButton"];
    [clearButton setImage:[UIImage imageNamed:@"login_icon_clear"] forState:UIControlStateNormal];
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    [super touchesBegan:touches withEvent:event];
    [_accountTextfield resignFirstResponder];
    [_nicknameTextfield resignFirstResponder];
    [_passwordTextfield resignFirstResponder];
}


- (BOOL)check{
    if (!self.checkAccount) {
        [self.view makeToast:@"账号长度有误"
                    duration:2
                    position:CSToastPositionCenter];
        
        return NO;
    }
    if (!self.checkPassword) {
        [self.view makeToast:@"密码长度有误"
                    duration:2
                    position:CSToastPositionCenter];
        
        return NO;
    }
    if (!self.checkNickname) {
        [self.view makeToast:@"昵称长度有误"
                    duration:2
                    position:CSToastPositionCenter];
        
        return NO;
    }
    return YES;
}

- (BOOL)checkAccount{
    NSString *account = [_accountTextfield text];
    return account.length > 0 && account.length <= 20;
}

- (BOOL)checkPassword{
    NSString *checkPassword = [_passwordTextfield text];
    return checkPassword.length >= 6 && checkPassword.length <= 20;
}

- (BOOL)checkNickname{
    NSString *nickname= [_nicknameTextfield text];
    return nickname.length > 0 && nickname.length <= 10;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskPortrait;
}

- (UIButton *)registerBtn {
    if (!_registerBtn) {
        _registerBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            [btn setBackgroundImage:[UIImage imageNamed:@"hallway_enter_normal"] forState:UIControlStateNormal];
            [btn setBackgroundImage:[UIImage imageNamed:@"hallway_enter_disabled"] forState:UIControlStateDisabled];
            [btn setBackgroundImage:[UIImage imageNamed:@"hallway_enter_pressed"] forState:UIControlStateSelected];
            [btn setTitle:@"注册" forState:UIControlStateNormal];
            [btn setTitleColor:UIColorFromRGB(0x5294ed) forState:UIControlStateNormal];
            btn.enabled = NO;
            [btn addTarget:self action:@selector(onRegister:) forControlEvents:UIControlEventTouchUpInside];
            btn;
        });
    }
    return _registerBtn;
}

@end
