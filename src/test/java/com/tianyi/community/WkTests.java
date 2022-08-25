package com.tianyi.community;


import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "/usr/local/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com /Users/xutianyi/Desktop/test.png";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            Thread.sleep(10000);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
