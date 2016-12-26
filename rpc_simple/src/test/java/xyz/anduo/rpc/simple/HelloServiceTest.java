package xyz.anduo.rpc.simple;



import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import xyz.anduo.rpc.client.RpcProxy;
import xyz.anduo.rpc.sieve.modules.simple.HelloService;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath*:spring-client.xml","classpath*:spring-server.xml"})
@ContextConfiguration(locations = {"classpath*:spring-client.xml"})
public class HelloServiceTest {

	@Autowired
    private RpcProxy rpcProxy;
 
    @Test
    public void helloTest() {
        try {
			System.out.println(rpcProxy);
			HelloService helloService = rpcProxy.create(HelloService.class);
			System.out.println(helloService);
			String result = helloService.hello("World");
			System.out.println("result:" + result);
			Assert.assertEquals("Hello! World", result);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
