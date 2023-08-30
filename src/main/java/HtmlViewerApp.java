import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;

public class HtmlViewerApp extends Application {
    WebView webView;
    Document document;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("HTML Viewer");
        webView = new WebView();
        // Создаем панель инструментов с кнопками и выпадающим меню
        ToolBar toolbar = new ToolBar();

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> webView.getEngine().executeScript("history.back()"));

        Button forwardButton = new Button("Forward");
        forwardButton.setOnAction(e -> webView.getEngine().executeScript("history.forward()"));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> webView.getEngine().reload());

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("H1", "H2", "H3", "H4", "H5", "H6");
        choiceBox.getSelectionModel().selectFirst();

        Button findButton = new Button("Find");
        findButton.setOnAction(e -> {
            String selectedHeading = choiceBox.getValue();
            // Ваш код для поиска определенного заголовка
        });

        toolbar.getItems().addAll(
                backButton,
                forwardButton,
                refreshButton,
                new Label("Select Heading:"),
                choiceBox,
                findButton
        );

        BorderPane root = new BorderPane(webView);
        root.setTop(toolbar); // Добавьте эту строку
        root.setCenter(webView); // Остальные элементы
        webView.getEngine().setUserStyleSheetLocation(getClass().getResource("disable-scripts.css").toExternalForm());

        try {
//            String url = "https://access.redhat.com/documentation/en-us/red_hat_fuse/7.12/html-single/release_notes_for_red_hat_fuse_7.12/index";
            String url = "https://access.redhat.com/documentation/en-us/red_hat_amq_broker/7.11/html-single/release_notes_for_red_hat_amq_broker_7.11/index";
            document = Jsoup.connect(url).get();
            String htmlCode = document.outerHtml();

//            webView.getEngine().loadContent(htmlCode);
            webView.getEngine().load(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        webView.setOnMouseReleased(event -> {
            String selectedText = (String) webView.getEngine().executeScript("window.getSelection().toString()");
            System.out.println("\n\n\nSelected Text: " + selectedText);

//            String jsCode =
//                    "function getPathTo(element) {" +
//                    "    if (element.tagName === 'BODY') return 'BODY';" +
//                    "    var parentPath = getPathTo(element.parentNode);" +
//                    "    return parentPath + ' > ' + element.tagName + ':nth-child(' + (Array.from(element.parentNode.children)" +
//                    "    .filter(child => child.tagName === element.tagName)" +
//                    "    .indexOf(element) + 1) + ')';" +
//                    "}" +
//                    "var range = window.getSelection().getRangeAt(0); getPathTo(range.startContainer.parentElement);";

            String jsCode =
                    "function getPathTo(element) {" +
                            "    if (element.tagName === 'BODY') return 'BODY';" +
                            "    var parentPath = getPathTo(element.parentNode);" +
                            "    var siblings = Array.from(element.parentNode.children).filter(child => child.tagName === element.tagName);" +
                            "    var index = siblings.indexOf(element) + 1;" +
                            "    return parentPath + ' > ' + element.tagName + ':nth-child(' + index + ')';" +
                            "}" +
                            "var range = window.getSelection().getRangeAt(0); getPathTo(range.startContainer.parentElement);";

            // искать не по твердому пути, а по классам и дивам


            String selectedElementPath = (String) webView.getEngine().executeScript(jsCode);
            System.out.println("Selected Element Path: " + selectedElementPath);

            String elementContent = findElementByPath(selectedElementPath);
            if (elementContent != null) {
                System.out.println("Element Content: " + elementContent);
            } else {
                System.out.println("Element not found.");
            }

            webView.getEngine().executeScript(
                    "document.addEventListener('click', function(event) {" +
                            "   var target = event.target;" +
                            "   if (!['DL'].includes(target.tagName)) {" +
                            "       if (window.getSelection().toString().length > 0) {" +
                            "           var selectedText = window.getSelection().toString();" +
                            "           var input = document.createElement('input');" +
                            "           input.type = 'text';" +
                            "           input.style.color = 'red';" +
                            "           input.value = selectedText;" +
                            "           input.addEventListener('blur', function() {" +
                            "               var span = document.createElement('span');" +
                            "               span.style.color = 'red';" +
                            "               span.textContent = input.value;" +
                            "               var range = window.getSelection().getRangeAt(0);" +
                            "               range.deleteContents();" +
                            "               range.insertNode(span);" +
                            "               input.remove();" +
                            "           });" +
                            "           var range = window.getSelection().getRangeAt(0);" +
                            "           range.deleteContents();" +
                            "           range.insertNode(input);" +
                            "           input.focus();" +
                            "       }" +
                            "   }" +
                            "});"
            );
        });

//        webView.getEngine().executeScript(
//                "document.addEventListener('click', function(event) {" +
//                        "   var target = event.target;" +
//                        "   if (['P', 'LI', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'H7', 'H8', 'H9', 'A'].includes(target.tagName)) {" +
//                        "       if (window.getSelection().toString().length > 0) {" +
//                        "           var selectedText = window.getSelection().toString();" +
//                        "           var input = document.createElement('input');" +
//                        "           input.type = 'text';" +
//                        "           input.style.color = 'red';" +
//                        "           input.style.width = getTextWidth(selectedText) + 'px';" +
//                        "           input.value = selectedText;" +
//                        "           input.addEventListener('blur', function() {" +
//                        "               var span = document.createElement('span');" +
//                        "               span.style.color = 'red';" +
//                        "               span.textContent = input.value;" +
//                        "               var range = window.getSelection().getRangeAt(0);" +
//                        "               range.deleteContents();" +
//                        "               range.insertNode(span);" +
//                        "               input.remove();" +
//                        "           });" +
//                        "           var range = window.getSelection().getRangeAt(0);" +
//                        "           range.deleteContents();" +
//                        "           range.insertNode(input);" +
//                        "           input.focus();" +
//                        "       }" +
//                        "   }" +
//                        "});" +
//                        "function getTextWidth(text) {" +
//                        "    var canvas = document.createElement('canvas');" +
//                        "    var context = canvas.getContext('2d');" +
//                        "    context.font = window.getComputedStyle(document.body).getPropertyValue('font');" +
//                        "    return context.measureText(text).width;" +
//                        "}"
//        );
//    });

        Scene scene = new Scene(root, 1800, 1300);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public String findElementByPath(String elementPath) {
        int var = 0;
        String[] pathParts = elementPath.split(" > ");
        System.out.println("Массив пути: " + Arrays.stream(pathParts).toList());


//        Document document = (Document) webView.getEngine().executeScript("document");
        Element currentElement = document.body();
        ToFile(currentElement.toString(), "start.txt");

        for (String pathPart : pathParts) {
            String tagName = pathPart.split(":nth-child")[0];
            System.out.println("Текущий элемент массива: " + tagName);

//            int childIndex = Integer.parseInt(pathPart.split(":nth-child\\(")[1].replace(")", "")) - 1;
            int childIndex;
            String[] childIndexParts = pathPart.split(":nth-child\\(");
            System.out.println("Имя и глубина текущего элемента: " + Arrays.stream(childIndexParts).toList());
            System.out.println("Количество элементов (есть ли значение в предыдущем массиве): " + childIndexParts.length);

            if (childIndexParts.length >= 2) {
                childIndex = Integer.parseInt(childIndexParts[1].replace(")", "")) - 1;
            } else {
                // Обработка ошибки или установка значения по умолчанию
                childIndex = 0;
            }
            System.out.println("[" + tagName + "]  индекс нужного элемента (число в кортеже): " + childIndex);

            Elements childElements = currentElement.getElementsByTag(tagName); // ошибка тутЁ!!!!!!!!!!!1

            ToFile(childElements.toString(), "current" + Integer.toString(var) + ".txt");
            var++;

            System.out.println("Количество символов внутри текущего элемента: " + childElements.toString().length());

            System.out.println("Массив элементов внутри текущего элемента с этим именем: " + childElements.size() + "   индекс нужного элемента: " + childIndex + "\n");

            if (childElements.size() > childIndex) {
                ToFile(childElements.toString(), "success.txt");
                System.out.println("Success " + pathPart);

                currentElement = childElements.get(childIndex);
            } else {
                // Element not found
                return null;
            }
        }

        return currentElement.text();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void ToFile(String str, String filename) {
        String content = "Это содержимое, которое нужно записать в файл.";

        // Укажите путь к файлу, в который вы хотите записать данные

        try {
            // Создаем объект FileWriter с указанием файла
            FileWriter fileWriter = new FileWriter(filename);

            // Оборачиваем FileWriter в BufferedWriter для более эффективной записи
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Создаем объект PrintWriter для удобной записи данных
            PrintWriter printWriter = new PrintWriter(bufferedWriter);

            // Записываем содержимое в файл
            printWriter.println(str);

            // Закрываем PrintWriter и связанные потоки
            printWriter.close();
            bufferedWriter.close();
            fileWriter.close();

        } catch (IOException e) {
            System.out.println("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }

}