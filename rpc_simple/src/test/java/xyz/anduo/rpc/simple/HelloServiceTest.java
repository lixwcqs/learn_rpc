package xyz.anduo.rpc.simple;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xyz.anduo.rpc.client.RpcProxy;
import xyz.anduo.rpc.sieve.modules.simple.HelloService;
import xyz.anduo.rpc.sieve.modules.simple.impl.HelloServiceImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-client.xml"})
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest() {
        try {
            System.out.println(rpcProxy);
            HelloService helloService = rpcProxy.create(HelloService.class);
            String result = helloService.hello("World");
            System.out.println("result:" + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProxy() throws Exception {
        HelloService hsl = new HelloServiceImpl();
        HelloService proxy = (HelloService) Proxy.newProxyInstance(HelloService.class.getClassLoader(), new Class<?>[]{HelloService.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(hsl, args);
            }
        });
        System.out.println(proxy.hello("Hello"));
    }
}
