package core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) //Аннтотация Jackson, котороая позволяет игнорировать неизвестные поля.
public class BookingResponse {
    public int bookingid;
    public BookingData booking;


    public int getBookingid() {
        return bookingid;
    }

    public void setBookingid(int bookingid) {
        this.bookingid = bookingid;
    }

    public BookingData getBooking() {
        return booking;
    }

    public void setBooking(BookingData booking) {
        this.booking = booking;
    }
}
