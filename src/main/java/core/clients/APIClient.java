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
        String url = baseUrl + ApiEndpoints.BOOKING.getPath();
        System.out.println("Отправка GET-запроса на URL: " + url);
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
    // GET-запрос на эндпоинт /bookingId
    public Response getBookingId(int id) {
        String url = baseUrl + ApiEndpoints.BOOKING.getPath() + "/" + id;
        System.out.println("Отправка GET-запроса на URL: " + url);
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/" + id)  // Используем новый метод getBookingPath
                .then()
                .statusCode(200) // Можно изменить статусный код в зависимости от того, что ожидается
                .extract()
                .response();
    }
    // GET-запрос на эндпоинт /bookingId
    public Response getBookingIdSecond(int bookingId) {
        String url = baseUrl + ApiEndpoints.BOOKING.getPath() + "/" + bookingId;
        System.out.println("Отправка GET-запроса на URL: " + url);
        return getRequestSpec()
                .pathParam("id", bookingId)
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/{id}")  // Используем новый метод getBookingPath
                .then()
                .statusCode(200) // Можно изменить статусный код в зависимости от того, что ожидается
                .extract()
                .response();
    }
}
