package com.ruoyi.system.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.admin.domain.model.AdminLoginUser;
import com.ruoyi.admin.helper.AdminLoginHelper;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.bo.RoleBo;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.service.LogininforService;
import com.ruoyi.common.enums.DeviceType;
import com.ruoyi.common.enums.LoginType;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.exception.user.UserException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * 登录校验方法
 *
 * @author Lion Li
 */
@Slf4j
@Service
public class SysLoginService {

    @Resource
    private ISysUserService userService;

    @Resource
    private ISysConfigService configService;

    /**
     * 后台用户登录日志服务
     * 前后台用户分开，暂时用Qualifier区分方案
     */
    @Resource
    @Qualifier("sysLogininforServiceImpl")
    private LogininforService asyncService;

    @Resource
    private SysPermissionService permissionService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        HttpServletRequest request = ServletUtils.getRequest();
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        // 验证码开关
        if (captchaOnOff) {
            validateCaptcha(username, code, uuid, request);
        }
        SysUser user = loadUserByUsername(username);
        checkLogin(LoginType.PASSWORD, username, () -> !BCrypt.checkpw(password, user.getPassword()));
        // 此处可根据登录用户的数据不同 自行创建 loginUser
        AdminLoginUser loginUser = buildLoginUser(user);
        // 生成token
        AdminLoginHelper.loginByDevice(loginUser, DeviceType.PC);

        asyncService.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"), request);
        recordLoginInfo(user.getUserId(), username);
        return StpUtil.getTokenValue();
    }

    public String smsLogin(String phonenumber, String smsCode) {
        // 通过手机号查找用户
        SysUser user = loadUserByPhonenumber(phonenumber);

        HttpServletRequest request = ServletUtils.getRequest();
        checkLogin(LoginType.SMS, user.getUserName(), () -> !validateSmsCode(phonenumber, smsCode, request));
        // 此处可根据登录用户的数据不同 自行创建 loginUser
        AdminLoginUser loginUser = buildLoginUser(user);
        // 生成token
        AdminLoginHelper.loginByDevice(loginUser, DeviceType.APP);

        asyncService.recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"), request);
        recordLoginInfo(user.getUserId(), user.getUserName());
        return StpUtil.getTokenValue();
    }


    public String xcxLogin(String xcxCode) {
        HttpServletRequest request = ServletUtils.getRequest();
        // xcxCode 为 小程序调用 wx.login 授权后获取
        // todo 以下自行实现
        // 校验 appid + appsrcret + xcxCode 调用登录凭证校验接口 获取 session_key 与 openid
        String openid = "";
        SysUser user = loadUserByOpenid(openid);

        // 此处可根据登录用户的数据不同 自行创建 loginUser
        AdminLoginUser loginUser = new AdminLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());
//        loginUser.setOpenid(openid);
        // 生成token
        AdminLoginHelper.loginByDevice(loginUser, DeviceType.XCX);

        asyncService.recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"), request);
        recordLoginInfo(user.getUserId(), user.getUserName());
        return StpUtil.getTokenValue();
    }


    public void logout(String loginName) {
        asyncService.recordLogininfor(loginName, Constants.LOGOUT, MessageUtils.message("user.logout.success"), ServletUtils.getRequest());
    }

    /**
     * 校验短信验证码
     */
    private boolean validateSmsCode(String phonenumber, String smsCode, HttpServletRequest request) {
        String code = RedisUtils.getCacheObject(Constants.CAPTCHA_CODE_KEY + phonenumber);
        if (StringUtils.isBlank(code)) {
            asyncService.recordLogininfor(phonenumber, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"), request);
            throw new CaptchaExpireException();
        }
        return code.equals(smsCode);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid, HttpServletRequest request) {
        String verifyKey = Constants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            asyncService.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"), request);
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            asyncService.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"), request);
            throw new CaptchaException();
        }
    }

    private SysUser loadUserByUsername(String username) {
        SysUser user = userService.selectUserByUserName(username);
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UserException("user.not.exists", username);
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new UserException("user.password.delete", username);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new UserException("user.blocked", username);
        }
        return user;
    }

    private SysUser loadUserByPhonenumber(String phoneNumber) {
        SysUser user = userService.selectUserByPhoneNumber(phoneNumber);
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", phoneNumber);
            throw new UserException("user.not.exists", phoneNumber);
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", phoneNumber);
            throw new UserException("user.password.delete", phoneNumber);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", phoneNumber);
            throw new UserException("user.blocked", phoneNumber);
        }
        return user;
    }

    private SysUser loadUserByOpenid(String openid) {
        // 使用 openid 查询绑定用户 如未绑定用户 则根据业务自行处理 例如 创建默认用户
        // todo 自行实现 userService.selectUserByOpenid(openid);
        SysUser user = new SysUser();
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", openid);
            // todo 用户不存在 业务逻辑自行实现
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", openid);
            // todo 用户已被删除 业务逻辑自行实现
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", openid);
            // todo 用户已被停用 业务逻辑自行实现
        }
        return user;
    }

    /**
     * 构建登录用户
     */
    private AdminLoginUser buildLoginUser(SysUser user) {
        AdminLoginUser loginUser = new AdminLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());
        loginUser.setMenuPermission(permissionService.getMenuPermission(user));
        loginUser.setRolePermission(permissionService.getRolePermission(user));
        loginUser.setDeptName(ObjectUtil.isNull(user.getDept()) ? "" : user.getDept().getDeptName());
        List<RoleBo> roles = BeanUtil.copyToList(user.getRoles(), RoleBo.class);
        loginUser.setRoles(roles);
        return loginUser;
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId, String username) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(ServletUtils.getClientIP());
        sysUser.setLoginDate(DateUtils.getNowDate());
        sysUser.setUpdateBy(username);
        userService.updateUserProfile(sysUser);
    }

    /**
     * 登录校验
     */
    private void checkLogin(LoginType loginType, String username, Supplier<Boolean> supplier) {
        HttpServletRequest request = ServletUtils.getRequest();
        String errorKey = Constants.LOGIN_ERROR + username;
        Integer errorLimitTime = Constants.LOGIN_ERROR_LIMIT_TIME;
        Integer setErrorNumber = Constants.LOGIN_ERROR_NUMBER;
        String loginFail = Constants.LOGIN_FAIL;

        // 获取用户登录错误次数(可自定义限制策略 例如: key + username + ip)
        Integer errorNumber = RedisUtils.getCacheObject(errorKey);
        // 锁定时间内登录 则踢出
        if (ObjectUtil.isNotNull(errorNumber) && errorNumber.equals(setErrorNumber)) {
            asyncService.recordLogininfor(username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), errorLimitTime), request);
            throw new UserException(loginType.getRetryLimitExceed(), errorLimitTime);
        }

        if (supplier.get()) {
            // 是否第一次
            errorNumber = ObjectUtil.isNull(errorNumber) ? 1 : errorNumber + 1;
            // 达到规定错误次数 则锁定登录
            if (errorNumber.equals(setErrorNumber)) {
                RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(errorLimitTime));
                asyncService.recordLogininfor(username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), errorLimitTime), request);
                throw new UserException(loginType.getRetryLimitExceed(), errorLimitTime);
            } else {
                // 未达到规定错误次数 则递增
                RedisUtils.setCacheObject(errorKey, errorNumber);
                asyncService.recordLogininfor(username, loginFail, MessageUtils.message(loginType.getRetryLimitCount(), errorNumber), request);
                throw new UserException(loginType.getRetryLimitCount(), errorNumber);
            }
        }

        // 登录成功 清空错误次数
        RedisUtils.deleteObject(errorKey);
    }
}
