package io.github.damiancritchfield.binchat;

import io.github.damiancritchfield.binchat.netty.server.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BinchatApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BinchatApplication.class);

    @Autowired
    private ChatServer chatServer;

    public static void main(String[] args) {
        SpringApplication.run(BinchatApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("starting");
        chatServer.main();
    }
}
