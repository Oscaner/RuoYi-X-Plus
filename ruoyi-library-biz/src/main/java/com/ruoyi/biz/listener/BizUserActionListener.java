package com.ruoyi.biz.listener;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.ruoyi.biz.domain.bo.BizUserOnlineBo;
import com.ruoyi.biz.domain.model.BizLoginUser;
import com.ruoyi.biz.helper.BizLoginHelper;
import com.ruoyi.common.constant.CacheNames;
import com.ruoyi.common.enums.UserType;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.redis.CacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 业务用户行为 侦听器的实现
 *
 * @author weibocy
 */
@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnProperty(prefix = "ruoyi", name = "appType", havingValue = "biz")
public class BizUserActionListener implements SaTokenListener {

    private final SaTokenConfig tokenConfig;

    /**
     * 每次登录时触发
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {
        UserType userType = UserType.getUserType(loginId.toString());

        UserAgent userAgent = UserAgentUtil.parse(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = ServletUtils.getClientIP();
        BizLoginUser user = BizLoginHelper.getLoginUser();
        BizUserOnlineBo dto = new BizUserOnlineBo();
        dto.setIpaddr(ip);
        dto.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        dto.setBrowser(userAgent.getBrowser().getName());
        dto.setOs(userAgent.getOs().getName());
        dto.setLoginTime(System.currentTimeMillis());
        dto.setTokenId(tokenValue);
        dto.setUserName(user.getUsername());
        String cacheNames = CacheNames.ONLINE_BIZ_TOKEN;
        if (tokenConfig.getTimeout() > 0) {
            // 增加 ttl 过期时间 单位秒
            cacheNames = CacheNames.ONLINE_BIZ_TOKEN + "#" + tokenConfig.getTimeout() + "s";
        }
        CacheUtils.put(cacheNames, tokenValue, dto);
        log.info("user doLogin, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次注销时触发
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        CacheUtils.evict(CacheNames.ONLINE_BIZ_TOKEN, tokenValue);
        log.info("user doLogout, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被踢下线时触发
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        CacheUtils.evict(CacheNames.ONLINE_BIZ_TOKEN, tokenValue);
        log.info("user doLogoutByLoginId, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被顶下线时触发
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        CacheUtils.evict(CacheNames.ONLINE_BIZ_TOKEN, tokenValue);
        log.info("user doReplaced, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被封禁时触发
     */
    @Override
    public void doDisable(String loginType, Object loginId, long disableTime) {
    }

    /**
     * 每次被解封时触发
     */
    @Override
    public void doUntieDisable(String loginType, Object loginId) {
    }

    /**
     * 每次创建Session时触发
     */
    @Override
    public void doCreateSession(String id) {
    }

    /**
     * 每次注销Session时触发
     */
    @Override
    public void doLogoutSession(String id) {
    }


}
