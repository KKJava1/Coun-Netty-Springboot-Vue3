package com.kangjunjie.netty.websocket.starter.support;

import com.kangjunjie.netty.websocket.starter.WebsocketProperties;
import com.kangjunjie.netty.websocket.starter.annotations.WsServerEndpoint;
import com.kangjunjie.netty.websocket.starter.netty.NettyWebsocketServer;
import com.kangjunjie.netty.websocket.starter.netty.WebsocketActionDispatch;
import lombok.SneakyThrows;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;


/**
 * @author KangJunjie
 */
//集成了Spring的后置处理器SmartInitializingSingleton
public class WebSocketAnnotationPostProcessor implements SmartInitializingSingleton {
    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private WebsocketProperties websocketProperties;

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNamesForAnnotation = beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
        String applicationStartBean = beanNamesForAnnotation[0];
        Object bean = beanFactory.getBean(applicationStartBean);
        String basePackage = ClassUtils.getPackageName(bean.getClass());
        scanWebsocketServiceBeans(basePackage,beanFactory);
        registerServerEndpoints();
    }

    @SneakyThrows
    private void registerServerEndpoints() {
        /**
         * 开发者只需要标注注解即可，无需手动配置端点路径和方法，简化了开发流程。
         * 动态发现的机制也方便后续扩展，如果新增了端点，只需添加新的类，不需要修改框架代码。
         */
        String[] beanNamesForAnnotation = beanFactory.getBeanNamesForAnnotation(WsServerEndpoint.class);

        WebsocketActionDispatch actionDispatch = new WebsocketActionDispatch();
        for (String beanName : beanNamesForAnnotation) {
            //获取类的类型和目标类
            Class<?> beanType = beanFactory.getType(beanName);
            Class<?> targetClass = getTargetClass(beanType);
            //提取 @WsServerEndpoint 注解信息
            WsServerEndpoint wsServerEndpoint = targetClass.getAnnotation(WsServerEndpoint.class);
            /**
             * 创建 WebsocketServerEndpoint 对象，包含服务端点的：
             * 目标类（pojoClazz）：targetClass
             * 类实例（object）：从 Spring 容器中获取
             * 路径（path）：从注解中提取的路径
             */
            WebsocketServerEndpoint websocketServerEndpoint = new WebsocketServerEndpoint(targetClass
                    ,beanFactory.getBean(targetClass),wsServerEndpoint.value());
            actionDispatch.addWebsocketServerEndpoint(websocketServerEndpoint);
        }
        NettyWebsocketServer websocketServer = new NettyWebsocketServer(actionDispatch,websocketProperties);
        // 启动websocket
        websocketServer.start();
    }


    /**
     * 扫描WsServerEndpoint的Bean
     * @param packagesToScan 扫描包路径
     * @param registry
     */
    private void scanWebsocketServiceBeans(String packagesToScan, BeanDefinitionRegistry registry) {

        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        // 扫描 @WsServerEndpoint标注的类
        scanner.addIncludeFilter(new AnnotationTypeFilter(WsServerEndpoint.class));
        scanner.scan(packagesToScan);
    }

    /**
     * 获取类型的目标类型
     * @param clazz
     * @return
     */
    public Class<?> getTargetClass(Class<?> clazz) {
        if (AopUtils.isCglibProxy(clazz)) {
            return clazz.getSuperclass();
        }
        return clazz;
    }
}
