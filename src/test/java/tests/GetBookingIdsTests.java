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
        public void testGetBookingClass ()throws Exception {
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
    public void testGetBookingClassSecond ()throws Exception {
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
}
