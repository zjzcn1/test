package com.zjzcn.test.transport;

import java.util.HashMap;
import java.util.Map;

public class Request extends Message {

	private Map<String, Object> attachments = new HashMap<>();

	public Map<String, Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Object> attachments) {
		this.attachments = attachments;
	}

	public void addAttachment(String key, Object value) {
		this.attachments.put(key, value);
	}
}
