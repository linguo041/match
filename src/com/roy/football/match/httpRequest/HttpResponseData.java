package com.roy.football.match.httpRequest;

public class HttpResponseData<T> {
	public String getErrorMmsg() {
		return errormsg;
	}

	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}

	public Boolean getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

    public String getErrormsg() {
        return errormsg;
    }
    private String errormsg;
    private Boolean isSuccess;

	private T data;
}
