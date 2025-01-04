import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class WeatherAppBackendLogic {
    //grab weather data for a given city, town, village
    public static JSONObject getWeatherData(String locationName){
        //get location coordinates using the geolocation API
        JSONArray locationData = getLocationData(locationName);

        //get longitude and latitude date
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (Double) location.get("latitude");
        double longitude = (Double) location.get("longitude");

        //build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" +
         latitude+"&longitude="+ longitude+ "&hourly=temperature_2m,relative_humidity_2m,precipitation,rain," +
                "weather_code,surface_pressure,wind_speed_10m,wind_direction_10m&timezone=Europe%2FBerlin";

        try{
            //call api and get a response
            HttpURLConnection conn_weather = fetchApiResponse(urlString);

            //check response status, 200 = success
            if (conn_weather.getResponseCode()!=200){
                System.out.println("Error: Connection to API failed.");
                return null;
            }
            //store the JSON data
            var resultJson = new StringBuilder();
            var scanner = new Scanner(conn_weather.getInputStream());
            while (scanner.hasNext()){
                resultJson.append(scanner.nextLine()); //reads and stores into the tring builder
            }
            scanner.close();
            conn_weather.disconnect();

            //parse through data
            var parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //retrieve hourly data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            //need current hour's data
            //grabbing it using the index of the current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (Double) temperatureData.get(index);

            //get weather condition from the weather code
            //all weather codes and their meanings are on the weather API's website
            JSONArray weather_code = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeather_Code((long) weather_code.get(index));

            //get humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            //get wind pressure
            JSONArray surface_pressure = (JSONArray) hourly.get("surface_pressure");
            double pressure = (double) surface_pressure.get(index);

            //get wind speed
            JSONArray wind_speed_10m = (JSONArray) hourly.get("wind_speed_10m");
            double wind_speed = (double) wind_speed_10m.get(index);

            //get wind direction as cardinal
            JSONArray wind_direction_10m = (JSONArray) hourly.get("wind_direction_10m");
            long wind_direction_long = (long) wind_direction_10m.get(index);
            String wind_direction = Long.toString(wind_direction_long);
            String wind_cardinal_direction = convertWindDirection(wind_direction);

            //build json weather data object that we'll access in the frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("pressure", pressure);
            weatherData.put("wind_speed", wind_speed);
            weatherData.put("wind_direction", wind_cardinal_direction);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static JSONArray getLocationData(String locationName){
        //replace whitespaces to '+' in location name to adhere to the API request format
        locationName = locationName.replaceAll(" ", "+");

        //API URL with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";
        try{
            //call the api and get a response
            HttpURLConnection conn = fetchApiResponse(urlString);

            //checking response status, if !=200, something went wrong
            assert conn != null;
            if (conn.getResponseCode() !=200){
                System.out.println("Error: Could not connect to geolocation API.");
                return null;
            }else{ //store the API results
                var resultJson= new StringBuilder();
                var scanner = new Scanner(conn.getInputStream());

                //read and store the resulting json data into the string builder
                while(scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();
                conn.disconnect();

                //parsing JSON string into a JSON object
                JSONParser parser = new JSONParser();
                JSONObject resultsJsobObject = (JSONObject) parser.parse(resultJson.toString());

                //get the list of location data generated by the API from the location name
                JSONArray locationData = (JSONArray) resultsJsobObject.get("results");

                return locationData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return null; //if the api can't find the location
    }


        private static HttpURLConnection fetchApiResponse(String urlString){
            try{
                //attempt to create a connection
                URL url = new URL(urlString);
                HttpURLConnection conn_geo = (HttpURLConnection) url.openConnection();

                // set request method to get
                conn_geo.setRequestMethod("GET");

                //connect to API
                conn_geo.connect();
                return conn_geo;

            }catch(IOException e){
                e.printStackTrace();

        }
            return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if(currentTime.equalsIgnoreCase(currentTime)){
                return i;
            }
        }
        return 0;
    }

    public static String getCurrentTime() {
        //getting current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        //re-format date so that it fits into the api url
        //being accurate to the hour is enough
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH': 00'");

        //format and return current date and time
        String formattedDate = currentDateTime.format(formatter);
        return formattedDate;
    }
    private static String convertWeather_Code(long weather_code) {
        String weatherCondition = "";
        if (weather_code == 0L) {
            weatherCondition = "Clear";
        }else if (weather_code > 1L && weather_code <= 3L) {
            weatherCondition = "Cloudy";
        } else if (weather_code ==45L){
            weatherCondition = "Fog";
        }else if (weather_code== 48L){
            weatherCondition = "Depositing rime fog";
        }else if (weather_code == 56L){
            weatherCondition = "Light Freezing Drizzle";
        }else if (weather_code == 57L){
            weatherCondition = "Dense Freezing Drizzle";
        }else if (weather_code >=51L && weather_code <= 55L){
            weatherCondition = "Drizzle";
        }else if (weather_code >=80L && weather_code <= 99L){
            weatherCondition = "Rain";
        }else if (weather_code >=71 && weather_code <= 77L){
            weatherCondition = "Snowy";
        }
        return weatherCondition;
    }
    private static String convertWindDirection (String wind_direction) throws ParseException {
        float wind_direction_int = new DecimalFormat("0.0").parse(wind_direction).floatValue();
        String wind_cardinal = "";
        if (wind_direction_int >=345 || wind_direction_int <=15) {
            wind_cardinal = "N";
        } else if (wind_direction_int >15 && wind_direction_int <=75) {
            wind_cardinal = "NE";
        } else if (wind_direction_int >75 && wind_direction_int <=105) {
            wind_cardinal = "E";
        } else if (wind_direction_int >105 && wind_direction_int <=165) {
            wind_cardinal = "SE";
        }else if (wind_direction_int >165 && wind_direction_int <=205) {
            wind_cardinal = "S";
        } else if (wind_direction_int >205 && wind_direction_int <=265) {
            wind_cardinal = "SW";
        } else if (wind_direction_int >255 && wind_direction_int <=285) {
            wind_cardinal = "W";
        } else if (wind_direction_int >285 && wind_direction_int <=315) {
            wind_cardinal = "NW";
        }
        return wind_cardinal;

    }
}



