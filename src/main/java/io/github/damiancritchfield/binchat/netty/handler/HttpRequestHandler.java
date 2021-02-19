package io.github.damiancritchfield.binchat.netty.handler;

import io.github.damiancritchfield.binchat.common.BusinessException;
import io.github.damiancritchfield.binchat.config.BinChatConfig;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import org.apache.commons.io.FileUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String websocketUri;

//    private static final File INDEX;
//    static {
//        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
//        try {
//            String path = location.toURI() + "index.html";
//            path = !path.contains("file:") ? path : path.substring(5);
//            INDEX = new File(path);
//        } catch (URISyntaxException e) {
//            throw new IllegalStateException("Unable to locate index.html", e);
//        }
//    }

    public HttpRequestHandler(String websocketUri){
        this.websocketUri = websocketUri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        //如果请求的不是websocket协议，则响应首页html文件
        if(!websocketUri.equalsIgnoreCase(fullHttpRequest.uri())){
            this.responseIndexFile(channelHandlerContext, fullHttpRequest);
            return;
        }

        //放行，使数据流向下一个handler
        channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause.getCause() instanceof SSLHandshakeException){
//            ctx.channel().writeAndFlush("Received fatal alert: certificate_unknown");
            ctx.writeAndFlush("Received fatal alert: certificate_unknown");
        }
        cause.printStackTrace();
        ctx.close();
    }

    private File readIndexFile(){
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("html/index.html");
        File indexFile = new File("index.html");
        try {
            FileUtils.copyToFile(inputStream, indexFile);
            assert inputStream != null;
            inputStream.close();
            return indexFile;
        } catch (IOException e) {
            throw new BusinessException();
        }
    }

    /**
     * 响应首页文件
     */
    private void responseIndexFile(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws IOException {

        //100 Continue符合Http1.1规范
        if(HttpUtil.is100ContinueExpected(fullHttpRequest)){
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
            channelHandlerContext.writeAndFlush(response);
        }

        //读取 index.html
        File indexFile = this.readIndexFile();
        RandomAccessFile file = new RandomAccessFile(indexFile, "r");
        HttpResponse response = new DefaultHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        //如果请求了keep-alive，则添加所需要的 HTTP 头信息
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            response.headers().set( HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        //将 HttpResponse 写到客户端
        channelHandlerContext.write(response);

        //将 index.html 写到客户端
        if (channelHandlerContext.pipeline().get(SslHandler.class) == null) {
            channelHandlerContext.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
        } else {
            channelHandlerContext.write(new ChunkedNioFile(file.getChannel()));
        }

        //写 LastHttpContent 并冲刷至客户端
        ChannelFuture future = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        //如果没有请求keep-alive，则在写操作完成后关闭 Channel
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
