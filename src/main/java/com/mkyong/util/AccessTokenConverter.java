package com.mkyong.util;

import com.mkyong.entity.AppToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * User: jianyuanyang
 * Date: 13-9-22
 * Time: 下午2:15
 */
public interface AccessTokenConverter {

    public abstract AppToken convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication);

}
