import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WeatherAppGUI().setVisible(true); //display the weather app gui

                System.out.println(WeatherAppBackendLogic.getLocationData("Berlin"));
                System.out.println(WeatherAppBackendLogic.getCurrentTime());

            }
        });
    }
}