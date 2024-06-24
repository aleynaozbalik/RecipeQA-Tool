package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;


public class YemekTarifiGUI {
    private JFrame frame;
    private JComboBox<String> questionComboBox;
    private JTextField answerTextField;
    private JPanel answerPanel;
    private JTextArea resultTextArea;

    public YemekTarifiGUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Turkish Recipe QA Tool");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        frame.getContentPane().add(questionPanel);

        JLabel lblSelectQuestion = new JLabel("Soru Seçin:");
        questionPanel.add(lblSelectQuestion);

        questionComboBox = new JComboBox<>();
        for (int i = 0; i < Sorular.sorularMenu.length; i++) {
            questionComboBox.addItem(Sorular.sorularMenu[i][1]);
        }
        questionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel newAnswerPanel = createAnswerPanel(questionComboBox.getSelectedIndex());
                frame.getContentPane().remove(answerPanel);
                frame.getContentPane().add(newAnswerPanel, 1);
                answerPanel = newAnswerPanel;
                frame.revalidate();
                frame.repaint();
            }
        });
        questionPanel.add(questionComboBox);

        answerPanel = createAnswerPanel(0);
        frame.getContentPane().add(answerPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        frame.getContentPane().add(buttonPanel);

        JButton btnSearch = new JButton("Seç");
        btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchRecipes();
            }
        });
        buttonPanel.add(btnSearch);

        JLabel resultLabel = new JLabel("Eşleşen Tarifler:");
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        frame.getContentPane().add(resultLabel);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        frame.getContentPane().add(resultPanel);

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultPanel.add(scrollPane, BorderLayout.CENTER);


        frame.validate();
        frame.repaint();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void searchRecipes() {
        int selectedQuestionIndex = questionComboBox.getSelectedIndex();
        String answer = answerTextField.getText();

        answer = normalizeAndLowercase(answer);

        Map<String, String> results = new HashMap<>();

        try (Connection connection = DbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT isim, cevap FROM yemektarif WHERE soru = ? AND LOWER(cevap) LIKE LOWER(?)")) {
            statement.setString(1, Sorular.sorularMenu[selectedQuestionIndex][1]);
            statement.setString(2, "%" + answer + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String recipeName = resultSet.getString("isim");
                String cevap = resultSet.getString("cevap");
                results.put(recipeName, cevap);
            }

            resultTextArea.setText("");
            if (results.isEmpty()) {
                resultTextArea.append("Bu kriterlere uygun bir tarif bulunamadı.");
            } else {
                if (results.size() > 1) {
                    String[] recipeNames = results.keySet().toArray(new String[0]);
                    String selectedRecipeName = (String) JOptionPane.showInputDialog(
                            frame,
                            "Birden fazla tarif bulundu, lütfen birini seçin:",
                            "Tarif Seçimi",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            recipeNames,
                            recipeNames[0]);
                    if (selectedRecipeName != null) {
                        resultTextArea.setText(selectedRecipeName + " - " + results.get(selectedRecipeName));
                    }
                } else {
                    String recipeName = results.keySet().iterator().next();
                    resultTextArea.setText(recipeName + " - " + results.get(recipeName));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createAnswerPanel(int selectedQuestionIndex) {
        JPanel answerPanel = new JPanel();
        answerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel lblAnswer = new JLabel();

        switch (selectedQuestionIndex) {
            case 0:
                lblAnswer.setText("Dakika giriniz:");
                break;
            case 1:
                lblAnswer.setText("Kişi sayısını giriniz:");
                break;
            case 2:
                lblAnswer.setText("Malzemeler:");
                break;
            case 3:
                lblAnswer.setText("Yemek tarifi ismini giriniz:");
                break;
            default:
                lblAnswer.setText("Cevap:");
                break;
        }

        answerPanel.add(lblAnswer);

        JTextField answerTextField = new JTextField();
        answerTextField.setColumns(10);
        this.answerTextField = answerTextField;
        answerPanel.add(answerTextField);

        return answerPanel;
    }

    private static String normalizeAndLowercase(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return normalized.toLowerCase();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    YemekTarifiGUI window = new YemekTarifiGUI();
                    window.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
