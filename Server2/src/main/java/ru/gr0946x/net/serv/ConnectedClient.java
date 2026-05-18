package ru.gr0946x.net.serv;
import ru.gr0946x.net.Communicator;
import ru.gr0946x.net.MessageType;
import ru.gr0946x.net.entity.User;
import ru.gr0946x.net.entity.Message;
import ru.gr0946x.net.repository.UserRepository;
import ru.gr0946x.net.repository.MessageRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;

import java.awt.*;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectedClient {
    private Timer updateTimer;
    private final Communicator communicator;
    private static final List<ConnectedClient> clients = new CopyOnWriteArrayList<>();
    private final ApplicationContext springContext;
    private User currentUser = null;
    private String currentPrivateChatWith = null;
    public ConnectedClient(Socket socket, ApplicationContext context) {
        this.communicator = new Communicator(socket);
        this.springContext = context;
        communicator.addDataListener(this::parseData);
        clients.add(this);
    }

    public void start() {
        communicator.start();
    }

    public static List<ConnectedClient> getAllClients() {
        return clients;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void startOnlineListUpdater() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentUser != null) {
                    sendUserList();
                }
            }
        }, 5000, 5000);
    }

    private void handleAuth(String data) {
        // Регистрация
        if (data.startsWith("/reg ")) {
            String[] parts = data.substring(5).split(" ", 2);
            if (parts.length != 2) {
                sendData(MessageType.REGISTER_RESPONSE + ":ОШИБКА: Неверный формат! Используйте: /reg логин пароль");
                sendAuthRequest();
                return;
            }

            String nickname = parts[0];
            String password = parts[1];

            if (nickname.contains(" ")) {
                sendData(MessageType.REGISTER_RESPONSE + ":ОШИБКА: Логин не может содержать пробелы!");
                sendAuthRequest();
                return;
            }

            if (nickname.isEmpty() || !Character.isLetter(nickname.charAt(0))) {
                sendData(MessageType.REGISTER_RESPONSE + ":ОШИБКА: Логин должен начинаться с буквы!");
                sendAuthRequest();
                return;
            }

            if (password.isEmpty()) {
                sendData(MessageType.REGISTER_RESPONSE + ":ОШИБКА: Пароль не может быть пустым!");
                sendAuthRequest();
                return;
            }

            UserRepository userRepo = springContext.getBean(UserRepository.class);

            if (userRepo.existsByNicknameIgnoreCase(nickname)) {
                sendData(MessageType.REGISTER_RESPONSE + ":ОШИБКА: Пользователь '" + nickname + "' уже существует!");
                return;
            }

            User newUser = new User(nickname, password);
            userRepo.save(newUser);

            sendData(MessageType.REGISTER_RESPONSE + ":УСПЕХ: Регистрация прошла успешно!");
            return;
        }

        // Авторизация
        if (data.startsWith("/auth ")) {
            String[] parts = data.substring(6).split(" ", 2);
            if (parts.length != 2) {
                sendData(MessageType.ERROR + ":Формат: /auth логин пароль");
                sendAuthRequest();
                return;
            }

            String nickname = parts[0];
            String password = parts[1];

            if (nickname.contains(" ")) {
                sendData(MessageType.ERROR + ":Логин не может содержать пробелы!");
                sendAuthRequest();
                return;
            }

            if (nickname.isEmpty() || !Character.isLetter(nickname.charAt(0))) {
                sendData(MessageType.ERROR + ":Логин должен начинаться с буквы!");
                sendAuthRequest();
                return;
            }

            UserRepository userRepo = springContext.getBean(UserRepository.class);
            var existingUser = userRepo.findByNicknameIgnoreCase(nickname);

            if (existingUser.isPresent()) {
                if (existingUser.get().getPassword().equals(password)) {
                    boolean alreadyOnline = clients.stream()
                            .anyMatch(c -> c.currentUser != null && c.currentUser.getId() == existingUser.get().getId());

                    if (alreadyOnline) {
                        sendData(MessageType.ERROR + ":Пользователь уже в сети!");
                        sendAuthRequest();
                        return;
                    }

                    currentUser = existingUser.get();
                    sendData(MessageType.AUTH_RESPONSE + ":Добро пожаловать, " + nickname + "!");
                    startOnlineListUpdater();
                    broadcastUserJoined();
                    sendUserList();
                    return;
                } else {
                    sendData(MessageType.ERROR + ":Неверный пароль!");
                    sendAuthRequest();
                    return;
                }
            } else {
                sendData(MessageType.ERROR + ":Пользователь '" + nickname + "' не найден.");
                sendData(MessageType.INFO + ":Введите /reg логин пароль для регистрации");
                sendAuthRequest();
                return;
            }
        }

        sendData(MessageType.ERROR + ":Используйте /auth логин пароль или /reg логин пароль");
        sendAuthRequest();
    }

    private void sendAuthRequest() {
        sendData(MessageType.AUTH_REQUEST + ":");
    }

    private void parseData(String data) {
        System.out.println("parseData получено от клиента: " + data);
        System.out.println("currentUser=" + (currentUser != null ? currentUser.getNickname() : "null"));

        if (currentUser == null) {
            handleAuth(data);
            return;
        }

        if (data.startsWith("/history ")) {
            String targetNick = data.substring(9);
            sendHistory(targetNick);
            return;
        }

        if (data.startsWith("/search ")) {
            String[] parts = data.substring(8).split(" ", 2);
            if (parts.length == 2) {
                searchMessages(parts[0], parts[1]);
            } else {
                sendData(MessageType.ERROR + ":Формат: /search ник текст");
            }
            return;
        }

        if (data.equals("/online")) {
            sendUserList();
            return;
        }

        if (data.equals("/exit")) {
            stop();
            return;
        }

        if (data.startsWith("@")) {
            int spaceIndex = data.indexOf(' ');
            if (spaceIndex > 0) {
                String targetNick = data.substring(1, spaceIndex);
                String message = data.substring(spaceIndex + 1);
                sendPrivateMessage(targetNick, message);
            } else {
                sendData(MessageType.ERROR + ":Формат: @ник сообщение");
            }
            return;
        }

        broadcastMessage(data);
    }
    private void sendPrivateMessage(String targetNick, String text) {

        UserRepository userRepo = springContext.getBean(UserRepository.class);
        MessageRepository msgRepo = springContext.getBean(MessageRepository.class);

        var targetOptional = userRepo.findByNicknameIgnoreCase(targetNick);

        if (targetOptional.isEmpty()) {
            sendData(MessageType.ERROR + ":Пользователь не найден");
            return;
        }

        User targetUser = targetOptional.get();

        Message msg = new Message(
                currentUser.getId(),
                targetUser.getId(),
                text
        );
        msgRepo.save(msg);

        String senderNick = currentUser.getNickname();

        sendData(MessageType.PRIVATE_MESSAGE + ":" + senderNick + ":" + text);

        if (targetUser.getId() == currentUser.getId()) {
            return;
        }

        ConnectedClient targetClient = findClientByUserId(targetUser.getId());

        if (targetClient != null) {
            targetClient.sendData(
                    MessageType.PRIVATE_MESSAGE + ":" + senderNick + ":" + text
            );
        } else {
            sendData(MessageType.INFO + ":Пользователь оффлайн");
        }
    }



    private void broadcastMessage(String text) {
        MessageRepository msgRepo = springContext.getBean(MessageRepository.class);
        Message broadcastMsg = new Message(currentUser.getId(), 0, text);

        try {
            msgRepo.save(broadcastMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = MessageType.MESSAGE + ":" + currentUser.getNickname() + ":" + text;
        for (ConnectedClient client : clients) {
            if (client.currentUser != null) {
                client.sendData(message);
            }
        }
    }

    private void sendHistory(String withNick) {
        UserRepository userRepo = springContext.getBean(UserRepository.class);
        var other = userRepo.findByNicknameIgnoreCase(withNick);

        if (other.isEmpty()) {
            sendData(MessageType.ERROR + ":Пользователь '" + withNick + "' не найден");
            return;
        }

        MessageRepository msgRepo = springContext.getBean(MessageRepository.class);

        try {
            List<Message> recentMessages = msgRepo.findLastMessages(
                    currentUser.getId(),
                    other.get().getId(),
                    PageRequest.of(0, 50)
            );

            if (recentMessages.isEmpty()) {
                return;
            }

            for (int i = recentMessages.size() - 1; i >= 0; i--) {
                Message m = recentMessages.get(i);
                String from = (m.getSenderId() == currentUser.getId()) ? "Я" : withNick;
                String time = m.getTimestamp().toString().substring(0, 16);
                String line = "[" + time + "] " + from + ": " + m.getText();
                sendData(MessageType.HISTORY + ":" + line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendData(MessageType.ERROR + ":Ошибка при загрузке истории");
        }
    }

    private void searchMessages(String withNick, String searchText) {
        UserRepository userRepo = springContext.getBean(UserRepository.class);
        var other = userRepo.findByNicknameIgnoreCase(withNick);

        if (other.isEmpty()) {
            sendData(MessageType.ERROR + ":Пользователь '" + withNick + "' не найден");
            return;
        }

        MessageRepository msgRepo = springContext.getBean(MessageRepository.class);

        try {
            List<Message> found = msgRepo.searchMessagesJPQL(currentUser.getId(), other.get().getId(), searchText);

            if (found.isEmpty()) {
                sendData(MessageType.SEARCH_RESULT + ":Сообщения с '" + withNick + "' содержащие '" + searchText + "' не найдены");
                return;
            }

            sendData(MessageType.SEARCH_RESULT + ":Результаты поиска в чате с " + withNick + " (по запросу: '" + searchText + "'):");

            int counter = 1;
            for (int i = found.size() - 1; i >= 0; i--) {
                Message m = found.get(i);
                String from = (m.getSenderId() == currentUser.getId()) ? "Я" : withNick;
                String time = m.getTimestamp().toString().substring(0, 16);
                sendData(MessageType.SEARCH_RESULT + ":" + counter + ". [" + time + "] " + from + ": " + m.getText());
                counter++;
            }

            sendData(MessageType.SEARCH_RESULT + ":Найдено сообщений: " + found.size() + ".");
        } catch (Exception e) {
            e.printStackTrace();
            sendData(MessageType.ERROR + ":Ошибка при поиске сообщений");
        }
    }

    private void sendUserList() {
        StringBuilder online = new StringBuilder();
        for (ConnectedClient client : clients) {
            if (client.currentUser != null) {
                if (online.length() > 0) online.append(",");
                online.append(client.currentUser.getNickname());
            }
        }

        String list = online.toString();
        for (ConnectedClient client : clients) {
            if (client.currentUser != null) {
                client.sendData(MessageType.USER_LIST + ":" + list);
            }
        }
    }

    private void broadcastUserJoined() {
        for (ConnectedClient client : clients) {
            if (client.currentUser != null && client != this) {
                client.sendData(MessageType.INFO + ":Пользователь " + currentUser.getNickname() + " вошёл в чат");
            }
        }
        sendUserList();
    }

    private ConnectedClient findClientByUserId(int userId) {
        for (ConnectedClient client : clients) {
            if (client.currentUser != null && client.currentUser.getId() == userId) {
                return client;
            }
        }
        return null;
    }

    public void sendData(String data) {
        communicator.sendData(data);
    }

    public void stop() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        if (currentUser != null) {
            for (ConnectedClient client : clients) {
                if (client.currentUser != null && client != this) {
                    client.sendData(MessageType.INFO + ":Пользователь " + currentUser.getNickname() + " покинул чат");
                }
            }
            sendUserList();
        }
        clients.remove(this);
        communicator.stop();
    }

}