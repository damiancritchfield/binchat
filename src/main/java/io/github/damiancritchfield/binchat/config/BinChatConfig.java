package io.github.damiancritchfield.binchat.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BinChatConfig {

    @Value("${binchat.server.port}")
    private Integer serverPort;

    @Value("${binchat.server.ws}")
    private String websocketUri;

    @Value("${binchat.server.ssl}")
    private boolean ssl;
}
