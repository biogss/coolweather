package com.coolweather.util;

public interface HttpCallbackListener {
	void onFinish(String respone);
	void onError(Exception e);

}
