package com.wzy.mvc.ser;

import com.wzy.core.annotation.Serivce;

@Serivce("userSer")
public class UserSer {

    public String getUser() {
        return "call getUser...";
    }
}
