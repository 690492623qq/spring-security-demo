package com.mkyong.common.controller;
 
import com.mkyong.entity.AppToken;
import com.mkyong.util.AccessTokenConverter;
import com.mkyong.util.DefaultTokenConverter;
import com.mkyong.util.HttpClientUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller

public class HelloController {


	@RequestMapping("/welcome")
	public String printWelcome(ModelMap model) {

        System.out.println("welcome--"+SecurityContextHolder.getContext().getAuthentication());
		model.addAttribute("message", "Spring Security Hello World");
		return "hello";
 
	}


    @RequestMapping("auth")
    public @ResponseBody String oauth(String code){

        System.out.println("auth--"+SecurityContextHolder.getContext().getAuthentication());

        if(null == code || "".equals(code)){
            return "this is code--"+code ;
        }

        String accessTokenUrl = "http://localhost:8080/SpringMVC3/oauth/token";
        List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
        nameValuePairList.add(new BasicNameValuePair("client_id", "external"));
        nameValuePairList.add(new BasicNameValuePair("client_secret", "externalsecret"));
        nameValuePairList.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nameValuePairList.add(new BasicNameValuePair("code", code));
        nameValuePairList.add(new BasicNameValuePair("redirect_uri", "http://localhost:8080/SpringMVC3/auth"));
        String tokenResult = HttpClientUtil.post(accessTokenUrl, nameValuePairList);

        return tokenResult ;
    }


    private AccessTokenConverter tokenConverter = new DefaultTokenConverter();

    @Autowired
    private DefaultTokenServices tokenServices;

    @RequestMapping(value = "/myToken/{token}",method= RequestMethod.GET)
    @ResponseBody
    public AppToken checkToken(@PathVariable String token) {
        OAuth2AccessToken accessToken = tokenServices.readAccessToken(token);
        if (accessToken == null) {
            throw new AccessDeniedException("Token was not recognised");
        }

        if (accessToken.isExpired()) {
            throw new AccessDeniedException("Token has expired");
        }

        OAuth2Authentication authentication = tokenServices.loadAuthentication(token);
        AppToken appToken = tokenConverter.convertAccessToken(accessToken, authentication);
        return appToken;
    }
 
}