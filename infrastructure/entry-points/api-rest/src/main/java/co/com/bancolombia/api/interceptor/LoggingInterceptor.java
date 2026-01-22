package co.com.bancolombia.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC = "correlationId";
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_MDC, correlationId);
        MDC.put("method", request.getMethod());
        MDC.put("path", request.getRequestURI());

        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        logger.info("Incoming request - Method: {}, Path: {}", request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        Object startTimeObj = request.getAttribute(START_TIME_ATTR);
        long duration = 0;

        if (startTimeObj != null) {
            long startTime = (long) startTimeObj;
            duration = System.currentTimeMillis() - startTime;
        }

        if (ex != null) {
            logger.error("Request failed - Status: {}, Duration: {}ms", response.getStatus(), duration, ex);
        } else {
            logger.info("Request completed - Status: {}, Duration: {}ms", response.getStatus(), duration);
        }

        MDC.clear();
    }
}
