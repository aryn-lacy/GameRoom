package data.game.scrapper;

import data.game.GameEntry;
import data.game.GameGenre;
import ui.Main;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by LM on 03/07/2016.
 */
public class IGDBScrapper {

    public static void main(String[] args) throws ConnectTimeoutException {
        JSONArray bf4_results = searchGame("The witcher 3");
        ArrayList list = new ArrayList();
        list.add(bf4_results.getJSONObject(0).getInt("id"));
        JSONArray bf4_data = getGamesData(list);
        System.out.println(bf4_data);

        /*ArrayList<Integer> list = new ArrayList();
        list.add(12);
        list.add(31);
        JSONArray genres_data = getGenresData(list);
        System.out.println(genres_data);
        System.out.println(GameGenre.getGenreFromIGDB(getGenreName(12,genres_data)));*/


    }

    public static String getYear(int id, JSONArray gamesData) {
        ArrayList<String> years = new ArrayList<>();
        try {
            for (Object obj : gamesData.getJSONObject(indexOf(id, gamesData)).getJSONArray("release_dates")) {
                //Windows platform is number 6
                //if (((JSONObject) obj).getInt("platform") == 6) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                try {
                    years.add(sdf.format(new Date((long) ((JSONObject) obj).getLong("date"))));
                } catch (JSONException je) {
                    if (!je.toString().contains("date")) {
                        je.printStackTrace();
                    }
                }
                //}
            }
            years.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            return ((years.size() > 0) ? years.get(0) : "");
        }catch (JSONException jse){
            if(jse.toString().contains("not found")){
                Main.LOGGER.error("Year not found");
            }else{
                jse.printStackTrace();
            }
        }
        return "";
    }

    public static int indexOf(int id, JSONArray data) {
        int i = 0;
        for (Object obj : data) {
            if (((JSONObject) obj).getInt("id") == id) {
                return i;
            }
            i++;
        }
        return -1;
    }
    public static String[] getScreenshotHash(JSONObject jsob){
        JSONArray screenshotsArray = jsob.getJSONArray("screenshots");
        String[] result  = new String[screenshotsArray.length()];
        for(int i = 0; i< screenshotsArray.length(); i++){
            result[i]=screenshotsArray.getJSONObject(i).getString("cloudinary_id");
        }
        return result;
    }
    public static String getCoverImageHash(JSONObject jsob){
        String cloudinary_id = jsob.getJSONObject("cover").getString("cloudinary_id");
        return cloudinary_id;
    }
    public static String[] getScreenshotHash(int id, JSONArray gamesData) {
        return getScreenshotHash(gamesData.getJSONObject(indexOf(id, gamesData)));
    }
    public static String getCoverImageHash(int id, JSONArray gamesData) {
        return getCoverImageHash(gamesData.getJSONObject(indexOf(id, gamesData)));
    }
    /*public static GameEntry getEntry(int id){
        JSONArray gamesData = getGamesData(new ArrayList<>(id));
        return getEntry(gamesData.getJSONObject(indexOf(id,gamesData)));
    }*/

    public static GameEntry getEntry(JSONObject game_data) {
        GameEntry entry = new GameEntry(game_data.getString("name"));
        entry.setSavedLocaly(false);

        try {
            entry.setDescription(game_data.getJSONObject("esrb").getString("synopsis"));
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no synopsis");
            }else{
                je.printStackTrace();
            }
        }

        try {
            ArrayList<String> years = new ArrayList<>();
            for (Object obj : game_data.getJSONArray("release_dates")) {
                //Windows platform is number 6
                //if (((JSONObject) obj).getInt("platform") == 6) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    years.add(sdf.format(new Date((long) ((JSONObject) obj).getLong("date"))));
                //}
            }
            years.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            entry.setYear(years.get(0));
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no year");
            }else{
                je.printStackTrace();
            }
        }
        try {
            String publishers = "";
            int publishersNumber = game_data.getJSONArray("publishers").length();
            ArrayList<Integer> companiesIDS = new ArrayList<>();
            for (int i = 0; i < publishersNumber; i++) {
                companiesIDS.add(game_data.getJSONArray("publishers").getInt(i));
                //System.out.println("TEST :"+ game_data.getJSONArray("publishers").getInt(i));
            }
            JSONArray companiesData = getCompaniesData(companiesIDS);

            for (int i = 0; i < publishersNumber; i++) {
                publishers += getCompanyName(game_data.getJSONArray("publishers").getInt(i),companiesData);
                if (i != publishersNumber - 1) {
                    publishers += ", ";
                }
            }
            entry.setPublisher(publishers);
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no publishers");
            }else{
                je.printStackTrace();
            }
        }
        try {
            int genresNumber = game_data.getJSONArray("genres").length();
            ArrayList<Integer> genresIDS = new ArrayList<>();
            for (int i = 0; i < genresNumber; i++) {
                genresIDS.add(game_data.getJSONArray("genres").getInt(i));
            }
            JSONArray genresData = getGenresData(genresIDS);

            GameGenre[] gameGenres = new GameGenre[genresNumber];
            for (int i = 0; i < genresNumber; i++) {
                gameGenres[i] = GameGenre.getGenreFromIGDB(getGenreName(genresIDS.get(i),genresData));
            }
            entry.setGenres(gameGenres);
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no developers");
            }else{
                je.printStackTrace();
            }
        }
        try {
            String developers = "";
            int developersNumber = game_data.getJSONArray("developers").length();
            ArrayList<Integer> companiesIDS = new ArrayList<>();
            for (int i = 0; i < developersNumber; i++) {
                companiesIDS.add(game_data.getJSONArray("developers").getInt(i));
            }
            JSONArray companiesData = getCompaniesData(companiesIDS);

            for (int i = 0; i < developersNumber; i++) {
                developers += getCompanyName(game_data.getJSONArray("developers").getInt(i),companiesData);
                if (i != developersNumber - 1) {
                    developers += ", ";
                }
            }
            entry.setDeveloper(developers);
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no developers");
            }else{
                je.printStackTrace();
            }
        }
        try {
            entry.setIgdb_imageHash(0, IGDBScrapper.getCoverImageHash(game_data));
            String[] screenshotsHashes = IGDBScrapper.getScreenshotHash(game_data);
            for(int i = 0; i<screenshotsHashes.length;i++){
                entry.setIgdb_imageHash(i+1,screenshotsHashes[i]);
            }
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no cover");
            }else{
                je.printStackTrace();
            }
        }
        try {
            entry.setIgdb_id(game_data.getInt("id"));
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no id");
            }else{
                je.printStackTrace();
            }
        }
        try {
            entry.setAggregated_rating(game_data.getInt("aggregated_rating"));
        } catch (JSONException je) {
            if(je.toString().contains("not found")){
                Main.LOGGER.warn(entry.getName()+" : no aggregated_rating");
            }else{
                je.printStackTrace();
            }
        }

        return entry;
    }

    public static JSONArray searchGame(String gameName) throws ConnectTimeoutException{
        gameName = gameName.replace(' ', '+');
        try {
            HttpResponse<String> response = Unirest.get("https://igdbcom-internet-game-database-v1.p.mashape.com/games/?fields=name&limit=10&offset=0&search=" + gameName)
                    .header("X-Mashape-Key", "8nsMgKEZ37mshwMwg2TC3Y3FYJRGp15lZycjsnduYWVMRNN8e5")
                    .header("Accept", "application/json")
                    .asString();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getRawBody(), "UTF-8"));
            String json = reader.readLine();
            reader.close();
            JSONTokener tokener = new JSONTokener(json);
            return new JSONArray(tokener);
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JSONObject getGameData(int id) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(id);
        return getGamesData(list).getJSONObject(0);
    }
    public static JSONArray getGenresData(Collection<Integer> ids){
        try {
            String idsString = "";
            for (Integer id : ids) {
                idsString += id + ",";
            }
            HttpResponse<String> response = Unirest.get("https://igdbcom-internet-game-database-v1.p.mashape.com/genres/" + idsString.substring(0, idsString.length() - 1) + "?fields=name")
                    .header("X-Mashape-Key", "8nsMgKEZ37mshwMwg2TC3Y3FYJRGp15lZycjsnduYWVMRNN8e5")
                    .header("Accept", "application/json")
                    .asString();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getRawBody(), "UTF-8"));
            String json = reader.readLine();
            reader.close();
            JSONTokener tokener = new JSONTokener(json);
            return new JSONArray(tokener);
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JSONArray getGamesData(Collection<Integer> ids) {
        try {
            String idsString = "";
            for (Integer id : ids) {
                idsString += id + ",";
            }
            HttpResponse<String> response = Unirest.get("https://igdbcom-internet-game-database-v1.p.mashape.com/games/" + idsString.substring(0, idsString.length() - 1) + "?fields=name,release_dates,esrb.synopsis,genres,aggregated_rating,cover,developers,publishers,screenshots")
                    .header("X-Mashape-Key", "8nsMgKEZ37mshwMwg2TC3Y3FYJRGp15lZycjsnduYWVMRNN8e5")
                    .header("Accept", "application/json")
                    .asString();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getRawBody(), "UTF-8"));
            String json = reader.readLine();
            reader.close();
            JSONTokener tokener = new JSONTokener(json);
            return new JSONArray(tokener);
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String getGenreName(int id, JSONArray genresData) {
        try {
            return genresData.getJSONObject(indexOf(id, genresData)).getString("name");
        } catch (JSONException je) {
            je.printStackTrace();
            return "";
        }
    }

    private static String getCompanyName(int id, JSONArray companiesData) {
        try {
            return companiesData.getJSONObject(indexOf(id, companiesData)).getString("name");
        } catch (JSONException je) {
            je.printStackTrace();
            return "";
        }
    }

    private static JSONArray getCompaniesData(Collection<Integer> ids) {
        String idsString = "";
        for (Integer id : ids) {
            idsString += id + ",";
        }
        try {
            HttpResponse<String> response = Unirest.get("https://igdbcom-internet-game-database-v1.p.mashape.com/companies/" + idsString.substring(0, idsString.length() - 1) + /*"?fields=*"+*/ "?fields=name")
                    .header("X-Mashape-Key", "8nsMgKEZ37mshwMwg2TC3Y3FYJRGp15lZycjsnduYWVMRNN8e5")
                    .header("Accept", "application/json")
                    .asString();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getRawBody(), "UTF-8"));
            String json = reader.readLine();
            reader.close();
            JSONTokener tokener = new JSONTokener(json);
            return new JSONArray(tokener);
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}