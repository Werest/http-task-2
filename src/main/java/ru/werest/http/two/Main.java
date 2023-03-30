package ru.werest.http.two;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static final String REMOTE_SERVICE_URL =
            "https://api.nasa.gov/planetary/apod?api_key=kZLp9QVv84Wa3I2OsVOmwe8Y1Rtir8Z8NwVaaaXc";

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(5000)
                                .setSocketTimeout(30000)
                                .setRedirectsEnabled(false)
                                .build()
                ).build();

        CloseableHttpResponse response = client.execute(new HttpGet(REMOTE_SERVICE_URL));

        NasaResponse nasaResponse = objectMapper.readValue(
                response.getEntity().getContent(),
                NasaResponse.class);

        Map<CloseableHttpResponse, String> closeableHttpResponseList = new HashMap<>();

        closeableHttpResponseList.put(client.execute(new HttpGet(nasaResponse.getHdurl())), nasaResponse.getHdurl());
        closeableHttpResponseList.put(client.execute(new HttpGet(nasaResponse.getUrl())), nasaResponse.getUrl());

        for (Map.Entry<CloseableHttpResponse, String> entry : closeableHttpResponseList.entrySet()) {
            byte[] img = entry.getKey().getEntity().getContent().readAllBytes();
            String imgName = new File(entry.getValue()).getName();

            try (FileOutputStream fileOutputStream        = new FileOutputStream(imgName);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                bufferedOutputStream.write(img);
                System.out.println("Файл " + imgName + " сохранён в корень!");
            } catch (IOException exception) {
                throw new RuntimeException(exception.getMessage());
            }
        }


    }
}