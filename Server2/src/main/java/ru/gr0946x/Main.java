package ru.gr0946x;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import ru.gr0946x.net.Server;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        var context = new SpringApplicationBuilder(Main.class)
                .headless(false)
                .run(args);
        new Thread(() -> {
            new Server(9468, context);
        }).start();
    }
}