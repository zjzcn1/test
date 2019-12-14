package com.zjzcn.test;

import com.sun.jna.Library;
import com.sun.jna.Native;


public class JnaTest {


    public interface CLibrary extends Library {
        CLibrary instance = Native.load("/Users/zjz/work/java/test/target/native/libtest.dylib", CLibrary.class);

        int say(String text);
    }

    public static void main(String[] args) {
        int sum = CLibrary.instance.say("hehe");
        System.out.println("say() = " + sum);
    }

}