package ru.gr0946x.ui;

import ru.gr0946x.net.MessageType;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Gui extends JFrame implements Ui {
    private JTextPane chatArea;
    private JTextField messageField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JButton sendButton;
    private JLabel statusLabel;
    private JLabel onlineCountLabel;
    private JButton publicChatButton;
    private JLabel chatTitleLabel;
    private JPanel searchTopPanel;
    private JTextField inlineSearchField;
    private JButton inlineSearchButton;
    private JButton clearHighlightButton;
    private JDialog authDialog;
    private String currentChatWith = null;
    private String currentUserName = null;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private JDialog registerDialog;
    private java.util.function.Consumer<String> dataSender;
    private JTextField loginField;
    private Map<String, String> privateChatHistory = new HashMap<>();
    private StringBuilder publicChatContent = new StringBuilder();
    private boolean isAuthenticated = false;

    public Gui() {
        initComponents();
        setupListeners();
    }

    private void initComponents() {
        setTitle("Мессенджер Карета");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Верхняя панель
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));

        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftTopPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        statusLabel = new JLabel("Не авторизован");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        leftTopPanel.add(statusLabel);

        JPanel centerTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        centerTopPanel.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 0));

        searchTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchTopPanel.setVisible(false);

        inlineSearchField = new JTextField(10);
        inlineSearchField.setPreferredSize(new Dimension(120, 24));
        inlineSearchField.setToolTipText("Поиск в этом чате");

        inlineSearchButton = new JButton("🔍");
        inlineSearchButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        inlineSearchButton.setPreferredSize(new Dimension(50, 25));

        clearHighlightButton = new JButton("✖");
        clearHighlightButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        clearHighlightButton.setPreferredSize(new Dimension(50, 25));

        searchTopPanel.add(inlineSearchField);
        searchTopPanel.add(inlineSearchButton);
        searchTopPanel.add(clearHighlightButton);
        centerTopPanel.add(searchTopPanel);

        onlineCountLabel = new JLabel("Онлайн: 0");
        onlineCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        onlineCountLabel.setForeground(new Color(0, 150, 0));

        topPanel.add(leftTopPanel, BorderLayout.WEST);
        topPanel.add(centerTopPanel, BorderLayout.CENTER);
        topPanel.add(onlineCountLabel, BorderLayout.EAST);

        // Центральная часть
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));

        JPanel chatHeaderPanel = new JPanel(new BorderLayout());
        chatTitleLabel = new JLabel("Общий чат");
        chatTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        publicChatButton = new JButton("Общий чат");
        publicChatButton.setEnabled(false);
        publicChatButton.setVisible(false);

        chatHeaderPanel.add(chatTitleLabel, BorderLayout.WEST);
        chatHeaderPanel.add(publicChatButton, BorderLayout.EAST);
        chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        chatArea.setBackground(new Color(255, 255, 255));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        JPanel usersPanel = new JPanel(new BorderLayout(20, 5));
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Arial", Font.PLAIN, 13));
        userList.setBackground(new Color(245, 245, 245));
        userList.setEnabled(false);

        JScrollPane usersScroll = new JScrollPane(userList);
        usersScroll.setPreferredSize(new Dimension(200, 0));
        usersPanel.add(new JLabel("Онлайн пользователи:"), BorderLayout.NORTH);
        usersPanel.add(usersScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);
        splitPane.setLeftComponent(chatPanel);
        splitPane.setRightComponent(usersPanel);

        // Нижняя панель
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 13));
        messageField.setEnabled(false);

        sendButton = new JButton("Отправить");
        sendButton.setBackground(new Color(0, 120, 215));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setEnabled(false);

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(messagePanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);

        setupTextPaneStyle();
    }

    private void setupTextPaneStyle() {
        StyledDocument doc = chatArea.getStyledDocument();

        Style systemStyle = chatArea.addStyle("system", null);
        StyleConstants.setForeground(systemStyle, Color.GRAY);
        StyleConstants.setItalic(systemStyle, true);

        Style myStyle = chatArea.addStyle("my", null);
        StyleConstants.setForeground(myStyle, Color.BLACK);
        StyleConstants.setBold(myStyle, true);

        Style otherStyle = chatArea.addStyle("other", null);
        StyleConstants.setForeground(otherStyle, Color.BLACK);
    }

    private void setupListeners() {
        messageField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        inlineSearchButton.addActionListener(e -> performInlineSearch());
        inlineSearchField.addActionListener(e -> performInlineSearch());

        clearHighlightButton.addActionListener(e -> {
            chatArea.getHighlighter().removeAllHighlights();
            if (isAuthenticated) {
                statusLabel.setText("Авторизован: " + currentUserName);
            }
        });

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && isAuthenticated) {
                String selected = userList.getSelectedValue();
                if (selected != null) {
                    if (selected.equals("Избранное")) {
                        selected = currentUserName;
                    }
                    if (!selected.equals(currentChatWith)) {
                        switchToPrivateChat(selected);
                    }
                }
            }
        });

        publicChatButton.addActionListener(e -> switchToPublicChat());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (dataSender != null) {
                    try {
                        dataSender.accept("/exit");
                        Thread.sleep(200);
                    } catch (Exception ex) {
                        System.out.println("Ошибка при отправке /exit: " + ex.getMessage());
                    }
                }
                System.exit(0);
            }
        });
    }

    private void performInlineSearch() {
        String searchText = inlineSearchField.getText().trim();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст для поиска", "Поиск",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (currentChatWith == null) {
            JOptionPane.showMessageDialog(this, "Поиск доступен только в личных чатах", "Поиск",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        highlightTextInChat(searchText);
    }

    private void enableChatFeatures(boolean enable) {
        messageField.setEnabled(enable);
        sendButton.setEnabled(enable);
        userList.setEnabled(enable);
        publicChatButton.setVisible(enable);
    }

    private void switchToPrivateChat(String username) {
        saveCurrentChatContent();

        if (username.equals(currentUserName)) {
            currentChatWith = currentUserName;
            chatTitleLabel.setText("Личный чат • Избранное");
        } else {
            currentChatWith = username;
            chatTitleLabel.setText("Личный чат с " + username);
        }

        publicChatButton.setVisible(true);
        publicChatButton.setEnabled(true);
        searchTopPanel.setVisible(true);

        inlineSearchField.setText("");
        chatArea.getHighlighter().removeAllHighlights();
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        clearChatArea();
        loadHistory(currentChatWith);
        userList.clearSelection();
    }

    private void switchToPublicChat() {
        saveCurrentChatContent();

        currentChatWith = null;
        chatTitleLabel.setText("Общий чат");
        publicChatButton.setVisible(false);
        searchTopPanel.setVisible(false);

        inlineSearchField.setText("");
        chatArea.getHighlighter().removeAllHighlights();
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        if (publicChatContent.length() > 0) {
            setChatContent(publicChatContent.toString());
        } else {
            clearChatArea();
            appendSystemMessage("Добро пожаловать в общий чат!");
        }

        userList.clearSelection();
    }

    private void saveCurrentChatContent() {
        if (currentChatWith == null) {
            publicChatContent = new StringBuilder(getChatContent());
        } else {
            privateChatHistory.put(currentChatWith, getChatContent());
        }
    }

    private String getChatContent() {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void setChatContent(String content) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.remove(0, doc.getLength());
            doc.insertString(0, content, null);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void clearChatArea() {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || !isAuthenticated) return;

        if (text.equals("/exit")) {
            if (dataSender != null) {
                dataSender.accept("/exit");
            }
            System.exit(0);
            return;
        }

        String messageToSend;

        // Если выбран личный чат (не null и не общий чат)
        if (currentChatWith != null && !currentChatWith.isEmpty()) {
            String targetUser = currentChatWith;
            if (currentChatWith.equals("Избранное")) {
                targetUser = currentUserName;
            }

            messageToSend = "@" + targetUser + " " + text;
        } else {
            // Отправляем в общий чат
            messageToSend = text;
        }

        if (dataSender != null) {
            dataSender.accept(messageToSend);
        }

        messageField.setText("");
        messageField.requestFocus();
    }
    private void loadHistory(String username) {
        if (dataSender != null) {
            dataSender.accept("/history " + username);
        }
    }

    private void highlightTextInChat(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return;
        }

        try {
            StyledDocument doc = chatArea.getStyledDocument();
            String fullContent = doc.getText(0, doc.getLength());
            String[] lines = fullContent.split("\n");
            chatArea.getHighlighter().removeAllHighlights();

            int currentPosition = 0;
            int foundCount = 0;
            String lowerSearch = searchText.toLowerCase();

            for (String line : lines) {
                int lineStartPosition = currentPosition;
                int lineLength = line.length();
                int colonAfterTimeIndex = line.indexOf("] ");
                if (colonAfterTimeIndex == -1) {
                    currentPosition += lineLength + 1;
                    continue;
                }
                int messageStartIndex = line.indexOf(": ", colonAfterTimeIndex + 2);
                if (messageStartIndex == -1) {
                    currentPosition += lineLength + 1;
                    continue;
                }

                int textStartInLine = messageStartIndex + 2;

                int textStartInDoc = lineStartPosition + textStartInLine;

                int textLength = lineLength - textStartInLine;

                String messageText = line.substring(textStartInLine);
                String lowerMessageText = messageText.toLowerCase();

                int searchIndex = 0;
                while ((searchIndex = lowerMessageText.indexOf(lowerSearch, searchIndex)) != -1) {
                    try {
                        int highlightStart = textStartInDoc + searchIndex;
                        int highlightEnd = highlightStart + searchText.length();

                        chatArea.getHighlighter().addHighlight(
                                highlightStart,
                                highlightEnd,
                                new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 100))
                        );
                        searchIndex += searchText.length();
                        foundCount++;
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                currentPosition += lineLength + 1;
            }

            if (foundCount > 0) {
                String lowerFullContent = fullContent.toLowerCase();
                int firstMatchIndex = -1;

                String[] contentLines = fullContent.split("\n");
                int pos = 0;
                for (String line : contentLines) {
                    int colonAfterTime = line.indexOf("] ");
                    if (colonAfterTime != -1) {
                        int messageStart = line.indexOf(": ", colonAfterTime + 2);
                        if (messageStart != -1) {
                            int textStart = messageStart + 2;
                            String messageText = line.substring(textStart);
                            int matchInMessage = messageText.toLowerCase().indexOf(lowerSearch);
                            if (matchInMessage != -1) {
                                firstMatchIndex = pos + textStart + matchInMessage;
                                break;
                            }
                        }
                    }
                    pos += line.length() + 1;
                }

                if (firstMatchIndex >= 0) {
                    try {
                        Rectangle rect = chatArea.modelToView(firstMatchIndex);
                        if (rect != null) {
                            chatArea.scrollRectToVisible(rect);
                            chatArea.setCaretPosition(firstMatchIndex);
                        }
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "Найдено совпадений в тексте сообщений: " + foundCount,
                        "Результаты поиска",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this,
                        "Текст \"" + searchText + "\" не найден в сообщениях",
                        "Результаты поиска",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void start() {
        setVisible(true);
        showAuthDialog();
    }

    private void showAuthDialog() {
        if (authDialog != null && authDialog.isShowing()) {
            authDialog.toFront();
            authDialog.requestFocus();
            return;
        }

        if (registerDialog != null && registerDialog.isShowing()) {
            registerDialog.dispose();
            registerDialog = null;
        }

        loginField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Логин:"), gbc);

        gbc.gridx = 1;
        panel.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Пароль:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton loginButton = new JButton("Войти");
        JButton registerButton = new JButton("Зарегистрироваться");

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        authDialog = new JDialog(this, "Вход в мессенджер", true);
        authDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        authDialog.add(panel);
        authDialog.pack();
        authDialog.setLocationRelativeTo(this);

        authDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!isAuthenticated) {
                    System.exit(0);
                }
                authDialog = null;
            }
        });

        loginButton.addActionListener(e -> {
            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        authDialog,
                        "Введите логин и пароль",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (dataSender != null) {
                dataSender.accept("/auth " + login + " " + password);
            }
        });

        registerButton.addActionListener(e -> {
            authDialog.dispose();
            authDialog = null;
            showRegisterDialog();
        });

        authDialog.setVisible(true);
    }

    private void showRegisterDialog() {
        if (authDialog != null && authDialog.isVisible()) {
            authDialog.dispose();
            authDialog = null;
        }

        if (registerDialog != null && registerDialog.isVisible()) {
            registerDialog.toFront();
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField loginField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Логин:"), gbc);
        gbc.gridx = 1;
        panel.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Подтвердите пароль:"), gbc);
        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);

        JButton regButton = new JButton("Зарегистрироваться");
        JButton cancelButton = new JButton("Назад");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(regButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        registerDialog = new JDialog(this, "Регистрация", true);
        registerDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        registerDialog.add(panel);
        registerDialog.pack();
        registerDialog.setLocationRelativeTo(this);

        registerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                registerDialog = null;
                if (!isAuthenticated) {
                    showAuthDialog();
                }
            }
        });

        regButton.addActionListener(e -> {
            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Заполните все поля!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (login.contains(" ")) {
                JOptionPane.showMessageDialog(registerDialog,
                        "Ошибка: логин не может содержать пробелы!",
                        "Ошибка регистрации",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (login.length() > 0 && Character.isDigit(login.charAt(0))) {
                JOptionPane.showMessageDialog(registerDialog,
                        "Ошибка: логин не может начинаться с цифры!",
                        "Ошибка регистрации",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!Character.isLetter(login.charAt(0))) {
                JOptionPane.showMessageDialog(registerDialog,
                        "Ошибка: логин должен начинаться с буквы!",
                        "Ошибка регистрации",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerDialog, "Пароли не совпадают!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dataSender != null) {
                dataSender.accept("/reg " + login + " " + password);
            }
        });

        cancelButton.addActionListener(e -> {
            registerDialog.dispose();
            registerDialog = null;
            showAuthDialog();
        });

        registerDialog.setVisible(true);
    }

    @Override
    public void showInfo(String data, MessageType type) {

        SwingUtilities.invokeLater(() -> {
            Window parent = getParentWindow();

            switch (type) {
                case MESSAGE -> {
                    int colonIndex = data.indexOf(':');
                    if (colonIndex > 0 && isAuthenticated) {
                        String from = data.substring(0, colonIndex);
                        String text = data.substring(colonIndex + 1);

                        if (text.contains("вошел в сеть") || text.contains("вышел из сети") ||
                                text.contains("подключился") || text.contains("отключился")) {
                            if (currentChatWith == null) {
                                String timestamp = LocalDateTime.now().format(timeFormatter);
                                String messageLine = "[" + timestamp + "] " + from + ": " + text + "\n";
                                publicChatContent.append(messageLine);
                                appendMessage(from, text, false);
                            }
                            return;
                        }

                        String timestamp = LocalDateTime.now().format(timeFormatter);
                        String messageLine = "[" + timestamp + "] " + from + ": " + text + "\n";
                        publicChatContent.append(messageLine);

                        if (currentChatWith == null) {
                            appendMessage(from, text, false);
                        }
                    }
                }
                case PRIVATE_MESSAGE -> {

                    int colonIndex = data.indexOf(':');
                    if (colonIndex <= 0 || !isAuthenticated) return;

                    String from = data.substring(0, colonIndex);
                    String text = data.substring(colonIndex + 1);

                    boolean isSelfMessage = from.equals(currentUserName);

                    String displayFrom = isSelfMessage ? "Я" : from;

                    String historyKey;

                    if (currentChatWith == null) {
                        return;
                    }

                    if (isSelfMessage) {
                        historyKey = currentChatWith;
                    } else {
                        historyKey = from;
                    }

                    saveToPrivateChat(historyKey,
                            "[" + LocalDateTime.now().format(timeFormatter)
                                    + "] " + displayFrom + ": " + text + "\n"
                    );

                    if (currentChatWith.equals(historyKey)) {
                        appendPrivateMessage(displayFrom, text);
                    }
                }



                case AUTH_RESPONSE -> {
                    if (data.contains("успешная") || data.contains("Добро пожаловать")) {
                        isAuthenticated = true;
                        currentUserName = loginFromResponse(data);

                        setTitle("Мессенджер Карета - " + currentUserName);
                        statusLabel.setText("Авторизован: " + currentUserName);

                        enableChatFeatures(true);
                        clearChatArea();
                        appendSystemMessage("Вы успешно вошли в систему!");

                        if (authDialog != null && authDialog.isVisible()) {
                            authDialog.dispose();
                            authDialog = null;
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                parent,
                                "Ошибка авторизации: " + data,
                                "Ошибка входа",
                                JOptionPane.ERROR_MESSAGE
                        );
                        showAuthDialog();
                    }
                }

                case REGISTER_RESPONSE -> {
                    if (data.contains("успешно") || data.contains("УСПЕХ")) {
                        if (registerDialog != null && registerDialog.isVisible()) {
                            registerDialog.dispose();
                            registerDialog = null;
                        }

                        JOptionPane.showMessageDialog(
                                parent,
                                "Регистрация прошла успешно!\nТеперь войдите в систему.",
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        showAuthDialog();
                    } else {
                        JOptionPane.showMessageDialog(
                                parent,
                                "Ошибка регистрации:\n" + data,
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }

                case ERROR -> {
                    JOptionPane.showMessageDialog(
                            parent,
                            data,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );

                    if (!isAuthenticated && (
                            data.contains("не найден") ||
                                    data.contains("пароль") ||
                                    data.contains("уже существует") ||
                                    data.contains("Неверный пароль") ||
                                    data.contains("пользователь") && data.contains("не найден"))) {

                        if (registerDialog != null && registerDialog.isVisible()) {
                            registerDialog.dispose();
                            registerDialog = null;
                        }

                        if ((authDialog == null || !authDialog.isVisible())) {
                            SwingUtilities.invokeLater(() -> {
                                if (authDialog != null && authDialog.isVisible()) {
                                    authDialog.dispose();
                                    authDialog = null;
                                }
                                showAuthDialog();
                            });
                        }
                    }
                }

                case AUTH_REQUEST -> {
                    if (!isAuthenticated) {
                        showAuthDialog();
                    }
                }

                case USER_LIST -> {
                    if (isAuthenticated) {
                        updateUserList(data);
                    }
                }

                case HISTORY -> {
                        appendHistoryMessage(data);
                }

                case INFO -> {
                    if (!data.contains("/reg") && !data.contains("зарегистрироваться")) {
                        appendSystemMessage(data);
                    }
                }

                default -> {
                    appendSystemMessage(data);
                }
            }
        });
    }

    private Window getParentWindow() {
        if (authDialog != null && authDialog.isShowing()) return authDialog;
        if (registerDialog != null && registerDialog.isShowing()) return registerDialog;
        return this;
    }

    private String loginFromResponse(String response) {
        if (response.contains("Добро пожаловать, ")) {
            String name = response
                    .replace("Добро пожаловать, ", "")
                    .replace("!", "")
                    .trim();
            return name;
        }
        return "Пользователь";
    }

    private void appendHistoryMessage(String text) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            int bracketEnd = text.indexOf(']');
            if (bracketEnd > 0) {
                String time = text.substring(1, bracketEnd);
                String rest = text.substring(bracketEnd + 2);
                int colonIndex = rest.indexOf(':');
                if (colonIndex > 0) {
                    String from = rest.substring(0, colonIndex);
                    String message = rest.substring(colonIndex + 2);
                    appendMessage(from, message, false);
                    return;
                }
            }
            doc.insertString(doc.getLength(), text + "\n", chatArea.getStyle("other"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    private void saveToPrivateChat(String chatUser, String message) {

        String old = privateChatHistory.getOrDefault(chatUser, "");

        privateChatHistory.put(chatUser, old + message);
    }

    private void updateUserList(String data) {
        userListModel.clear();
        int onlineCount = 0;

        if (data != null && !data.isEmpty() && !data.equals("null")) {
            String[] users = data.split(",");
            for (String user : users) {
                String trimmedUser = user.trim();
                if (!trimmedUser.isEmpty()) {
                    onlineCount++;
                    if (trimmedUser.equals(currentUserName)) {
                        userListModel.addElement("Избранное");
                    } else {
                        userListModel.addElement(trimmedUser);
                    }
                }
            }
        }
        onlineCountLabel.setText("Онлайн: " + onlineCount);
    }

    private void appendMessage(String from, String text, boolean isPrivate) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            String timestamp = LocalDateTime.now().format(timeFormatter);
            Style senderStyle;
            boolean isSelfChat = (currentChatWith != null && currentChatWith.equals(currentUserName));

            if (isSelfChat) {
                senderStyle = chatArea.getStyle("other");
                if (senderStyle == null) {
                    senderStyle = chatArea.addStyle("other", null);
                    StyleConstants.setForeground(senderStyle, Color.BLACK);
                    StyleConstants.setBold(senderStyle, false);
                }
            } else if (isPrivate) {
                senderStyle = chatArea.getStyle("private");
            } else if (from.equals(currentUserName) || from.equals("Я")) {
                senderStyle = chatArea.getStyle("my");
                if (senderStyle == null) {
                    senderStyle = chatArea.addStyle("my", null);
                    StyleConstants.setForeground(senderStyle, Color.BLACK);
                    StyleConstants.setBold(senderStyle, true);
                }
            } else {
                senderStyle = chatArea.getStyle("other");
                if (senderStyle == null) {
                    senderStyle = chatArea.addStyle("other", null);
                    StyleConstants.setForeground(senderStyle, Color.BLACK);
                    StyleConstants.setBold(senderStyle, false);
                }
            }

            String displayName = (from.equals(currentUserName)) ? "Я" : from;
            doc.insertString(doc.getLength(), "[" + timestamp + "] ", senderStyle);
            doc.insertString(doc.getLength(), displayName + ": ", senderStyle);
            doc.insertString(doc.getLength(), text + "\n", senderStyle);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendPrivateMessage(String from, String text) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            String timestamp = LocalDateTime.now().format(timeFormatter);
            Style style;
            boolean isSelfChat = (currentChatWith != null && currentChatWith.equals(currentUserName));

            if (isSelfChat) {
                style = chatArea.getStyle("other");
                if (style == null) {
                    style = chatArea.addStyle("other", null);
                    StyleConstants.setForeground(style, Color.BLACK);
                    StyleConstants.setBold(style, false);
                }
                doc.insertString(doc.getLength(), "[" + timestamp + "] ", style);
                doc.insertString(doc.getLength(), from + ": ", style);
            } else if (from.equals("Я")) {
                style = chatArea.getStyle("my");
                if (style == null) {
                    style = chatArea.addStyle("my", null);
                    StyleConstants.setForeground(style, Color.BLACK);
                    StyleConstants.setBold(style, true);
                }
                doc.insertString(doc.getLength(), "[" + timestamp + "] ", style);
                doc.insertString(doc.getLength(), "Я: ", style);
            } else {
                style = chatArea.getStyle("private");
                if (style == null) {
                    style = chatArea.addStyle("private", null);
                    StyleConstants.setForeground(style, new Color(128, 0, 128));
                    StyleConstants.setBold(style, false);
                }
                doc.insertString(doc.getLength(), "[" + timestamp + "] ", style);
                doc.insertString(doc.getLength(), from + ": ", style);
            }

            doc.insertString(doc.getLength(), text + "\n", style);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendSystemMessage(String text) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), "[" + LocalDateTime.now().format(timeFormatter) + "] ",
                    chatArea.getStyle("system"));
            doc.insertString(doc.getLength(), text + "\n", chatArea.getStyle("system"));
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addUserDataListener(java.util.function.Consumer<String> listener) {
        this.dataSender = listener;
    }

}