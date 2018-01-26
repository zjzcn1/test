package com.zjzcn.test.transport.serialization;

public interface Serializer<T> {

	byte[] serialize(T data);
	
	T deserialize(byte[] bytes, Class<T> clazz);
}
