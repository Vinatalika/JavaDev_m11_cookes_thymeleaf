package org.example;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() {
        // Ініціалізуємо шаблонізатор Thymeleaf
        templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("webapp/WEB-INF/temp/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateEngine.setTemplateResolver(templateResolver);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Отримуємо значення параметра timezone з запиту
        String timezone = request.getParameter("timezone");

        // Перевіряємо, чи параметр timezone не є null
        if (timezone == null || timezone.isEmpty()) {
            // Отримуємо значення параметра timezone з Cookie
            timezone = getLastTimezoneFromCookie(request);
            // Встановлюємо значення UTC за замовчуванням, якщо параметр відсутній
            if (timezone == null || timezone.isEmpty()) {
                timezone = "UTC";
            }
        }

        // Встановлюємо часовий пояс відповідно до параметра timezone
        ZoneId zoneId = ZoneId.of(getValidTimeZone(timezone));

        // Отримуємо поточний час в вказаному часовому поясі
        ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedTime = currentTime.format(formatter);

        // Створюємо об'єкт контексту Thymeleaf
        Context context = new Context();
        context.setVariable("formattedTime", formattedTime);

        // Рендеримо HTML сторінку за допомогою Thymeleaf
        templateEngine.process("time", context, response.getWriter());
    }

    private String getValidTimeZone(String timezone) {
        // Виправляємо формат параметру timezone
        timezone = timezone.trim().replace(" ", "+");
        return timezone;
    }

    private String getLastTimezoneFromCookie(HttpServletRequest request) {
        String timezone = null;
        // Отримуємо значення параметра timezone з Cookie
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if (cookie.getName().equals("lastTimezone")) {
                    timezone = cookie.getValue();
                    break;
                }
            }
        }
        return timezone;
    }
}