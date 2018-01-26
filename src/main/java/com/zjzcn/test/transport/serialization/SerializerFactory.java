package com.zjzcn.test.transport.serialization;

public class SerializerFactory {

	public static Serializer create() {
		return new StringSerializer();
	}
}
