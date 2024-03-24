package org.example;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;

@WebFilter("/time")
public class TimezoneValidateFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Перевіряємо, чи існує параметр timezone
        String timezone = httpRequest.getParameter("timezone");

        if (timezone == null || timezone.isEmpty()) {
            // Отримуємо значення параметра timezone з Cookie
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("lastTimezone")) {
                        timezone = cookie.getValue();
                        break;
                    }
                }
            }
            // Якщо часовий пояс не знайдено у Cookie, встановлюємо UTC за замовчуванням
            if (timezone == null || timezone.isEmpty()) {
                timezone = "UTC";
            }
        }

        // Декодуємо значення параметра timezone (якщо потрібно)
        timezone = URLDecoder.decode(timezone, StandardCharsets.UTF_8);

        // Валідуємо часовий пояс
        if (!isValidTimezone(timezone)) {
            // Повертаємо помилку, якщо часовий пояс недійсний
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getWriter().write("Invalid timezone");
            return;
        }

        // Зберігаємо значення параметра timezone у Cookie
        if (timezone != null && !timezone.isEmpty()) {
            // URL-кодуємо значення параметра timezone перед збереженням у Cookie
            String encodedTimezone = URLEncoder.encode(timezone, StandardCharsets.UTF_8);
            Cookie cookie = new Cookie("lastTimezone", encodedTimezone);
            httpResponse.addCookie(cookie);
        }

        // Продовжуємо ланцюжок фільтрів, якщо все валідно
        chain.doFilter(request, response);
    }

    private boolean isValidTimezone(String timezone) {
        try {
            // Спробуємо створити об'єкт TimeZone з вказаним значенням
            TimeZone.getTimeZone(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // Метод init не потрібен, але вимагається інтерфейсом Filter
    }

    public void destroy() {
        // Метод destroy не потрібен, але вимагається інтерфейсом Filter
    }
}

