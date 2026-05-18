package ru.gr0946x.net;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Scanner;

public class Server {
    private boolean isActive;
    private final ApplicationContext springContext;

    public Server(int port, ApplicationContext context) {
        this.springContext = context;
        isActive = true;
        startConsoleInterface();

        new Thread(() -> {
            try (var serverSocket = new ServerSocket(port)) {
                System.out.println("Сервер запущен на порту " + port);
                while (isActive) {
                    var socket = serverSocket.accept();
                    System.out.println("Клиент подключен");
                    var connClient = new ConnectedClient(socket, springContext);
                    connClient.start();
                }
            } catch (IOException e) {
                System.out.println("Ошибка сервера: " + e.getMessage());
            }
        }).start();
    }
    private void startConsoleInterface() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Серверный консольный интерфейс запущен");
            System.out.println("Доступные команды: /stats, /users, /stop");

            while (isActive) {
                String command = scanner.nextLine();
                switch (command) {
                    case "/stats":
                        List<ConnectedClient> allClients = ConnectedClient.getAllClients();
                        System.out.println("Подключено клиентов: " + allClients.size());
                        System.out.println("Активных пользователей: " +
                                allClients.stream()
                                        .filter(c -> c.getCurrentUser() != null)
                                        .count());
                        break;
                    case "/users":
                        ConnectedClient.getAllClients().stream()
                                .filter(c -> c.getCurrentUser() != null)
                                .forEach(c -> System.out.println(" - " + c.getCurrentUser().getNickname()));
                        break;
                    case "/stop":
                        System.out.println("Остановка сервера...");
                        isActive = false;
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Неизвестная команда");
                }
            }
        }).start();
    }
}