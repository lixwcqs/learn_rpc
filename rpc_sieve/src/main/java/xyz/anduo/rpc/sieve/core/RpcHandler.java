package xyz.anduo.rpc.sieve.core;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.anduo.rpc.client.RpcRequest;
import xyz.anduo.rpc.client.RpcResponse;
import xyz.anduo.rpc.common.CqsLogger;
import xyz.anduo.rpc.sieve.modules.simple.HelloService;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> implements CqsLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

	private final Map<String, Object> handlerMap;

	public RpcHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, RpcRequest request) throws Exception {

		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		try {
			Object result = handle(request);
			logger.debug("channelRead0:\t" + result);
			response.setResult(result);
		} catch (Throwable t) {
			t.printStackTrace();
			response.setError(t);
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		logger.debug("className:" + className);
		Object serviceBean = handlerMap.get(className);
//		Object serviceBean = handlerMap.get(HelloService.class.getName());
		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();

		/*
		 * Method method = serviceClass.getMethod(methodName, parameterTypes);
		 * method.setAccessible(true); return method.invoke(serviceBean,
		 * parameters);
		 */

		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		Object invoke = serviceFastMethod.invoke(serviceBean, parameters);
		logger.debug("invoke:" + invoke);
		return invoke;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOGGER.error("server caught exception", cause);
		ctx.close();
	}
}
