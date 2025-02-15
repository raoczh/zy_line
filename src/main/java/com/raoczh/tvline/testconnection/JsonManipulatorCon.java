package com.raoczh.tvline.testconnection;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonManipulatorCon {

    public static void main(String[] args) {
        try {
            // 读取JSON文件
            File file = new File("d:/TestWorkSpace/Tvbox1/tvbox_line.json");
            FileReader fileReader = new FileReader(file);
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            while ((i = fileReader.read()) != -1) {
                stringBuilder.append((char) i);
            }
            fileReader.close();

            // 解析JSON字符串
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            JSONArray urlsArray = jsonObject.getJSONArray("urls");

            // 过滤掉url以https://agit.ai/开头的对象
            JSONArray validUrlsArray = new JSONArray();
            HashSet<String> seenUrls = new HashSet<>(); // 用于跟踪已经添加到数组中的URL

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS) // 设置连接超时时间
                    .readTimeout(15, TimeUnit.SECONDS) // 设置读取超时时间
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build();

            for (int j = 0; j < urlsArray.length(); j++) {
                JSONObject urlObject = urlsArray.getJSONObject(j);
                String urlString = urlObject.getString("url");
                // 检查URL是否符合条件，并且没有被添加过
                if (!seenUrls.contains(urlString)) {
                    try {
                        Request request = new Request.Builder()
                                .url(urlString)
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (response.code() < 400 && response.body() != null) {
//                                String contentType = response.header("Content-Type");
//                                if (contentType != null && contentType.startsWith("application/json")) {
//                                    String responseBody = response.body().string();
                                // 尝试解析JSON
//                                    new JSONObject(responseBody); // 如果不是有效的JSON，这里会抛出异常

                                // 如果JSON有效，添加到数组中
                                validUrlsArray.put(urlObject);
                                System.out.printf("添加URL: %s，共有数据：%d条。%n", urlString, validUrlsArray.length());
                                seenUrls.add(urlString);
//                                } else {
//                                    System.out.println("URL返回的不是JSON: " + urlString);
//                                }
                            } else {
                                System.out.println("URL获取资源失败: " + urlString);
                            }
                        }
                    } catch (Exception e) {
                        // URL无法连接或返回的不是有效的JSON，忽略此URL
                        System.out.println("URL无效或不是JSON: " + urlString);
                    }
                }
            }

            // 更新JSON对象
            jsonObject.put("urls", validUrlsArray);

            // 将修改后的JSON写回文件
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(jsonObject.toString(4)); // 使用缩进格式化输出
            fileWriter.close();

            System.out.println("操作成功完成。共有数据：" + validUrlsArray.length() + "条。");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
