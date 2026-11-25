package com.airline.airline_booking_system.notification.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.airline.airline_booking_system.notification.entity.NotificationHistory;
import com.airline.airline_booking_system.notification.enums.NotificationType;

@Component
public class NotificationEmailTemplate {
    private static final DateTimeFormatter DEPARTURE_FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm 'UTC'").withZone(ZoneId.of("UTC"));

    private final TemplateEngine templateEngine;

    public NotificationEmailTemplate(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public RenderedEmail render(NotificationHistory notification) {
        NotificationType type = notification.getNotificationType();
        String greeting = "Xin chào " + notification.getRecipientName() + ",";
        String subject;
        String title;
        String message;
        List<Map<String, String>> details = List.of();

        if (type == NotificationType.ACCOUNT_REGISTERED) {
            subject = "Chào mừng bạn đến với Airline Booking System";
            title = "Chào mừng bạn đến với Airline Booking System!";
            message = "Tài khoản của bạn đã được tạo thành công.\n"
                    + "Bạn có thể đăng nhập để tìm chuyến bay và quản lý đặt chỗ.\n\n"
                    + "Cảm ơn bạn đã lựa chọn Airline Booking System.";
        } else {
            boolean confirmed = type == NotificationType.BOOKING_CONFIRMED;
            subject = (confirmed ? "Xác nhận đặt vé " : "Thông báo hủy vé ") + notification.getBookingCode();
            title = confirmed ? "Đặt vé đã được xác nhận" : "Thông báo hủy đặt vé";
            message = confirmed
                    ? "Đặt vé của bạn đã được xác nhận. Vui lòng lưu lại thông tin đặt chỗ bên dưới."
                    : "Đặt vé của bạn đã được hủy. Thông tin đặt chỗ được ghi bên dưới để bạn tiện theo dõi.";
            details = List.of(
                    Map.of("label", "Mã đặt chỗ", "value", safe(notification.getBookingCode())),
                    Map.of("label", "Chuyến bay", "value", safe(notification.getFlightNumber())),
                    Map.of("label", "Khởi hành", "value", formatDeparture(notification.getDepartureTime())));
        }

        Context context = new Context(Locale.forLanguageTag("vi"));
        context.setVariable("preheader", subject);
        context.setVariable("title", title);
        context.setVariable("greeting", greeting);
        context.setVariable("message", message);
        context.setVariable("details", details);
        String html = templateEngine.process("email/notification", context);
        return new RenderedEmail(subject, greeting + "\n\n" + message + renderTextDetails(details), html);
    }

    private String renderTextDetails(List<Map<String, String>> details) {
        if (details.isEmpty()) return "";
        StringBuilder output = new StringBuilder("\n\n");
        details.forEach(detail -> output.append(detail.get("label")).append(": ")
                .append(detail.get("value")).append('\n'));
        return output.toString();
    }

    private String formatDeparture(Instant departureTime) {
        return departureTime == null ? "Chưa có thông tin" : DEPARTURE_FORMAT.format(departureTime);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Chưa có thông tin" : value;
    }
}
