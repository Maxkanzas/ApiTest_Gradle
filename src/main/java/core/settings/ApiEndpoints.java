package core.settings;


// Класс ApiEndpoints служит для хранения всех конечных точек API в одном месте
// Обращаться к данным API можно методом ApiEndpoints.BOOKING.getPath()

public enum ApiEndpoints {
    PING("/ping"),
    BOOKING("/booking"),
    AUTH("/auth");

    private final String path;

    ApiEndpoints(String path) {
        this.path = path;
    }
    public String getPath() {
        return path;
    }
}
