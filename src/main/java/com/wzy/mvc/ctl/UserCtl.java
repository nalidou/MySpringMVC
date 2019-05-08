package com.wzy.mvc.ctl;

import com.wzy.core.annotation.Autowired;
import com.wzy.core.annotation.Controller;
import com.wzy.core.annotation.RequertMapping;
import com.wzy.mvc.ser.UserSer;

@Controller
@RequertMapping("/user")
public class UserCtl {

    @Autowired("userSer")
    private UserSer userSer;

    @RequertMapping("/getUser")
    public String getUser() {

        System.out.println("----> getUser " + userSer.getUser());
        return "getUser...";
    }
}
