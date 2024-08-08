package topen.discordbot;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ColorFetcher {
    private ColorFetcher() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger logger = LoggerFactory.getLogger(ColorFetcher.class);

    private static final Random random = new Random();

    public static String fetchColorData(String color) {
        if(color.equals("랜덤") || color.equals("random")){
            int nextInt = random.nextInt(16777215);
            return  "#"+Integer.toHexString(new Color(nextInt).getRGB()).substring(2);
        }
        if(color.matches("^#[a-zA-Z0-9]{6}$")) return color;
        if(color.matches("^[a-zA-Z0-9]{6}$")) return "#"+color;
        try {
            // URL 인코딩
            String encodedColor = URLEncoder.encode(color, StandardCharsets.UTF_8);
            String apiUrl = "https://text2color.com/api/color/" + encodedColor; // 실제 API URL로 변경 필요

            // HTTP GET 요청
            URL url = new URI(apiUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 응답 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // JSON 파싱
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("hex_code");
            } else {
                return "#FFFFFF";
            }
        } catch (Exception e) {
            logger.error("Error parsing color : {}", color);
        }
        return "#FFFFFF";
    }
}
