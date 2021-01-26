//this program is connected to a chat bot and every time the user requests information it calls the api of the required site and returns the information
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jibble.pircbot.PircBot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBot {
    public static void main(String[] args) throws Exception {
        bot ChatBot = new bot();
        ChatBot.setVerbose(true);
        ChatBot.connect("irc.freenode.net"); //web interface
        ChatBot.joinChannel("#testChannel"); // Name of channel in this case “#testChannel”
// default message
        ChatBot.sendMessage("#testChannel", "Hey! Use keywords 'weather' or 'definition' to get the weather of a place or the definition of a word!");
    }
}
//main logic of pircbot
class bot extends PircBot
{
    static final String query = (" "); //word
    static final String searchedWord = (" "); //location

    final Pattern regex = Pattern.compile("(\\d{20})"); // to get the location/word
    String temperature;
    String definition;
    //constructor
    public bot(){
        this.setName("myChatBot"); // the name of the bot
    }
//read message from the channel
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
// read the message
        if (message.contains("Hello")) { //if user sends chat "hello" reply is set to be hey "sender"
// send a message back to the channel
            sendMessage(channel, "Hey " + sender + "! ");
        }
        if (message.contains("weather")) { //requesting weather
// call the weather API
            String location = searchedWord;
            String[] request = message.split(" ");
            if (request.length >= 2) //for ex if request = weather {cityName}
            {
                if (request[0].equals("weather")) //the first element = "weather'
                {
                    location = request[1]; //so the location will be the second element
                } else {
                    location = request[0];
                }
            } else {
                Matcher matcher = regex.matcher(message);
                if (matcher.find()) //find the requested location
                {
                    location = matcher.group(1); //look for match of location provided
                } else //if not found
                {
                    sendMessage(channel, "Unable to determine location"); //if the location provided is not found
                }
            }
            temperature = startWebRequest(location);
            sendMessage(channel, "Hey " + sender + "! " + temperature); //displaying to the sender the requested weather
        }
        if(message.contains("definition")) {  //while searching for a definition
            //call dictionary api
            String word = query;
            String[] search = message.split(" "); //if search input was in the format definition <word>
            if (search.length >= 2) {
                if (search[0].equals("definition")) {  //and the first entry is "definition
                    word = search[1];  //the second entry is the requested word
                } else {
                    word = search[0];
                }
            }else {
                Matcher matcher = regex.matcher(message); //finding a matching entry
                if(matcher.find()) {
                    word = matcher.group(1);
                } else {
                    sendMessage(channel, "Not Found"); //if it is not found
                }
            }
            definition =  wbeRequest(word);
            sendMessage(channel, "Hey " + sender + "! " + definition); //displaying result
        }
    } // end of class onMessage

    static String wbeRequest(String word_id) {
        final String app_id = "b2ce5fcb"; //  Application ID from Oxford dictionary API
        final String app_key = "6c27b8b7b3fd61c29501076883a8d582"; //  Application Key from Oxford dictionary API
        final String lang = "en-gb";
        String dictionaryURL = "https://od-api.oxforddictionaries.com/api/v2/entries/" + lang + "/"+word_id.toLowerCase();
        StringBuilder result1 = new StringBuilder();
        try {
            URL url1 = new URL(dictionaryURL);
            HttpURLConnection connct = (HttpURLConnection) url1.openConnection();
            connct.setRequestProperty("Accept", "application/json");
            connct.setRequestProperty("app_id", app_id);
            connct.setRequestProperty("app_key", app_key);
            BufferedReader rd = new BufferedReader(new InputStreamReader(connct.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result1.append(line);
            }
            rd.close();
            return parseJSON(result1.toString());
        }
        catch (Exception excep) {
            return "Error! Exception: " + excep;
        }
    } // end of wbeRequest function

    static String startWebRequest(String ciyName) {
        //String weatherURL2 = "http://api.openweathermap.org/data/2.5/weather?zip="+zipcode+"&appid=669fd60465c36c91182fa5766beb1b7b"; //Application ID from OpenWeather API
        //String weatherURL1 = "http://api.openweathermap.org/data/2.5/forecast/hourly?q="+ciyName+"&appid=669fd60465c36c91182fa5766beb1b7b";
        String weatherURL = "http://api.openweathermap.org/data/2.5/weather?q="+ciyName+"&appid=669fd60465c36c91182fa5766beb1b7b";
        StringBuilder result = new StringBuilder();
        try
        {
            URL url = new URL(weatherURL);
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            connect.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null)
            {
                result.append(line);
            }
            rd.close();
            return parseJson(result.toString());
        }
        catch(Exception e)
        {return "Error! Exception: " + e;} //display the reason for error or the exception
    } // end of startWebRequest
    static String parseJSON(String json) { //parse method for dictionary
        String definition = null;
        String examples = null;
        String syn = null;
        try {
            JSONObject main = new JSONObject(json);
            JSONArray results = main.getJSONArray("results");
            JSONObject lexical = results.getJSONObject(0);
            JSONArray lexi = lexical.getJSONArray("lexicalEntries");
            JSONObject entries = lexi.getJSONObject(0);
            JSONArray ent = entries.getJSONArray("entries");
            JSONObject senses = ent.getJSONObject(0);
            JSONArray sen = senses.getJSONArray("senses");
            JSONObject d = sen.getJSONObject(0);
            JSONArray de = d.getJSONArray("definitions");
            definition = d.getString("definitions");
            examples = d.getString("examples");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "The definition of your word: " + definition + " Examples for the word: " + examples ;
    }
    static String parseJson(String json) { //parse method for weather
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        String cityName = object.get("name").getAsString();
        //  String state = object.get("state").getAsString();
        //  String country = object.get("country").getAsString();

        JsonObject main = object.getAsJsonObject("main");
        JsonObject coord = object.getAsJsonObject("coord");
        JsonObject wind = object.getAsJsonObject("wind");

        double temp = main.get("temp").getAsDouble();
        temp = (temp * 1.8) - 459.67;
        double miniTemp = main.get("temp_min").getAsDouble();
        miniTemp = (miniTemp * 1.8) - 459.67;  //temp is by default in K, changing K to F
        double maxiTemp = main.get("temp_max").getAsDouble();
        maxiTemp = (maxiTemp * 1.8) - 459.67;  //temp is by default in K, changing K to F
        double feelsLike = main.get("feels_like").getAsDouble();
        feelsLike = (feelsLike * 1.8)- 459.67;
        double pres = main.get("pressure").getAsDouble();
        double humid = main.get("humidity").getAsDouble();

        double longi = coord.get("lon").getAsDouble();
        double lati = coord.get("lat").getAsDouble();

        double windSpeed = wind.get("speed").getAsDouble();

        DecimalFormat df = new DecimalFormat("####0.0");

        // Bot output
        return "Information for " + cityName + ": Longitude: " +  df.format(longi) + " & Latitude: " + df.format(lati) + " Current temperature is: " + df.format(temp) + "˚F " + "with a high of " + df.format(maxiTemp) +
                "˚F " + "and a low of " + df.format(miniTemp) + "˚F. " + " Feels Like: " + df.format(feelsLike) + "˚F. " + "The wind speed is: " + df.format(windSpeed) + ". Pressure: " + df.format(pres) + ". Humidity: " + df.format(humid); //displaying all the results
        // end of parseJson
    }
}




