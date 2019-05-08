package com.wzy.run;

import com.wzy.core.http.HttpServer;

public class AppRun {

    public static void main(String[] args) {
        new HttpServer().start("localhost", 80);
    }
}
