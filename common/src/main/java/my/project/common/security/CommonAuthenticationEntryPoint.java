package my.project.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import my.project.common.dto.ApiError;
import my.project.common.exception.CommonErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CommonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Locale locale = RequestContextUtils.getLocale(request);

        ApiError body = new ApiError(
                401,
                CommonErrorCode.UNAUTHORIZED.name(),
                messageSource.getMessage("common.unauthorized", null, "common.unauthorized", locale),
                request.getRequestURI()
        );

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
