package com.tpservers.Core;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import com.google.gson.JsonObject;

import tungtt.Console.Console;
import tungtt.Console.JsonConsole;

public class RequestCore {

    private RequestCore() {
    }

    private static class Holder {

        static final RequestCore INSTANCE = new RequestCore();
    }

    public static RequestCore getInstance() {
        return Holder.INSTANCE;
    }

    public static String post(String url, JsonObject headers, JsonObject payload) throws Exception {

        /* ========= CONNECTION ========= */

        HttpURLConnection connection = null;
        try {

            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            Console.info("Request URL: " + url);
            Console.info("Request Method: " + connection.getRequestMethod());
            Console.info("Request Do Output: " + connection.getDoOutput());
        } catch (Exception e) {

            Console.error("Request failed");
            Console.error("Request error message: " + e.getMessage());
            e.printStackTrace();
        }

        /* ========= HEADERS ======== */

        if (headers != null) {
            headers.addProperty("Content-Type", "application/json");
            Console.info("[HEADER] COUNT: " + headers.size());
            Console.info("[HEADER] CONTENT-TYPE: " + headers.get("Content-Type").getAsString());
            Console.info("[HEADER] " + JsonConsole.toJson(headers));
            int i = 1;

            for (String key : headers.keySet()) {

                connection.setRequestProperty(key, headers.get(key).getAsString());
                Console.info("[HEADER] " + i++ + ". " + key + ": " + headers.get(key).getAsString());
            }
        }

        /* ========= PAYLOAD ========= */

        try (OutputStream outStream = connection.getOutputStream()) {

            outStream.write(payload.toString().getBytes());
            Console.info("[PAYLOAD] " + JsonConsole.toJson(payload));
        } catch (Exception e) {

            Console.error("Request failed");
            Console.error("Request error message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        /* ========= RESPONSE ========= */

        try {

            return new String(connection.getInputStream().readAllBytes());
        } catch (Exception e) {

            Console.error("Request failed");
            Console.error("Request error message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
