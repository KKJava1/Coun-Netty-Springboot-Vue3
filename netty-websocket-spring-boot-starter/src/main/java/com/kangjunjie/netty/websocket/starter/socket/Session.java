package com.kangjunjie.netty.websocket.starter.socket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author KangJunjie
 */
public class Session {
    private final Channel channel;

    public Session(Channel channel) {
        this.channel = channel;
    }

    public void sendText(String text) {
        channel.writeAndFlush(new TextWebSocketFrame(text));
    }

    public void close() {
        channel.close();
    }

    public String getId() {
        return channel.id().asShortText();
    }
}
