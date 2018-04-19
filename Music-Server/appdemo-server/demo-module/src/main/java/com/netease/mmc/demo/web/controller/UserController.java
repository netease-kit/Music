package com.netease.mmc.demo.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netease.mmc.demo.common.constant.RedisKeys;
import com.netease.mmc.demo.common.context.WebContextHolder;
import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.util.DataPack;
import com.netease.mmc.demo.common.util.RedissonUtil;
import com.netease.mmc.demo.dao.domain.StudentDO;
import com.netease.mmc.demo.service.UserService;
import com.netease.mmc.demo.service.model.BizResultModel;
import com.netease.mmc.demo.service.model.UserCheckModel;
import com.netease.mmc.demo.web.util.ParamCheckUtil;
import com.netease.mmc.demo.web.util.VOUtil;

/**
 * 用户相关Controller.
 *
 * @author hzwanglin1
 * @date 2018/4/1
 * @since 1.0
 */
@Controller
@RequestMapping("music/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource()
    @Qualifier(value = "whiteIpList")
    private List<String> whiteIpList;

    @Resource
    private UserService userService;

    /**
     * 校验用户账号
     *
     * @param accid
     * @return
     */
    @RequestMapping(value = "check", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap checkUser(@RequestParam(value = "accid") String accid) {
        BizResultModel<UserCheckModel> resultModel = userService.checkUser(accid);
        if (resultModel.isSuccess()) {
            return DataPack.packOk(resultModel.getData());
        } else {
            return DataPack.packFailure(resultModel.getCode(), resultModel.getMessage());
        }
    }

    /**
     * 学生注册.
     *
     * @param accid 注册账号
     * @param nickname 昵称
     * @param password 密码（md5加密后的密文）
     * @return
     */
    @RequestMapping(value = "reg", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ModelMap register(@RequestParam("accid") String accid, @RequestParam("nickname") String nickname,
            @RequestParam("password") String password) {
        // 注册参数校验
        if (!ParamCheckUtil.validateAccid(accid)) {
            return DataPack.packFailure(HttpCodeEnum.ACCID_INVALID);
        }
        if (!ParamCheckUtil.validateNickname(nickname)) {
            return DataPack.packFailure(HttpCodeEnum.NICKNAME_INVALID);
        }
        if (!ParamCheckUtil.validatePassword(password)) {
            return DataPack.packFailure(HttpCodeEnum.PASSWORD_INVALID);
        }

        // 将accid统一小写处理
        String lowerCaseAccid = StringUtils.lowerCase(accid);

        if (isUserRegIPCountLimited(WebContextHolder.getIp())) {
            return DataPack.packFailure(HttpCodeEnum.USER_REG_FREQUENTLY);
        }

        if (userService.existsUser(lowerCaseAccid)) {
            return DataPack.packFailure(HttpCodeEnum.USER_ALREADY_EXISTS);
        }

        BizResultModel<StudentDO> resultModel =
                userService.registerUser(lowerCaseAccid, nickname, password);
        if (resultModel.isSuccess()) {
            return DataPack.packOk(VOUtil.INSTANCE.studentDO2VO(resultModel.getData()));
        } else {
            return DataPack.packFailure(HttpCodeEnum.USER_ERROR);
        }
    }

    /**
     * 检查此IP注册用户数是否达到每日上限.
     *
     * @param ip
     * @return
     */
    private boolean isUserRegIPCountLimited(String ip) {
        boolean result;
        // IP白名单直接返回
        if (CollectionUtils.isNotEmpty(whiteIpList) && whiteIpList.contains(ip)) {
            return false;
        }
        long ipCount = RedissonUtil.getAtomicLong(String.format(RedisKeys.USER_REG_IP_COUNT_TODAY, ip));
        result = ipCount >= userService.getUserRegLimit();
        if (result) {
            logger.warn("user reg ip limited today, ip[{}]", ip);
        }
        return result;
    }



}