package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.google.gson.annotations.SerializedName;

/**
 * This application allows the user to randomize a piece of advice (provided by Advice Slip API).
 * Then translates that advice into their desired language (using Apyhub Translate Text API),
 * which can then be mailed to people all over the world (theoretically)!
 */
public class ApiApp extends Application {

    // create Http Client - one can send multiple requests
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    // Google {@code Gson} object for parsing JSON-formatted strings.
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    Stage stage;
    Scene scene;
    VBox root;

    // top layer
    HBox hboxTop;
    Text intro;
    TextField userLanguage;
    Text instructions;

    // middle layer
    VBox vboxMid;
    Text quoteText;
    Text translatedText;

    // bottom layer
    HBox hboxBottom;
    Button quoteButton; // search for chosen quote -- inactive!!
    Button translateButton; // translate button

    // copyright layer
    HBox hboxCredits;
    Text copyright;

    // image layer
    HBox hboxImg;
    Image image = new Image("file:resources/TranslationImage.png");
    ImageView imgView = new ImageView(image);
    int imgWidth = (508 - 100);
    int imgHeight = (369 - 100);

    // API info
    public static final String APYHUB_LINK =
        "https://api.apyhub.com/sharpapi/api/v1/content/translate";

    public static final String QUOTE_LINK = "https://api.adviceslip.com/advice";

    // stage size
    public static final int WIDTH = 700;
    public static final int HEIGHT = 700;

    // Other
    private String languageType; // actual input from user
    private String quote;
    private String newQuote;
    private String content; // input passed for api to translate

    String apiToken = "APY0uhUFkqHqom40aYOrV6HpIX8y0K8sIgs1XMZms00Z3UAoIc8IPwOWl9l8ZU4mN8eiTePD";

    /**
     * Constructs an {@code ApiApp} object.
     * This default (i.e., no argument) constructor is executed in Step 2
     * of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
        root.setPadding(new Insets(10)); // element padding

        root.setStyle("-fx-background-color: #FFE4E1;");

        imgView.setFitWidth(imgWidth);
        imgView.setFitHeight(imgHeight);

        // image layer
        hboxImg = new HBox(10);
        hboxImg.getChildren().add(imgView);
        hboxImg.setAlignment(Pos.CENTER);
        hboxImg.setPadding(new Insets(0, 0, 20, 0));

        // top layer
        hboxTop = new HBox(10);
        hboxTop.setAlignment(Pos.TOP_LEFT);
        intro = new Text("Enter a language");
        userLanguage = new TextField("Spanish");
        hboxTop.getChildren().addAll(intro, userLanguage);
        hboxTop.setPadding(new Insets(0, 0, 20, 0));

        //middle layer
        vboxMid = new VBox(10);
        vboxMid.setAlignment(Pos.TOP_LEFT);
        quoteText = new Text("Quote..."); // TEMP
        quoteText.setWrappingWidth(350);
        translatedText = new Text("Translation...");
        translatedText.setWrappingWidth(350);
        vboxMid.getChildren().addAll(quoteText, translatedText);

        VBox.setVgrow(vboxMid, Priority.ALWAYS);

        // bottom layer
        hboxBottom = new HBox(10);
        hboxBottom.setAlignment(Pos.BOTTOM_LEFT);
        quoteButton = new Button("Generate Quote");
        translateButton = new Button("Translate"); // ERROR IF no input
        hboxBottom.getChildren().addAll(quoteButton, translateButton);
        HBox.setHgrow(quoteButton, Priority.ALWAYS);
        HBox.setHgrow(translateButton, Priority.ALWAYS);

        // copyright layer
        hboxCredits = new HBox(10);
        hboxCredits.setAlignment(Pos.BOTTOM_LEFT);
        copyright = new Text("Translation by ApyHub - Quotes by Advice Slip");
        hboxCredits.getChildren().add(copyright);

        // add to root
        root.getChildren().addAll(hboxImg, hboxTop, vboxMid, hboxBottom, copyright);
        root.setSpacing(20);

    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        System.out.println("init() called");
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        scene = new Scene(root, 500, 700);

        translateButton.setDisable(true); // begin disabled

        this.quoteButton.setOnAction(event -> {
            buttonsOff();
            quoteText.setText("Loading...");

            new Thread(() -> {
                sendQuoteRequest();
                Platform.runLater(() -> buttonsOn());
            }).start();
        });

        this.translateButton.setOnAction(event -> {
            buttonsOff();
            translatedText.setText("Loading...");

            new Thread(() -> {
                sendTranslationRequest();
                Platform.runLater(() -> buttonsOn());
            }).start();
        });

        // setup stage
        stage.setTitle("Polyglot Ponderings!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start


    /**
     * Send/recieve request then retrieve response from Advice Slip API.
     */
    public void sendQuoteRequest() {
        // create request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(QUOTE_LINK))
            .GET()
            .build();

        try {
            // sedn request with same client & get response
            HttpResponse<String> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            // check status
            if (response.statusCode() != 200) {
                quoteText.setText("Failed to get quote: " + response.statusCode());
            }
            // store json response in variable
            String jsonString = response.body();

            // check whether API returned message object or slip object
            if (jsonString.contains("\"slip\"")) {
                QuoteResult jsonResponse = GSON.fromJson(jsonString, QuoteResult.class);
                if (jsonResponse.slip != null) {
                    quoteText.setText(jsonResponse.slip.advice);
                    content = jsonResponse.slip.advice;
                    translateButton.setDisable(false); // turn translate button ON
                } else {
                    quoteText.setText("Unexpected null quote.");
                }

            } else if (jsonString.contains("\"message\"")) {
                QuoteErrorMessage jsonResponseError =
                    GSON.fromJson(jsonString, QuoteErrorMessage.class);
                if (jsonResponseError.text != null) {
                    quoteText.setText("API error: " + jsonResponseError.text); // createdObject.var
                } else {
                    quoteText.setText("Unexpected null quote.");
                }

            } else {
                quoteText.setText("Something weird happened");
            }

        } catch (Exception e) { // DO ALERTS???
            e.printStackTrace();
            quoteButton.setDisable(false);

        } finally {
            try {
                Thread.sleep(2000); // wait 2 sec so same quote is not called
            } catch (InterruptedException e) {
                System.out.println("Sleep went wrong: " + e.getMessage());
                Thread.currentThread().interrupt(); // tell thread sleep was interrupted
            }
            quoteButton.setDisable(false); // turn quote back on regardless
        }
    } // sendQuoteRequest


    /**
     * Send/receive request then retrieve response from Apyhub Translate Text API.
     *
     * @return String either return null or return the url to be used in second request.
     */
    public String translationRequest() {

        String language = this.userLanguage.getText().trim();

        if (language.isEmpty()) { // OR not in options...
            translatedText.setText("Please enter your desired language.");
            buttonsOn();
            return null;
        }

        String fill = String.format("{\"content\":\"%s\",\"language\":\"%s\"}", content, language);
        HttpRequest request = HttpRequest.newBuilder() // PART 1 - submit job
            .uri(URI.create("https://api.apyhub.com//sharpapi/api/v1/content/translate"))
            .POST(BodyPublishers.ofString(fill))
            .setHeader("Content-Type", "application/json")
            .setHeader("apy-token", apiToken)
            .build();

        try { // get job_id in first response
            HttpResponse<String> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 202) {
                translatedText.setText("Failed to connect to API: " + response.statusCode());
                buttonsOn();
                return null;
            }

            String jsonString = response.body();
            TranslationResponse jsonResponse = GSON.fromJson
                (jsonString, TranslationResponse.class);

            // return first response so that the second request can use it
            String statusUrl =
                "https://api.apyhub.com/sharpapi/api/v1/content/translate/job/status//%s"
                .formatted(jsonResponse.jobId);
            return statusUrl;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            translatedText.setText("An error occurred while processing the request");
            buttonsOn();
            return null;
        }
    } // sendTranslationRequest

    /**
     * Poll/check the status of the first request sent.
     *
     * @param statusUrl the statusUrl
     */
    public void pollTranslationRequest(String statusUrl) {
        try {

            for (int i = 0; i < 5; i++) { // limit num polls to 5
                HttpRequest statusRequest = HttpRequest.newBuilder() // send request with job_id
                    .uri(URI.create(statusUrl))
                    .GET()
                    .setHeader("Content-Type", "application/json")
                    .setHeader("apy-token", apiToken)
                    .build();

                HttpResponse<String> responseStatus = // get actual translation in second response
                    HTTP_CLIENT.send(statusRequest, HttpResponse.BodyHandlers.ofString());

                if (responseStatus.statusCode() != 200) {
                    translatedText.setText("Failed: " + responseStatus.statusCode());
                    buttonsOn();
                    return;
                }

                String jsonStringStatus = responseStatus.body();
                TranslationResult jsonResult =
                    GSON.fromJson(jsonStringStatus, TranslationResult.class);
                String statusNow = jsonResult.data.attributes.status;
                String translation = null;

                // if result isnt null then assign translation
                if (jsonResult.data.attributes.result != null) {
                    translation = jsonResult.data.attributes.result.content;
                }

                if ("success".equals(statusNow) && translation != null) {
                    translatedText.setText(translation);
                    buttonsOn();
                    break;
                } else if ("error".equals(statusNow)) {
                    translatedText.setText("Error during translation :(");
                    buttonsOn();
                    break;
                } else {
                    Thread.sleep(5000);
                    continue;
                }
            } // for loop

        } catch (IOException | InterruptedException e) { // return?? idk CHWCK ALERTS BFROM LAST !!
            e.printStackTrace();
            translatedText.setText("An error occurred while processing the request");

        } finally {
            buttonsOn();
        }
    } // getTranslation()

    /**
     * This method contains both methods required to translate the provided quote.
     */
    public void sendTranslationRequest() {

        // send the first request to translation API
        String statusUrl = translationRequest();

        // if returns null, dont run the polling/second request
        if (statusUrl == null) {
            buttonsOn();
            return;
        }

        pollTranslationRequest(statusUrl);
        buttonsOn();

    } // sendTranslationRequest

    /**
     * Turn translate and quote buttons off.
     */
    public void buttonsOff() {
        quoteButton.setDisable(true);
        translateButton.setDisable(true);
    } // buttonsOff

    /**
     * Turn translate and quote buttons on.
     */
    public void buttonsOn() {
        quoteButton.setDisable(false);
        translateButton.setDisable(false);
    } // buttonsOn

    /**
     * Main method to launch program.
     *
     * @param args the arguments.
     */
    public static void main(String[] args) {
        launch(args);
    } // main

} // ApiApp
