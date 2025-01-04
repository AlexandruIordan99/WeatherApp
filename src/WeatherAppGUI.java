import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class WeatherAppGUI extends JFrame{
    private JSONObject weatherData;

    public WeatherAppGUI() {
//        creates gui window and adds a title
        super("Weather App");
        // configure gui to end the process once program is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //set the size of gui (pixels)
        setSize(450, 650);
        //load gui at the center of the screen
        setLocationRelativeTo(null);

        setLayout(null);  //setting Layout Manager to null lets us manually position components withing gui

        //prevent resizing of GUI
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents() {
        //Initialize search field
        JTextField searchTextField = new JTextField();

        // Set its location and size
        searchTextField.setBounds(15, 15, 351, 45);

        //change font size and style
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);


        //weather image
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/weatherapp_images/cloudy.png"));
        weatherConditionImage.setBounds(0,125,450,217);
        add(weatherConditionImage);

        //temperature text
        JLabel temperatureText = new JLabel("20 C");
        temperatureText.setBounds(0, 350, 450,54);
        temperatureText.setFont(new Font("Dialog", Font.PLAIN, 48));

        //center the temperature text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        //initialize weather condition description
        JLabel weatherConditionDesc = new JLabel("Cloudy");

        //give location, font, and center the weather condition description
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc); //adds weather conditions

        //humidity image
        JLabel humidityImage = new JLabel(loadImage("src/assets/weatherapp_images/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        //humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b></html>");
        humidityText.setBounds(15, 550, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        //wind speed image
        JLabel windSpeedImage = new JLabel(loadImage("src/assets/weatherapp_images/windspeed.png"));
        windSpeedImage.setBounds(180, 500, 74, 66);
        add(windSpeedImage);

        //wind speed & wind direction text
        JLabel wind_speedText = new JLabel("<html><b>Wind Speed</b></html>");
        JLabel wind_directionText = new JLabel("<html><b>Direction</b></html>");
        wind_speedText.setBounds(180, 550, 85, 55);
        wind_directionText.setBounds(240, 550, 85, 55);
        wind_speedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(wind_speedText);

        wind_directionText.setFont(new Font("Dialog", Font.PLAIN, 16));

        add(wind_directionText);

        //pressure image
        JLabel pressureImage = new JLabel(loadImage("src/assets/weatherapp_images/windpressure.png"));
        pressureImage.setBounds(300, 500, 74, 66);
        add(pressureImage);  //had to resize image to 138x132 and remove its background

        //pressure text
        JLabel pressureText = new JLabel("<html><b>Wind Pressure</b></html>");
        pressureText.setBounds(320, 550, 85, 55);
        add(pressureText);


        //clear image
        JLabel clearImage = new JLabel(loadImage("src/assets/weatherapp_images/clear.png"));

        //snow image
        JLabel snowImage = new JLabel(loadImage("src/assets/weatherapp_images/snow.png"));


        // Initiliaze search button
        JButton searchButton = new JButton(loadImage("src/assets/weatherapp_images/search.png"));

        //change cursor to a hand cursor when hovering over this button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get location from user
                String userInput = searchTextField.getText();
                //reformatting input: removing whitespaces to ensure non-empty text
                userInput = userInput.replaceAll("\\s", "");
                // retrieve weather data
                weatherData = WeatherAppBackendLogic.getWeatherData(userInput);

                //update gui
                //update weather image
                String weatherCondition = (String) weatherData.get("weather_condition");

                //changing weatherCondition image based on corresponding condition, i.e. a cloud for cloudy weather
                switch(weatherCondition){
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/weatherapp_images/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/weatherapp_images/cloudy.png"));
                        break;
                    case "Foggy":
                        weatherConditionImage.setIcon(loadImage("src/assets/weatherapp_images/fog.png"));
                        break;
                    case "Snowy":
                        weatherConditionImage.setIcon(loadImage("src/assets/weatherapp_images/snow.png"));
                        break;
                    case "Rainy":
                        weatherConditionImage.setIcon(loadImage("src/assets/weatherapp_images/rain.png"));
                        break;
                }
                //update temperature text
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(String.valueOf(temperature + "C"));

                //update weather condition text
                weatherConditionDesc.setText(weatherCondition);

                //update humidity text
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b> " + humidity + "%</b></html>");

                //update surface pressure text
                double pressure = (double) weatherData.get("pressure");
                pressureText.setText("<html><b> " + pressure + "%</b></html>");

                //update wind speed text
                double wind_speed = (double) weatherData.get("wind_speed");
                wind_speedText.setText("<html><b> " + wind_speed +"m/s" + "</b></html>");

                //update wind direction text
                String wind_direction = (String) weatherData.get("wind_direction");
                wind_directionText.setText("<html><b> " + wind_direction + "</b></html>");
            }
        });
        add(searchButton);

    }

        private ImageIcon loadImage(String resourcePath){
            try{
            BufferedImage image = ImageIO.read(new File(resourcePath));  //reads image from file address
            return new ImageIcon(image); //returns the image when called
            } catch (IOException e) {

                e.printStackTrace();

            }
            System.out.println("Could not find the image.");
            return null;
        }
        }


