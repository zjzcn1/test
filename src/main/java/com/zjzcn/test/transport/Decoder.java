package com.zjzcn.test.transport;

public interface Decoder {

    Message decode(byte[] data);
}
