package com.zjzcn.test.water;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyClient implements Client{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long REQUEST_TIMEOUT_MS = 10000;

	private EventLoopGroup eventLoopGroup;
	private Channel channel;
	private Bootstrap bootstrap;
	
	private String serverHost;
	private int serverPort;

	private SocketAddress localAddress;
	private SocketAddress remoteAddress;
	
	private CallbackManager callbackManager;

	public NettyClient(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;

		callbackManager = new CallbackManager();

		bootstrap = new Bootstrap();
		eventLoopGroup = new NioEventLoopGroup();
		bootstrap.group(eventLoopGroup)
		.channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("encoder", new NettyEncoder());
				pipeline.addLast("decoder", new NettyDecoder());
				pipeline.addLast("handler", new NettyClientHandler(callbackManager));
			}
		});
	}
	
	@Override
	public synchronized void connect() {
		logger.info("Http client connecting...");
		try {
			channel = bootstrap.connect(serverHost, serverPort).sync().channel();
			localAddress = channel.localAddress();
			remoteAddress = channel.remoteAddress();
			logger.info("Netty client connected. localAttress[{}], remoteAddress[{}].", localAddress, remoteAddress);
		} catch (Exception e) {
			logger.error("Netty client error while connecting.", e);
		}
	}

	@Override
	public synchronized void reconnect() {
		logger.info("Http client reconnecting...");
		try {
			channel.close();
			channel = bootstrap.connect(serverHost, serverPort).sync().channel();
			localAddress = channel.localAddress();
			remoteAddress = channel.remoteAddress();
			logger.info("Netty client connected. localAttress[{}], remoteAddress[{}].", localAddress, remoteAddress);
		} catch (Exception e) {
			logger.error("Netty client error while connecting.", e);
		}
	}
	
	@Override
	public synchronized void close() {
		logger.info("Http client closing...");
		try {
			// 取消定期的回收任务
			callbackManager.cancelTimeoutChecker();
			// 关闭连接池
			eventLoopGroup.shutdownGracefully();
			// 清空回调
			callbackManager.clearCallback();
			channel.close();
			logger.info("Netty client closed, remoteAddress={}", remoteAddress);
		} catch (Exception e) {
			logger.error("Netty client close Error: remoteAddress={}", remoteAddress, e);
		}
	}

	@Override
	public Response send(Request request) {
        return send(request, REQUEST_TIMEOUT_MS);
	}

    @Override
    public Response send(Request request, long timeoutMillis) {
        ResponseFuture newResponseFuture = new ResponseFuture(request, timeoutMillis);
        callbackManager.registerCallback(request.getRequestId(), newResponseFuture);

        ChannelFuture writeFuture = channel.write(request);
        channel.flush();

        boolean result = writeFuture.awaitUninterruptibly(timeoutMillis, TimeUnit.MILLISECONDS);

        if(result && writeFuture.isSuccess()) {
            return newResponseFuture.get();
        } else {
            ResponseFuture responseFuture = callbackManager.removeCallback(request.getRequestId());

            if (responseFuture != null) {
                responseFuture.cancel();
            }

            String errorMsg = "Error while sending request to server, remoteAddress=" + remoteAddress
                    + ", localAddress=" + localAddress +", requestId=" +  request.getRequestId();
            if (writeFuture.cause()!= null) {
                throw new RuntimeException(errorMsg, writeFuture.cause());
            } else {
                throw new RuntimeException(errorMsg);
            }
        }
    }

	@Override
	public boolean isConnected() {
		if(channel == null) {
			return false;
		}
		return channel.isActive();
	}
	
	@Override
	public SocketAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
}
