package com.sofm.recommend.infrastructure.adapter;

public interface MessageAdapter {

    void onMessage(String topic, String key, String message);
}
