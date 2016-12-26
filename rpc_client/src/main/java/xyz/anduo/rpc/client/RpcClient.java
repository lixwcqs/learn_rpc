package xyz.anduo.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.anduo.rpc.common.CqsLogger;
import xyz.anduo.rpc.common.RpcDecoder;
import xyz.anduo.rpc.common.RpcEncoder;


public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> implements CqsLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

	private String host;
	private int port;

	private RpcResponse response;

	private final Object obj = new Object();

	public RpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		this.response = response;
		logger.debug("channelRead0- object " + obj);
		synchronized (obj) {
			obj.notifyAll(); // 收到响应，唤醒线程
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("client caught exception", cause);
		ctx.close();
	}

	public RpcResponse send(RpcRequest request) throws Exception {
		logger.debug("send message");
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
							.addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
							.addLast(RpcClient.this); // 使用 RpcClient 发送 RPC 请求
				}
			}).option(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().writeAndFlush(request).sync();
			logger.debug("object:" + obj.getClass());
			synchronized (obj) {
//				obj.wait(); // 未收到响应，使线程等待
				obj.wait(3000);
			}
			logger.debug("wait over...");
			if (response != null) {
				future.channel().closeFuture().sync();
			}
			return response;
		} finally {
			group.shutdownGracefully();
		}
	}
}