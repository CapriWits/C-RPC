package me.hypocrite30.rpc.core.remote.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.factory.SingletonFactory;
import me.hypocrite30.rpc.common.utils.concurrent.threadpool.ThreadPoolUtils;
import me.hypocrite30.rpc.common.utils.net.NetUtils;
import me.hypocrite30.rpc.core.config.RpcServiceConfig;
import me.hypocrite30.rpc.core.config.ShutdownHook;
import me.hypocrite30.rpc.core.provider.ServiceProvider;
import me.hypocrite30.rpc.core.provider.impl.EtcdServiceProvider;
import me.hypocrite30.rpc.core.remote.transport.netty.codec.RpcMessageDecoder;
import me.hypocrite30.rpc.core.remote.transport.netty.codec.RpcMessageEncoder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Rpc Server based on Netty
 *
 * @Author: Hypocrite30
 * @Date: 2021/11/18 22:40
 */
@Slf4j
@Component
public class NettyRpcServer {

    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(EtcdServiceProvider.class);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start() {
        // unregister firstly
        ShutdownHook.getShutdownHook().unregister();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // number of thread is double number of CPU cores
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                Runtime.getRuntime().availableProcessors() * 2,
                ThreadPoolUtils.createThreadFactory("service-handler-group", false));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // disable TCP "Nagle" algorithm
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // turn on heartbeat
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // close connection if server not receive client request in 30s
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            String host = NetUtils.getLocalHostExactAddress().toString();
            ChannelFuture future = bootstrap.bind(NetUtils.newInetSocketAddress(host + ":" + PORT)).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("netty server start error", e);
        } finally {
            log.info("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
