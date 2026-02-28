package tungtt.Console;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class JsonConsole {
    private JsonConsole() {
    }

    private static class Holder {
        final static Gson gson = new Gson();
    }

    public static String toJson(JsonObject root) {
        return Holder.gson.toJson(root);
    }

    public static JsonObject stringToObject(String jsonString) {
        return JsonParser.parseString(new String(jsonString)).getAsJsonObject();
    }

    public static String objectToString(JsonObject jsonObject, String jsonKey) {
        return jsonObject.get(jsonKey).getAsString();
    }

}
