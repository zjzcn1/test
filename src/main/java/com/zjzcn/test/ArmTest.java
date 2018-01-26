package com.zjzcn.test;

import java.io.IOException;

/**
 * Created by zjz on 18/1/5.
 *
 通讯接口：串口，115200
 通讯协议：0XFE 0XFE 长度 功能码 参数1...参数N 校验和
 0XFE 0XFE 两个0XFE表示数据包的开头
 长度 代表长度字节时候的字节数，一般为参数个数+2
 功能码 代表需要执行的功能
 参数 根据功能码不同，参数也会有所不同
 校验和 校验和是整个数据包的错误校验机制，为除了开头两个 0xFE 以外所有字节的和	   再取逆
 目前功能码暂时提供的有写数据（0x03）,机械臂回零（0x0a）
 */

public class ArmTest {

    private static final String TAG = "ArmControl";

    /**
     * 手臂移动指令, (byte)0xFE, (byte)0xFE, (byte)0x09, (byte)0x03, (byte)0x01, (byte)0x00,
     (byte)0x00, (byte)0x13, (byte)0x71, (byte)0x06, (byte)0x21, (byte)0x47
     * @param x
     * @param y
     * @param z
     */
    public void moveTo(int x, int y, int z) throws IOException {
        byte[] cmd = new byte[]{(byte)0xFE, (byte)0xFE, (byte)0x09, (byte)0x03, (byte)0x01, (byte)(x>>8),
                (byte)x, (byte)(y>>8), (byte)y, (byte)(z>>8), (byte)z, (byte)0x00};

        int check = 0;
        for(int i = 2; i < cmd.length-1; i++) {
            check = check + cmd[i]&0xFF;
        }
        check = ~check;

        cmd[cmd.length-1] = (byte)check;
    }

    /**
     * 手臂复位指令： FE FE 02 0A F3
     * @throws IOException
     */
    public void reset() throws IOException {
        byte[] cmd = new byte[]{(byte)0xFE, (byte)0xFE, (byte)0x02, (byte)0x0A, (byte)0xF3};
    }

    public static void main(String[] args) throws IOException {
        ArmTest control = new ArmTest();
        control.moveTo(2327, 3114, -5136);
    }
}
