package core.clients;
import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIClient {
    private final String baseUrl;
    public APIClient() {

        this.baseUrl = determineBaseUrl();
    }
    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: " + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration file:" + configFileName, e);
        }
        return properties.getProperty("baseUrl"); // извлекает значение по ключу "baseUrl" из объекта Properties.
    }
    // Настройка базовых параметров HTTP-запросов
    private RequestSpecification getRequestSpec(){
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Content-type", "application/json")
                .header("Accept", "application/json");
    }
    // GET-запрос на эндпоинт /ping
    public Response ping(){
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath())
                .then()
                .statusCode(201)
                .extract()
                .response();
    }
    // GET-запрос на эндпоинт /booking
    public Response getBooking(){
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
    // GET-запрос на эндпоинт /booking
    public Response getBookingId(){
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKINGID.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
}
