package com.ccd.exception;

import java.util.Map;

public class InvalidFieldException extends Exception {

	private final Map<String, String> fieldErrors;

	public InvalidFieldException(Map<String, String> fieldErrors) {
		super();
		this.fieldErrors = fieldErrors;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

}
