package core.clients;

import core.models.Booking;
import core.models.BookingData;
import core.models.BookingDates;
import core.models.UpdateBookingRequest;
import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;

import static io.restassured.RestAssured.given;

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
    private RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Content-type", "application/json")
                .header("Accept", "application/json")
                .filter(addAuthTokenFilter()); // Фильтр для добавления токена
    }
    // Метод возвращает полностью заполненный объект BookingData для отправки PUT-запроса
    private BookingData prepareBookingData(UpdateBookingRequest request) {
        BookingData updatedBookingData = new BookingData();
        updatedBookingData.setFirstname(request.getFirstname());
        updatedBookingData.setLastname(request.getLastName());
        updatedBookingData.setTotalprice(request.getTotalPrice());
        updatedBookingData.setDepositpaid(request.isDepositPaid());
        updatedBookingData.setAdditionalneeds(request.getAdditionalNeeds());

        BookingDates bookingDates = new BookingDates();
        bookingDates.setCheckin(request.getBookingdates().getCheckin());
        bookingDates.setCheckout(request.getBookingdates().getCheckout());
        updatedBookingData.setBookingdates(bookingDates);

        return updatedBookingData;
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
    public Response ping() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath())
                .then()
                .statusCode(201)
                .extract()
                .response();
    }

    // GET-запрос на эндпоинт /booking
    public Response getBooking() {
        String url = baseUrl + ApiEndpoints.BOOKING.getPath();
        System.out.println("Отправка GET-запроса на URL: " + url + " для получения списка всех бронирований");
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath())
                .then()
                .log().all()
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

    // GET-запрос на создание нового бронирования
    public Response createdBooking(String bookingData) {
        String url = baseUrl + ApiEndpoints.BOOKING.getPath();
        System.out.println("Отправка POST-запроса о создании бронирования на URL: " + url);

        return getRequestSpec()
                .body(bookingData)
                .log().all()
                .when()
                .post(ApiEndpoints.BOOKING.getPath())
                .then()
                .log().all()
                .extract()
                .response();
    }

    // PUT-запрос на обновление данных о бронировании (firstName , totalPrice)
    public Response updateBooking(int bookingId, UpdateBookingRequest request) {
        String url = baseUrl + ApiEndpoints.BOOKING.getPath();
        System.out.println("Отправка PUT-запроса на обновление данных о бронировании на URL: " + url);

        // Подготовка тела запроса
        BookingData updatedBookingData = prepareBookingData(request);

        return getRequestSpec()
                .body(updatedBookingData)
                .when()
                .put(ApiEndpoints.BOOKING.getPath() + "/" + bookingId)
                .then()
                .log().all()
                .extract()
                .response();
    }
    public Response partialUpdateBooking(int bookingId, Map<String, Object> updatedFields) {
        String url = baseUrl + ApiEndpoints.BOOKING.getPath();
        System.out.println("Отправка PUT-запроса на обновление данных о бронировании на URL: " + url);

        // Отправляем PATCH-запрос с обновлёнными данными
        return getRequestSpec()
                .body(updatedFields) // Передаём только изменённые поля
                .when()
                .patch(ApiEndpoints.BOOKING.getPath() + "/" + bookingId)
                .then()
                .log().all()
                .extract()
                .response();
    }
    public Response getBookingsWithFilter(String firstName, String lastName, String checkin, String checkout, Integer limit) {
        Map<String, String> queryParams = new HashMap<>();
        String url = baseUrl + ApiEndpoints.BOOKING.getPath();
        System.out.println("Отправка GET-запроса на URL: " + url + " с параметрами фильтрации: " + queryParams);

        if (firstName != null) {
            queryParams.put("firstname", firstName);
        }
        if (lastName != null) {
            queryParams.put("lastname", lastName);
        }
        if (checkin != null) {
            queryParams.put("checkin", checkin);
        }
        if (checkout != null) {
            queryParams.put("checkout", checkout);
        }
        if (limit != null) {
            queryParams.put("limit", limit.toString()); // Добавляем параметр limit, если он указан
        }

        return given()
                .spec(getRequestSpec())
                .queryParams(queryParams)
                .when()
                .get(url)
                .then()
                .log().all()
                .statusCode(200) // Ожидаемый статус можно настроить
                .extract()
                .response();
    }
}

