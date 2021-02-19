package io.github.damiancritchfield.binchat.netty.initializer;

import io.github.damiancritchfield.binchat.config.BinChatConfig;
import io.github.damiancritchfield.binchat.netty.handler.HttpRequestHandler;
import io.github.damiancritchfield.binchat.netty.handler.TextWebSocketFrameHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ChatServerInitializer extends ChannelInitializer<Channel> {

    @Resource
    private ChannelGroup channelGroup;

    @Resource
    private BinChatConfig binChatConfig;

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline channelPipeline = channel.pipeline();

        channelPipeline.addLast(new HttpServerCodec());
        channelPipeline.addLast(new ChunkedWriteHandler());
        channelPipeline.addLast(new HttpObjectAggregator(64 * 1024));

        //协议升级
        channelPipeline.addLast(new HttpRequestHandler(binChatConfig.getWebsocketUri()));

        channelPipeline.addLast(new WebSocketServerProtocolHandler(binChatConfig.getWebsocketUri()));

        //文本聊天
        channelPipeline.addLast(new TextWebSocketFrameHandler(channelGroup));
    }
}
