package ru.gr0946x.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Communicator {
    private Socket socket;
    private boolean isActive;

    private List<Consumer<String>> dataListeners = new ArrayList<>();

    public void addDataListener(Consumer<String> c){
        dataListeners.add(c);
    }

    public void removeDataListener(Consumer<String> c){
        dataListeners.remove(c);
    }

    public Communicator(Socket socket){
        this.socket = socket;
    }

    public void start(){
        isActive = true;
        new Thread(()-> {
            try (var br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                while (isActive) {
                    var data = br.readLine();
                    if (data == null) throw new RuntimeException("Ошибка получения данных");
                    for (var dataListener : dataListeners) {
                        dataListener.accept(data);
                    }
                }
            } catch (Exception e) {
                System.out.println("Ошибка чтения данных из сети");
                stop();
            }
        }).start();
    }

    public void sendData(String data){
        try {
            System.out.println("Communicator.sendData: " + data); // ОТЛАДКА
            var pw = new PrintWriter(socket.getOutputStream(), true); // autoFlush = true
            pw.println(data);
        } catch (Exception e) {
            System.out.println("Ошибка отправки данных: " + e.getMessage());
            e.printStackTrace(); // Покажем полную ошибку
            stop();
        }
    }

    public void stop(){
        isActive = false;
        try {
            socket.close();
        } catch (IOException e) {
        }
    }
    public boolean isActive() {
        return isActive;
    }
}
