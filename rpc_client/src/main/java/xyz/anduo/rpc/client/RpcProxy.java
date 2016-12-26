package xyz.anduo.rpc.client;

import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;
import org.springframework.util.StringUtils;
import xyz.anduo.rpc.common.CqsLogger;

public class RpcProxy implements CqsLogger {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass) {
        logger.debug(interfaceClass.getClassLoader() + "\t" + interfaceClass);
        System.out.println("代理新建对象:" + interfaceClass.getClassLoader() + "\t" + new Class<?>[]{interfaceClass});
        T instance = (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), interfaceClass.getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("init invoke create method...");
                        RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        logger.debug("request:"+request.toString());

                        if (serviceDiscovery != null) {
                            serverAddress = serviceDiscovery.discover(); // 发现服务
                        }
                        if (StringUtils.isEmpty(serverAddress)) serverAddress = serviceDiscovery.getRegistryAddress();
                        String[] array = serverAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);

                        RpcClient client = new RpcClient(host, port); // 初始化 RPC 客户端
                        RpcResponse response = client.send(request); // 通过 RPC客户端发送RPC请求并获取RPC响应
                        if (response.isError()) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    }
                });
        logger.debug("代理新建对象instance:\t" + instance.toString());
        return instance;
    }
}
