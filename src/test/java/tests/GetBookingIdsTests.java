package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetBookingIdsTests {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private BookingResponse bookingResponse; // Объект, который хранит полученные данные о бронировании
    private BookingData bookingData;
    private UpdateBookingRequest updateRequest;// Объект хранит данные для бронирования, которые отправляются на сервер.
    private BookingData bookingData1;
    private BookingData bookingData2;


    // Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        bookingData = new BookingData();
        bookingData.setFirstname("John");
        bookingData.setLastname("Travolta");
        bookingData.setTotalprice(200);
        bookingData.setDepositpaid(true);
        bookingData.setBookingdates(new BookingDates("2024.12.12", "2024.12.31"));
        bookingData.setAdditionalneeds("Вы можете избавиться от моей жены?");

        // Бронирование 1
        bookingData1 = new BookingData();
        bookingData1.setFirstname("John");
        bookingData1.setLastname("Mr.Anderson");
        bookingData1.setTotalprice(300);
        bookingData1.setDepositpaid(true);
        bookingData1.setBookingdates(new BookingDates("2025-01-01", "2025-01-10"));
        bookingData1.setAdditionalneeds("Breakfast");

        // Бронирование 2
        bookingData2 = new BookingData();
        bookingData2.setFirstname("Agent");
        bookingData2.setLastname("Smith");
        bookingData2.setTotalprice(500);
        bookingData2.setDepositpaid(false);
        bookingData2.setBookingdates(new BookingDates("2025-02-01", "2025-02-15"));
        bookingData2.setAdditionalneeds("Lunch");

        updateRequest = new UpdateBookingRequest();
        updateRequest.setFirstname("Боливар");
        updateRequest.setLastName("Фантикович");
        updateRequest.setTotalPrice(1500);
        updateRequest.setDepositPaid(true);
        updateRequest.setBookingdates(new BookingDates("2024.12.01", "2024.12.20"));
        updateRequest.setAdditionalNeeds("Вы можете избавиться от моей жены?");
    }

    @Test
    public void testGetBookingList() throws Exception {
        //Выполняем запрос к endpoint / booking через APIClient
        Response response = apiClient.getBooking();
        //Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        });
        assertThat(bookings).isNotEmpty();

        for (Booking booking : bookings) {
            assertThat(booking.getBookingid()).isGreaterThan(0);
        }
    }

    @Test
    public void testGetBookingClass() throws Exception {
        //Выполняем запрос к endpoint / booking через APIClient
        Response response = apiClient.getBookingId(2);

        //Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        BookingData bookings = objectMapper.readValue(responseBody, BookingData.class);

        //Проверки значений полей из входящего json
        assertThat(bookings.getTotalprice()).isGreaterThan(0); // Предполагается, что метод getBookingId() возвращает ID бронирования
        assertThat(bookings.getBookingdates()).isNotNull(); // Проверка, что дата заезда не null
        assertThat(bookings.isDepositpaid()).isIn(true, false);// Проверяет, что значение может быть как true, так false
    }

    @Test
    public void testGetBookingClassSecond() throws Exception {
        //Выполняем запрос к endpoint / booking через APIClient
        Response response = apiClient.getBookingIdSecond(2408);

        //Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        BookingData bookings = objectMapper.readValue(responseBody, BookingData.class);

        //Проверки значений полей из входящего json
        assertThat(bookings.getTotalprice()).isGreaterThan(0); // Предполагается, что метод getBookingId() возвращает ID бронирования
        assertThat(bookings.getBookingdates()).isNotNull(); // Проверка, что дата заезда не null
        assertThat(bookings.isDepositpaid()).isIn(true, false);// Проверяет, что значение может быть как true, так false
    }

    @Test
    public void testGetBookingIdAndDelete() throws Exception {
        // Выполняем запрос к endpoint /booking через APIClient
        Response response = apiClient.getBooking();

        // Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        });

        // Проверяем, что список бронирований не пустой
        assertThat(bookings).isNotEmpty();

        // Предполагаем, что берем первое бронирование из списка
        Booking bookingIdToDelete = bookings.get(0);
        int bookingId = bookingIdToDelete.getBookingid();

        assertThat(bookingId).isGreaterThan(0); // Проверка, что bookingId валиден

        // Удаляем бронирование по ID
        Response deleteResponse = apiClient.deleteBooking(bookingId);

        // Проверяем, что статус-код ответа на удаление равен 201 (или любому другому ожидаемому коду)
        assertThat(deleteResponse.getStatusCode()).isEqualTo(201);

        // Проверяем, что бронирование действительно удалено
        Response checkResponse = apiClient.getBookingId(bookingId);
        assertThat(checkResponse.getStatusCode()).isEqualTo(404); // Ожидаем, что бронирование не найдено
    }

    @Test
    public void createBookingTest() throws Exception {
        // Создаём одно бронирование. Выполняем запрос к endpoint /booking через APIClient
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(bookingData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize bookingData to JSON", e);
        }
        Response response = apiClient.createdBooking(requestBody);

        // Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализация полученного от сервера JSON в JAVA-объект BookingResponse
        bookingResponse = objectMapper.readValue(response.getBody().asString(), BookingResponse.class);

        // Проверка на то, что ответ не пустой
        assertThat(bookingResponse).isNotNull();

        // Проверки на то, что отправленные данные (bookingData) совпадают с полученными данными (bookingResponse)
        assertEquals(bookingData.getFirstname(), bookingResponse.getBooking().getFirstname());
        assertEquals(bookingData.getLastname(), bookingResponse.getBooking().getLastname());
        assertEquals(bookingData.getTotalprice(), bookingResponse.getBooking().getTotalprice());
        assertEquals(bookingData.isDepositpaid(), bookingResponse.getBooking().isDepositpaid());
        assertEquals("2024-12-12", bookingResponse.getBooking().getBookingdates().getCheckin());
        assertEquals("2024-12-31", bookingResponse.getBooking().getBookingdates().getCheckout());
        assertEquals(bookingData.getAdditionalneeds(), bookingResponse.getBooking().getAdditionalneeds());
    }

    @Test
    public void getAllReservationTest() throws Exception {
        // Создаём одно бронирование. Выполняем запрос к endpoint /booking через APIClient
        String requestBody = objectMapper.writeValueAsString(bookingData);
        Response response = apiClient.createdBooking(requestBody);

        // Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Сохраняем id созданного бронирования
        bookingResponse = objectMapper.readValue(response.getBody().asString(), BookingResponse.class);
        int bookingid = bookingResponse.getBookingid();

        // Проверка, что ID создан
        assertThat(bookingid).isGreaterThan(0);

        // Получаем от сервера список всех бронирований. Отправляем GET-запрос на эндпоинт /booking
        Response getAllResponse = apiClient.getBooking();
        String responseBody = getAllResponse.getBody().asString();

        // Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализация полученного от сервера JSON в JAVA-объект Booking
        List<Booking> booking = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        });

        // Проверка, что вернувшийся список ID booking не null и не пуст
        assertThat(booking).isNotNull().isNotEmpty();

        // Проверка, что созданное бронирование находится в общем списке
        boolean createdBooking = booking.stream().anyMatch(b -> b.getBookingid() == bookingid);

        // Проверка, что бронирование найдено
        assertThat(createdBooking).isTrue();
    }

    @Test
    public void testGetReservationById() throws Exception {
        // Создаём одно бронирование. Выполняем запрос к endpoint /booking через APIClient
        String requestBody = objectMapper.writeValueAsString(bookingData);
        Response response = apiClient.createdBooking(requestBody);

        // Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Сохраняем id созданного бронирования
        bookingResponse = objectMapper.readValue(response.getBody().asString(), BookingResponse.class);
        int bookingid = bookingResponse.getBookingid();

        // Проверка, что ID создан
        assertThat(bookingid).isGreaterThan(0);

        // Отправляем GET-запрос на эндпоинт /booking/{id}, подставив созданный bookingid.
        Response getByIdResponse = apiClient.getBookingIdSecond(bookingid);
        String responseBody = getByIdResponse.getBody().asString();

        // Проверяем, что статус-код ответа равен 200
        assertThat(getByIdResponse.getStatusCode()).isEqualTo(200);

        // Десериализация полученного от сервера JSON в JAVA-объект BookingResponse
        bookingData = objectMapper.readValue(responseBody, BookingData.class);

        // Проверка на то, что ответ не пустой
        assertThat(bookingResponse).isNotNull();

        System.out.println("API Response: " + response.getBody().asString());

        // Проверки на то, что отправленные данные (bookingData) совпадают с полученными данными от сервера
        assertEquals(bookingData.getFirstname(), bookingResponse.getBooking().getFirstname());
        assertEquals(bookingData.getLastname(), bookingResponse.getBooking().getLastname());
        assertEquals(bookingData.getTotalprice(), bookingResponse.getBooking().getTotalprice());
        assertEquals(bookingData.isDepositpaid(), bookingResponse.getBooking().isDepositpaid());
        assertEquals("2024-12-12", bookingResponse.getBooking().getBookingdates().getCheckin());
        assertEquals("2024-12-31", bookingResponse.getBooking().getBookingdates().getCheckout());
        assertEquals(bookingData.getAdditionalneeds(), bookingResponse.getBooking().getAdditionalneeds());
    }

    @Test
    public void testChangeReservation() throws Exception {
        // Предусловие: создать бронирование и сохранить ID бронирования
        String requestBody = objectMapper.writeValueAsString(bookingData);
        Response response = apiClient.createdBooking(requestBody);
        assertThat(response.getStatusCode()).isEqualTo(200);
        bookingResponse = objectMapper.readValue(response.getBody().asString(), BookingResponse.class);
        int bookingid = bookingResponse.getBookingid();
        assertThat(bookingid).isGreaterThan(0);
        // Шаг 1: Отправить PUT-запрос на /booking/{id} , подставив bookingid, с обновлёнными
        //данными в теле (например, firstName , totalPrice ).
        Response putResponse = apiClient.updateBooking(bookingid, updateRequest);
        String responseBody = putResponse.getBody().asString();
        assertThat(putResponse.getStatusCode()).isEqualTo(200);
        // Шаг 2: дессириализация ответа от сервера
        updateRequest = objectMapper.readValue(responseBody, UpdateBookingRequest.class);
        // Шаг 3: Проверки
        assertThat(updateRequest).isNotNull();
        assertThat(updateRequest.getFirstname()).isEqualTo(updateRequest.getFirstname());
        assertThat(updateRequest.getLastName()).isEqualTo(updateRequest.getLastName());
        assertThat(updateRequest.getTotalPrice()).isEqualTo(updateRequest.getTotalPrice());
        assertThat(updateRequest.getBookingdates()).isEqualTo(updateRequest.getBookingdates());

        System.out.println("Обновлённые данные бронирования: " + objectMapper.writeValueAsString(updateRequest));
    }

    @Test
    public void testPartialUpdateBooking() throws JsonProcessingException {
        // Предусловие: создать бронирование и сохранить ID бронирования
        String requestBody = objectMapper.writeValueAsString(bookingData);
        Response response = apiClient.createdBooking(requestBody);
        assertThat(response.getStatusCode()).isEqualTo(200);
        bookingResponse = objectMapper.readValue(response.getBody().asString(), BookingResponse.class);
        int bookingid = bookingResponse.getBookingid();
        assertThat(bookingid).isGreaterThan(0);
        // Шаг 1: Отправить PATCH-запрос на /booking/{id} , подставив bookingid , с изменёнными
        // значениями (например, только firstName).
        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("firstname", "Mirko");
        updatedFields.put("lastname", "Krokop");// Меняем только firstName
        Response patchResponse = apiClient.partialUpdateBooking(bookingid, updatedFields);
        String responseBody = patchResponse.getBody().asString();
        assertThat(patchResponse.getStatusCode()).isEqualTo(200);
        // Шаг 2: дессириализация ответа от сервера
        updateRequest = objectMapper.readValue(responseBody, UpdateBookingRequest.class);
        // Проверяем, что обновлённые поля изменились
        assertThat(updateRequest).isNotNull();
        assertThat(updateRequest.getFirstname()).isEqualTo(updateRequest.getFirstname());
        assertThat(updateRequest.getLastName()).isEqualTo(updateRequest.getLastName());
        // Проверяем, что другие поля не изменились (например, totalprice, depositpaid)
        int originalTotalPrice = bookingResponse.getBooking().getTotalprice();
        boolean originalDepositPaid = bookingResponse.getBooking().isDepositpaid();
        assertEquals(originalTotalPrice, updateRequest.getTotalPrice());
        assertEquals(originalDepositPaid, updateRequest.isDepositPaid());
    }

    @Test
    public void testGetBookingsWithFilters() throws Exception {
        // Создаём тестовые бронирования для проверки фильтров
        String requestBody1 = objectMapper.writeValueAsString(bookingData1);
        String requestBody2 = objectMapper.writeValueAsString(bookingData2);

        // Отправляем запросы на создание бронирований
        Response response1 = apiClient.createdBooking(requestBody1);
        Response response2 = apiClient.createdBooking(requestBody2);

        // Проверяем, что оба бронирования успешно созданы
        assertThat(response1.getStatusCode()).isEqualTo(200);
        assertThat(response2.getStatusCode()).isEqualTo(200);

        // Получаем ID созданных бронирований
        BookingResponse createdBooking1 = objectMapper.readValue(response1.getBody().asString(), BookingResponse.class);
        BookingResponse createdBooking2 = objectMapper.readValue(response2.getBody().asString(), BookingResponse.class);

        int bookingId1 = createdBooking1.getBookingid();
        int bookingId2 = createdBooking2.getBookingid();

        // Шаг 1: Фильтрация по FirstName с ограничением (limit) = 10
        Response filterByFirstNameResponse = apiClient.getBookingsWithFilter("John", null, null, null, 2);
        assertThat(filterByFirstNameResponse.getStatusCode()).isEqualTo(200);

        // Проверяем, что созданные ID присутствуют в отфильтрованном результате
        List<Integer> filteredIdsByFirstName = filterByFirstNameResponse.jsonPath().getList("bookingid");
        assertThat(filteredIdsByFirstName).contains(bookingId1);

        // Проверка деталей бронирования по фильтру
        for (int bookingId : filteredIdsByFirstName) {
            Response bookingDetails = apiClient.getBookingId(bookingId);
            assertThat(bookingDetails.getStatusCode()).isEqualTo(200);

            BookingData booking = objectMapper.readValue(bookingDetails.getBody().asString(), BookingData.class);

            // Убедимся, что имя соответствует фильтру
            assertEquals("John", booking.getFirstname());
        }

        // Шаг 2: Фильтрация по LastName с ограничением (limit) = 10
        Response filterByLastNameResponse = apiClient.getBookingsWithFilter(null, "Smith", null, null, 2);
        assertThat(filterByLastNameResponse.getStatusCode()).isEqualTo(200);

        // Проверяем, что ID соответствует фильтрации
        List<Integer> filteredIdsByLastName = filterByLastNameResponse.jsonPath().getList("bookingid");
        assertThat(filteredIdsByLastName).contains(bookingId2);

        for (int bookingId : filteredIdsByLastName) {
            Response bookingDetails = apiClient.getBookingId(bookingId);
            assertThat(bookingDetails.getStatusCode()).isEqualTo(200);

            BookingData booking = objectMapper.readValue(bookingDetails.getBody().asString(), BookingData.class);

            // Убедимся, что фамилия соответствует фильтру
            assertEquals("Smith", booking.getLastname());
        }

        // Шаг 3: Фильтрация по Checkin/Checkout с ограничением (limit) = 10
        Response filterByDateResponse = apiClient.getBookingsWithFilter(null, null, "2025-01-01", "2025-01-10", 2);
        assertThat(filterByDateResponse.getStatusCode()).isEqualTo(200);

        // Проверяем, что ID соответствует диапазону дат
        List<Integer> filteredIdsByDate = filterByDateResponse.jsonPath().getList("bookingid");
        assertThat(filteredIdsByDate).contains(bookingId1);

        for (int bookingId : filteredIdsByDate) {
            Response bookingDetails = apiClient.getBookingId(bookingId);
            assertThat(bookingDetails.getStatusCode()).isEqualTo(200);

            BookingData booking = objectMapper.readValue(bookingDetails.getBody().asString(), BookingData.class);

            // Убедимся, что даты соответствуют фильтру
            assertEquals("2025-01-01", booking.getBookingdates().getCheckin());
            assertEquals("2025-01-10", booking.getBookingdates().getCheckout());
        }
    }
    @AfterEach
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(bookingResponse.getBookingid());
        assertThat(apiClient.getBookingId(bookingResponse.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}