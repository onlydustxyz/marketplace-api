package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

@AllArgsConstructor
public class DelegatedAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final HandlerExceptionResolver resolver;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
    resolver.resolveException(request, response, null, authException);
  }
}
