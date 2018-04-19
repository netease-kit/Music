package com.netease.mmc.demo.service;

import java.util.Objects;
import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.netease.mmc.demo.common.enums.HttpCodeEnum;
import com.netease.mmc.demo.common.enums.UserTypeEnum;
import com.netease.mmc.demo.common.util.CheckSumBuilder;
import com.netease.mmc.demo.common.util.RandomUtil;
import com.netease.mmc.demo.dao.StudentDao;
import com.netease.mmc.demo.dao.TeacherDao;
import com.netease.mmc.demo.dao.domain.StudentDO;
import com.netease.mmc.demo.dao.domain.TeacherDO;
import com.netease.mmc.demo.httpdao.nim.NIMServerApiHttpDao;
import com.netease.mmc.demo.httpdao.nim.dto.NIMUserDTO;
import com.netease.mmc.demo.httpdao.nim.dto.NimUserResponseDTO;
import com.netease.mmc.demo.httpdao.nim.util.NIMErrorCode;
import com.netease.mmc.demo.service.model.BizResultModel;
import com.netease.mmc.demo.service.model.UserCheckModel;
import com.netease.mmc.demo.service.model.UserModel;
import com.netease.mmc.demo.service.util.ModelUtil;

/**
 * 用户相关业务.
 *
 * @author hzwanglin1
 * @date 2018/4/1
 * @since 1.0
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * 老师accid前缀
     */
    private static final String TEACHER_ACCID_PREFIX = "t";

    /**
     * 老师昵称前缀
     */
    private static final String TEACHER_NICKNAME_PREFIX = "老师";

    /**
     * 老师密码长度
     */
    private static final int TEACHER_PASSWORD_LENGTH = 4;

    @Resource
    private NIMServerApiHttpDao nimServerApiHttpDao;
    @Resource
    private StudentDao studentDao;
    @Resource
    private TeacherDao teacherDao;
    @Resource
    private SeqService seqService;

    @Value("${user.reg.limit}")
    private int userRegLimit;

    /**
     * <p>注册学生账号.</p>
     *
     * @param accid 用户名
     * @param nickname 用户昵称
     * @param password 注册密码
     * @return
     */
    public BizResultModel<StudentDO> registerUser(String accid, String nickname, String password) {
        BizResultModel<NIMUserDTO> resultModel = createNimUser(accid, nickname, password);
        if (resultModel.isSuccess()) {
            StudentDO studentDO = new StudentDO();

            NIMUserDTO userDTO = resultModel.getData();
            studentDO.setAccid(userDTO.getAccid());
            studentDO.setNickname(userDTO.getName());
            studentDO.setImToken(userDTO.getToken());
            studentDO.setPassword(password);

            // 数据库新增user
            studentDao.insertSelective(studentDO);

            return new BizResultModel<>(studentDO);
        } else {
            return new BizResultModel<>(resultModel.getCode(), resultModel.getMessage());
        }
    }

    /**
     * 分配老师
     *
     * 业务场景是为学生预定的课程分配老师;
     * 这里为了简化业务逻辑，每次分配时都创建一个新的老师账号
     *
     * @return
     */
    public BizResultModel<TeacherDO> allocateTeacher() {
        String password = RandomUtil.randomPassword(TEACHER_PASSWORD_LENGTH);
        String defaultToken = CheckSumBuilder.getMD5(password);
        BizResultModel<NIMUserDTO> resultModel =
                createNimUser(produceTeacherAccid(), produceTeacherNickname(), defaultToken);
        if (resultModel.isSuccess()) {
            TeacherDO teacherDO = new TeacherDO();

            NIMUserDTO userDTO = resultModel.getData();
            teacherDO.setAccid(userDTO.getAccid());
            teacherDO.setNickname(userDTO.getName());
            teacherDO.setImToken(userDTO.getToken());
            teacherDO.setPassword(password);

            // 数据库新增user
            teacherDao.insertSelective(teacherDO);

            return new BizResultModel<>(teacherDO);
        } else {
            return new BizResultModel<>(resultModel.getCode(), resultModel.getMessage());
        }
    }

    /**
     * 同一个ip每天的账号注册上限
     *
     * @return
     */
    public int getUserRegLimit() {
        return userRegLimit;
    }

    /**
     * 判断学生账号是否存在
     *
     * @param accid
     * @return
     */
    public boolean existsUser(String accid) {
        return studentDao.existsStudent(accid);
    }

    /**
     * 查询学生账号信息
     *
     * @param accid
     * @return
     */
    public UserModel queryStudentByAccid(String accid) {
        return ModelUtil.INSTANCE.studentDo2UserModel(studentDao.findByAccid(accid));
    }

    /**
     * 查询老师账号信息
     *
     * @param accid
     * @return
     */
    public UserModel queryTeacherByAccid(String accid) {
        return ModelUtil.INSTANCE.studentDo2UserModel(teacherDao.findByAccid(accid));
    }

    /**
     * 创建IM账号.
     *
     * @param accid
     * @param nickname
     * @param imToken
     * @return
     */
    private BizResultModel<NIMUserDTO> createNimUser(String accid, String nickname, String imToken) {
        // 注册云信账号
        NimUserResponseDTO nimResDTO = nimServerApiHttpDao.createUser(accid, nickname, imToken);
        if (nimResDTO.isSuccess()) {
            return new BizResultModel<>(nimResDTO.getInfo());
        } else if (Objects.equals(nimResDTO.getCode(), NIMErrorCode.ILLEGAL_PARAM.value())) {
            // 注册时api接口返回414，可以认为是账号已在云信服务器注册
            return new BizResultModel<>(HttpCodeEnum.USER_ALREADY_EXISTS.value(),
                    HttpCodeEnum.USER_ALREADY_EXISTS.getReasonPhrase());
        } else {
            logger.error("createStudent.createIMUser failed for accid[{}] cause[{}]", accid, nimResDTO);
            return new BizResultModel<>(HttpCodeEnum.USER_ERROR.value(), nimResDTO.getDesc());
        }
    }

    /**
     * 构造需要创建的老师账号，不重复
     *
     * @return
     */
    private String produceTeacherAccid() {
        // 递增序列号 + 1位随机数
        return TEACHER_ACCID_PREFIX + seqService.getSeqId() + new Random().nextInt(10);
    }

    /**
     * 构造需要创建的老师昵称
     *
     * @return
     */
    private String produceTeacherNickname() {
        return TEACHER_NICKNAME_PREFIX + String.valueOf(100000 + new Random().nextInt(899999));
    }

    /**
     * 校验用户账号
     *
     * @param accid
     * @return
     */
    public BizResultModel<UserCheckModel> checkUser(String accid) {
        StudentDO studentDO = studentDao.findByAccid(accid);
        if (studentDO != null) {
            return new BizResultModel<>(
                    new UserCheckModel(studentDO.getAccid(), UserTypeEnum.STUDENT.getValue()));
        }
        TeacherDO teacherDO = teacherDao.findByAccid(accid);
        if (teacherDO != null) {
            return new BizResultModel<>(
                    new UserCheckModel(teacherDO.getAccid(), UserTypeEnum.TEACHER.getValue()));
        }
        return new BizResultModel<>(HttpCodeEnum.USER_NOT_FOUND.value(), HttpCodeEnum.USER_NOT_FOUND.getReasonPhrase());
    }
}