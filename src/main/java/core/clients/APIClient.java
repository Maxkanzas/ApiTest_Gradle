package core.clients;
import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;

public class APIClient {
    private final String baseUrl;
    public String token;
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
                .header("Accept", "application/json")
                .filter(addAuthTokenFilter()); // Фильтр для добавления токена
    }
    // Метод для получения токена авторизации
    public void createToken(String username, String password) {
        // Формирование JSON тела для запроса
        String requestBody = String.format("{ \"username\": \"%s\",\"password\": \"%s\" }", username, password);
        // Отправка POST-запроса на эндпоинт для аутентификации и получение токена
        Response response = getRequestSpec()
                .body(requestBody) // Устанавливаем тело запроса
                .when()
                .post(ApiEndpoints.AUTH.getPath()) // POST-запрос на эндпоинт аутентификации
                .then()
                .statusCode(200) // Проверяем, что статус ответа 200 (ОК)
                .extract()
                .response();
        // Извлечение токена из ответа и сохранение в переменной
        token = response.jsonPath().getString("token");
    }
    // Фильтр для добавления токена в заголовок
    private Filter addAuthTokenFilter() {
        return (FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) -> {
            if (token != null) {
                requestSpec.header("Cookie", "token=" + token);
            }
            return ctx.next(requestSpec, responseSpec); // Продолжает выполнениезапроса
        };
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
        Response response = getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/" + id)  // Используем новый метод getBookingPath
                .then()
                .extract()
                .response();
        if (response.getStatusCode() != 200 && response.getStatusCode() != 404) {
            throw new RuntimeException("Неожиданный статус-код: " + response.getStatusCode());
        }
        return response;
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
    // GET-запрос на удаление бронирования
    public Response deleteBooking(int bookingId) {
        String url = baseUrl + ApiEndpoints.BOOKING.getPath() + "/" + bookingId;
        System.out.println("Отправка DELETE-запроса на URL: " + url);
        return getRequestSpec()
                .pathParam("id", bookingId)
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}")  // Используем новый метод getBookingPath
                .then()
                .log().all()
                .statusCode(201) // Можно изменить статусный код в зависимости от того, что ожидается
                .extract()
                .response();
    }
}

