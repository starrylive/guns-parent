package com.stylefeng.guns.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * jwt相关配置
 *
 * @author fengshuonan
 * @date 2017-08-23 9:23
 */
@Configuration
@ConfigurationProperties(prefix = JwtProperties.JWT_PREFIX)
public class JwtProperties {

    public static final String JWT_PREFIX = "jwt";

    /**
     * http请求头所需要的字段
     */
    private String header = "Authorization";

    /**
     * #jwt秘钥
     */
    private String secret = "defaultSecret";

    /**
     * #7天 单位:秒
     */
    private Long expiration = 604800L;

    /**
     * #认证请求的路径
     */
    private String authPath = "auth";

    /**
     * #md5加密混淆key
     */
    private String md5Key = "randomKey";

    /**
     * 忽略的url列表
     */
    private String ignoreUrl = "";

    public String getIgnoreUrl() {
        return ignoreUrl;
    }

    public void setIgnoreUrl(String ignoreUrl) {
        this.ignoreUrl = ignoreUrl;
    }

    public static String getJwtPrefix() {
        return JWT_PREFIX;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getAuthPath() {
        return authPath;
    }

    public void setAuthPath(String authPath) {
        this.authPath = authPath;
    }

    public String getMd5Key() {
        return md5Key;
    }

    public void setMd5Key(String md5Key) {
        this.md5Key = md5Key;
    }
}
