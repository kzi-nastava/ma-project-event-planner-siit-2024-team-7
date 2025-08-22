package rs.ac.uns.eventplanner.team7.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodingHelper {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public static LatLng getCoordinates(String country, String city, String street, String houseNumber) {
        try {
            String address = String.format("%s %s, %s, %s", houseNumber, street, city, country);
            String url = NOMINATIM_URL + "?q=" + java.net.URLEncoder.encode(address, "UTF-8") + "&format=json&limit=1";

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "EventPlannerApp") // Nominatim requires User-Agent
                    .build();

            Response response = client.newCall(request).execute();
            String body = response.body().string();

            JSONArray array = new JSONArray(body);
            if (array.length() > 0) {
                JSONObject obj = array.getJSONObject(0);
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");
                return new LatLng(lat, lon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // or handle error
    }

    public static class LatLng {
        public final double lat;
        public final double lon;

        public LatLng(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
