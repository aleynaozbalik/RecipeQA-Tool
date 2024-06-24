package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.*;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

public class Main {


    private static final int WAIT_TIME_MS = 2000;

    public static void main(String[] args) {
        String[] urls = {
                "https://www.nefisyemektarifleri.com/etli-enginar-girit-usulu/",
               "https://www.nefisyemektarifleri.com/pirasa-yemegi-nasil-yapilir/",
                "https://www.nefisyemektarifleri.com/taze-bakla-yemegi-tarifi/#google_vignette",
                "https://www.nefisyemektarifleri.com/pirincli-kabak-yemegi-yapimi/",
                "https://www.nefisyemektarifleri.com/video/kiymali-kabak-yemegi/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-kabak-tarifi/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-kereviz-yemegi-videolu/",
                "https://www.nefisyemektarifleri.com/yogurtlu-kereviz-salatasi-2606641/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-dolmabiber-tarifi/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-kabak-dolmasi/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-taze-fasulye-tarifi-196168/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-taze-bakla/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-barbunya-tarifi-223409/",
                "https://www.nefisyemektarifleri.com/patatesli-havuclu-bezelye-yemegi-tarifi/",
                "https://www.nefisyemektarifleri.com/sade-bezelye-5793037/",
                "https://www.nefisyemektarifleri.com/etli-bezelye-yemegi-3245793/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-patlican-yemegi-yapimi/",
                "https://www.nefisyemektarifleri.com/patlican-musakka-yapilisi/",
                "https://www.nefisyemektarifleri.com/karni-yarik-tarifi/",
                "https://www.nefisyemektarifleri.com/ali-nazik-tarifi/",
                "https://www.nefisyemektarifleri.com/15-dakikada-tahinli-nohut-salatasi-videolu/",
                "https://www.nefisyemektarifleri.com/pazi-yemegi-tarifi/",
                "https://www.nefisyemektarifleri.com/yogurtlu-semizotu-yemegi/",
                "https://www.nefisyemektarifleri.com/semizotu-salatasi-tarifi/",
                "https://www.nefisyemektarifleri.com/ispanak-yemegi-tarifi/",
                "https://www.nefisyemektarifleri.com/kolay-ispanak-graten-tarifi/#google_vignette",
                "https://www.nefisyemektarifleri.com/brokoli-corbasi-tarifi/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-brokoli-salatasi-3872807/",
                "https://www.nefisyemektarifleri.com/karnabahar-salatasi-tarifi/",
                "https://www.nefisyemektarifleri.com/karnabahar-yemegi-yapilisi/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-lezzetli-lahana-sarmasi-videolu/",
                "https://www.nefisyemektarifleri.com/beyaz-lahana-yemegi-1104525/",
                "https://www.nefisyemektarifleri.com/beyaz-lahana-salatasi-yapimi/",
                "https://www.nefisyemektarifleri.com/zeytinyagli-kara-lahana-sarmasi-3439325/",
                "https://www.nefisyemektarifleri.com/yogurtlu-pirasa-kavurmasi-7007517/",
                "https://www.nefisyemektarifleri.com/kabak-mucveri-nasil-yapilir/",
                "https://www.nefisyemektarifleri.com/patates-oturtma-nasil-yapilir/",
                "https://www.nefisyemektarifleri.com/patates-puresi-tarifi/",
                "https://www.nefisyemektarifleri.com/saksuka-tarifi/",
                "https://www.nefisyemektarifleri.com/kuru-borulce-yemegi-278671/",
                "https://www.nefisyemektarifleri.com/taze-borulce-yemegi/",
                "https://www.nefisyemektarifleri.com/kuru-borulce-salatasi/",

        };

        try {
            StringBuilder allData = new StringBuilder();
            int id = 1;
            for (String url : urls) {
                String recipeName = getRecipeName(url);
                Document doc = Jsoup.connect(url).get();
                allData.append(Sorular.getSorular(doc, id, recipeName));
                id++;


                Thread.sleep(WAIT_TIME_MS);
            }
            writeToCSV(allData.toString());
            importToDatabase("RecipesOutput.csv");

            showGUI();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void showGUI() {
        EventQueue.invokeLater(() -> {
            try {
                YemekTarifiGUI window = new YemekTarifiGUI();
                window.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String getRecipeName(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.selectFirst("h1.recipe-name").text();
    }

    private static void writeToCSV(String data) {
        String csvFile = "RecipesOutput.csv";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {
            writer.write("ID,İSİM,SORU,CEVAP\n");
            writer.write(data);
            System.out.println("Veriler başarıyla CSV dosyasına yazıldı.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void importToDatabase(String csvFile) {
        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO yemektarif (id, isim, soru, cevap) VALUES (?, ?, ?, ?)")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", 4);
                if (data.length != 4) {
                    continue;
                }
                int id = Integer.parseInt(data[0]);
                String recipeName = data[1];
                String question = data[2];
                String answer = data[3];
                statement.setInt(1, id);
                statement.setString(2, recipeName);
                statement.setString(3, question);
                statement.setString(4, answer);
                statement.executeUpdate();
            }
            System.out.println("CSV dosyasındaki veriler başarıyla veritabanına aktarıldı.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}