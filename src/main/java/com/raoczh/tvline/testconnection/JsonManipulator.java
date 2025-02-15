package com.raoczh.tvline.testconnection;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonManipulator {

    public static void main(String[] args) {
        try {
            // 读取JSON文件
            File file = new File("d:/TestWorkSpace/Tvbox1/tvbox_line18.json");
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
            JSONArray filteredUrlsArray = new JSONArray();
            HashSet<String> seenUrls = new HashSet<>(); // 用于跟踪已经添加到数组中的URL
            for (int j = 0; j < urlsArray.length(); j++) {
                JSONObject urlObject = urlsArray.getJSONObject(j);
                String url = urlObject.getString("url");
                // 检查URL是否符合条件，并且没有被添加过
                if (!url.contains("jihulab.com") && !seenUrls.contains(url)) {
                    filteredUrlsArray.put(urlObject);
                    seenUrls.add(url); // 将URL添加到已见集合中
                }
            }

            // 将JSONArray转换为List<JSONObject>
            List<JSONObject> urlList = new ArrayList<>();
            for (int t = 0; t < filteredUrlsArray.length(); t++) {
                urlList.add(filteredUrlsArray.getJSONObject(t));
            }
            urlList.sort((o1, o2) -> {
                // 确保o1和o2都是JSONObject类型，并且url字段存在
                String url1 = (o1 != null) ? o1.optString("url", "") : "";
                String url2 = (o2 != null) ? o2.optString("url", "") : "";
                return url1.compareTo(url2); // 使用String的compareTo方法进行字典序排序
            });
            filteredUrlsArray = new JSONArray(urlList); // 将排序后的List<JSONObject>转换回JSONArray

            // 转换URL格式
//            Pattern pattern = Pattern.compile("^https://github\\.moeyy\\.xyz/https://raw\\.githubusercontent\\.com/(.*)$", Pattern.CASE_INSENSITIVE);
            Pattern pattern = Pattern.compile("^https://github\\.(?:moeyy\\.xyz|ednovas\\.xyz)/https://raw\\.githubusercontent\\.com/(.*)$", Pattern.CASE_INSENSITIVE);
            for (int j = 0; j < filteredUrlsArray.length(); j++) {
                JSONObject urlObject = filteredUrlsArray.getJSONObject(j);
                String url = urlObject.getString("url");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    String newUrl = "https://fastly.jsdelivr.net/gh/" + matcher.group(1).replaceFirst("/main/", "@main/");
                    System.out.println(String.format("%s -> %s", url, newUrl)); // 打印转换前后的URL
                    urlObject.put("url", newUrl); // 更新URL
                }
            }

            // 更新JSON对象
            jsonObject.put("urls", filteredUrlsArray);

            // 将修改后的JSON写回文件
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(jsonObject.toString(4)); // 使用缩进格式化输出
            fileWriter.close();

            System.out.println("操作成功完成。共有数据：" + filteredUrlsArray.length() + "条。");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
