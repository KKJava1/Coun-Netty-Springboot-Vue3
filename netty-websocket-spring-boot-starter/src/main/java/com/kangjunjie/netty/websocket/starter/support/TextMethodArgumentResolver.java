package com.kangjunjie.netty.websocket.starter.support;

import com.kangjunjie.netty.websocket.starter.annotations.OnMessage;
import com.kangjunjie.netty.websocket.starter.netty.AttributeKeyConstant;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.core.MethodParameter;

import java.util.Objects;

/**
 * @author KangJunjie
 */
public class TextMethodArgumentResolver implements MethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnMessage.class)
                && Objects.equals(parameter.getParameterType(),String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel) {
        TextWebSocketFrame text = channel.attr(AttributeKeyConstant.textWebSocketFrame).get();
        return text.text();
    }
}
