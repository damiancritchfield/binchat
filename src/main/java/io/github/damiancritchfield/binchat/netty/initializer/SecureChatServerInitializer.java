package io.github.damiancritchfield.binchat.netty.initializer;

import io.github.damiancritchfield.binchat.common.BusinessException;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

@Component
public class SecureChatServerInitializer extends ChatServerInitializer {

    @Resource
    private SslContext sslContext;

    @Override
    protected void initChannel(Channel channel) throws Exception {

        super.initChannel(channel);

        SSLEngine sslEngine = sslContext.newEngine(channel.alloc());
        sslEngine.setUseClientMode(false);

        channel.pipeline().addFirst(new SslHandler(sslEngine));
    }

    @Bean
    public SslContext sslContext(){
        try {
            SelfSignedCertificate cert = new SelfSignedCertificate();
            return SslContext.newServerContext(cert.certificate(), cert.privateKey());
        } catch (CertificateException | SSLException e) {
            throw new BusinessException();
        }
    }
}
