package com.mkyong.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User: jianyuanyang
 * Date: 13-9-18
 * Time: 下午2:18
 */
@Controller
public class MySecurityController {

    @RequestMapping("/test")
    public @ResponseBody String test(){
      return "this is test";
    }
}
