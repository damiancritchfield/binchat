package io.github.damiancritchfield.binchat.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup channelGroup;

    public TextWebSocketFrameHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    //处理自定义事件
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        //如果该事件表示握手成功，则从该 ChannelPipeline 中移除HttpRequest-Handler，因为将不会接收到任何HTTP消息了
        if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){

            ctx.pipeline().remove(HttpRequestHandler.class);

            //通知所有已经连接的WebSocket客户端新的客户端已经连接上了
            channelGroup.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + " joined"));

            //将新的 WebSocket Channel 添加到 ChannelGroup 中，以便它可以接收到所有的消息
            channelGroup.add(ctx.channel());
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {

        //增加消息的引用计数，并将它写到 ChannelGroup 中所有已经连接的客户端
        channelGroup.writeAndFlush(textWebSocketFrame.retain());
    }
}
