package io.github.damiancritchfield.binchat.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {

    @Bean
    public ChannelGroup channelGroup(){
        return new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    }

    @Bean
    public EventLoopGroup eventLoopGroup(){
        return new NioEventLoopGroup();
    }

}
