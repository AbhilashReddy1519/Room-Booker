package com.roombooker.exception;


public class ConflictException extends RuntimeException {
  private String code;
  public ConflictException(String message, String code) {
    super(message);
    setCode(code);
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
}
