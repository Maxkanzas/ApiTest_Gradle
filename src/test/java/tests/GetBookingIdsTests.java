package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import com.fasterxml.jackson.core.type.TypeReference;
import core.models.BookingData;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class GetBookingIdsTests {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    // Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup(){
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBookingList()throws Exception{
        //Выполняем запрос к endpoint / booking через APIClient
        Response response = apiClient.getBooking();
        //Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference <List<Booking>>(){});
        assertThat(bookings).isNotEmpty();

        for (Booking booking : bookings){
            assertThat(booking.getBookingid()).isGreaterThan(0);
        }
    }
    @Test
    public void testGetBookingClass()throws Exception{
        // Сначала получаем список бронирований, чтобы взять ID для тестирования
        Response bookingListResponse = apiClient.getBooking();
        assertThat(bookingListResponse.getStatusCode()).isEqualTo(200);

        String bookingListBody = bookingListResponse.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(bookingListBody, new TypeReference<List<Booking>>() {});
        assertThat(bookings).isNotEmpty();

        // Получаем ID первого бронирования для теста
        int bookingId = bookings.get(0).getBookingid();

        // Выполняем запрос к endpoint /booking/{id} через APIClient, используя ID бронирования
        Response response = apiClient.getBookingId(bookingId);

        // Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        BookingData bookingData = objectMapper.readValue(responseBody, BookingData.class);

        // Проверки значений полей из входящего json
        assertThat(bookingData.getTotalprice()).isGreaterThan(0); // Ожидается, что цена будет больше 0
        assertThat(bookingData.getBookingdates()).isNotNull(); // Проверка, что дата заезда не null
        assertThat(bookingData.isDepositpaid()).isIn(true, false); // Проверяет, что значение может быть как true, так false
    }
}
