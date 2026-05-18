import ru.gr0946x.ui.Gui;
import javax.swing.*;

public class Client {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ru.gr0946x.net.Client client = new ru.gr0946x.net.Client("localhost", 9468);
                Gui gui = new Gui();

                gui.addUserDataListener(client::sendData);
                client.addDataListener(gui::showInfo);

                client.start();
                gui.start();

                System.out.println("GUI клиент запущен");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Ошибка подключения: " + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}