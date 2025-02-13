package com.danny.ewf_service.utils;

import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ImageCheck {

    public boolean isImageLinkAlive(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");  // Changed to GET instead of HEAD
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0 Safari/537.36");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            try {
                connection.connect();
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                connection.disconnect();
            }

        } catch (Exception e) {
            return false;
        }
    }
}