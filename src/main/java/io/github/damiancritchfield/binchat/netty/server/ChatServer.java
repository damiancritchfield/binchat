package io.github.damiancritchfield.binchat.netty.server;

import io.github.damiancritchfield.binchat.config.BinChatConfig;
import io.github.damiancritchfield.binchat.netty.initializer.ChatServerInitializer;
import io.github.damiancritchfield.binchat.netty.initializer.SecureChatServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

@Component
public class ChatServer {

    @Resource
    private ChannelGroup channelGroup;

    @Resource
    private EventLoopGroup eventLoopGroup;

    @Resource
    private BinChatConfig binChatConfig;

    @Autowired
    private ChatServerInitializer chatServerInitializer;

    @Autowired
    private SecureChatServerInitializer secureChatServerInitializer;

    private Channel channel;

    public ChannelFuture start(InetSocketAddress inetSocketAddress){

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(binChatConfig.isSsl() ? secureChatServerInitializer : chatServerInitializer);

        ChannelFuture channelFuture = serverBootstrap.bind(inetSocketAddress);
        channelFuture.syncUninterruptibly();

        channel = channelFuture.channel();
        return channelFuture;
    }

    public void destroy(){
        if(channel != null){
            channel.close();
        }
        channelGroup.close();
        eventLoopGroup.shutdownGracefully();
    }

    public void main(){
        ChannelFuture future = this.start(new InetSocketAddress(binChatConfig.getServerPort()));
        Runtime.getRuntime().addShutdownHook(new Thread(this::destroy));
        future.channel().closeFuture().syncUninterruptibly();
    }
}
