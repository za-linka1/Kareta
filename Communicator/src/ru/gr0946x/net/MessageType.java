package ru.gr0946x.net;

public enum MessageType {
    // базовые
    MESSAGE,
    INFO,
    REQUEST,
    ERROR,

    // для авторизации
    AUTH_REQUEST,
    AUTH_RESPONSE,
    REGISTER_REQUEST,
    REGISTER_RESPONSE,

    // для чата
    PRIVATE_MESSAGE,
    USER_LIST,
    HISTORY,
    SEARCH_RESULT
}
