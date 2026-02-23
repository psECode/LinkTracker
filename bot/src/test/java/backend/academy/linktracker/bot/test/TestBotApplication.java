package backend.academy.linktracker.bot.test;

import backend.academy.linktracker.bot.BotApplication;
import org.springframework.boot.SpringApplication;

public class TestBotApplication {

    static void main(String[] args) {
        SpringApplication.from(BotApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
