package com.wzy.core.http;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpServerHandler {

    public void hander(HttpServletRequest request, HttpServletResponse response){

        try {
            System.out.println("handle request ..");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
