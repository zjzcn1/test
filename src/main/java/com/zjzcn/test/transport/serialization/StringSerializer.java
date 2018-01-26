package com.zjzcn.test.transport.serialization;

public class StringSerializer implements Serializer<String> {

	@Override
	public byte[] serialize(String data) {
		return data.getBytes();
	}

	@Override
	public String deserialize(byte[] bytes, Class<String> clazz) {
		return new String(bytes);
	}

}
