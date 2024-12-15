package com.kangjunjie.netty.websocket.starter.support;

import com.kangjunjie.netty.websocket.starter.annotations.OnEvent;
import com.kangjunjie.netty.websocket.starter.netty.AttributeKeyConstant;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

import java.util.Objects;

/**
 * @author KangJunjie
 */
public class IdleEventMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnEvent.class) && Objects.equals(parameter.getParameterType(),Object.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel) {
        return channel.attr(AttributeKeyConstant.idleStateEvent).get();
    }
}
