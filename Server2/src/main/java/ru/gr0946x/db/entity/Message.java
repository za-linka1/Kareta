package ru.gr0946x.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "sender_id", nullable = false)
    private int senderId;

    @Column(name = "receiver_id", nullable = false)
    private int receiverId;  // 0 = сообщение для всех

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean delivered;

    public Message() {}
    public Message(int senderId, int receiverId, String text) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = LocalDateTime.now();
        this.delivered = false;
    }

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }

    public int getSenderId() {
        return senderId;
    }

//    public void setSenderId(int senderId) {
//        this.senderId = senderId;
//    }
//
//    public int getReceiverId() {
//        return receiverId;
//    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getText() {
        return text;
    }

//    public void setText(String text) {
//        this.text = text;
//    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
//
//    public void setTimestamp(LocalDateTime timestamp) {
//        this.timestamp = timestamp;
//    }
//
//    public boolean isDelivered() {
//        return delivered;
//    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
}