package com.github.gdenga;


import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author: gdenga
 * @date: 2019/8/1 23:28
 * @content:
 */
public class Test {
    public static String result;

    public static void main(String[] args){

        Calculator calculator = new Calculator();
        String charset = "(\\d+[+\\-*])+(\\d+)";

        Date start = new Date();
        DefaultHttpClient httpClient = new DefaultHttpClient(HttpClientTestDemo.cm, HttpClientTestDemo.parentParams);
        httpClient.setHttpRequestRetryHandler(HttpClientTestDemo.httpRequestRetryHandler);
        try {

            result = HttpClientTestDemo.post(httpClient, "http://123.206.87.240:8002/qiumingshan/",new ArrayList<>());

            String[] s1 = result.split(charset);
            calculator.caculate(result.replaceAll(s1[0], "").replace(s1[1], ""));
            System.out.println("算式："+result.replaceAll(s1[0], "").replace(s1[1], ""));
            System.out.println("结果："+calculator.getResult());


            Object [] params = new Object[]{"value"};
            Object [] value = new Object[]{calculator.getResult()};
            result = HttpClientTestDemo.post(httpClient, "http://123.206.87.240:8002/qiumingshan/",HttpClientTestDemo.getParams(params,value));
            System.out.println(result);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        Date end = new Date();
        System.out.println("用时："+(end.getTime() - start.getTime()) / 1000.0 + " 秒");
    }
}
