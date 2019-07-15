package gov.va.api.health.dataquery.service.controller;

import lombok.Builder;
import lombok.Value;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * This argument resolver is a replacement for {@link
 * org.springframework.web.bind.annotation.RequestParam} specifically for the `_count` parameter. It
 * supports automatic clamping to ensure that the _count value is never too big.
 *
 * <p>This resolver will need to be registered with an WebMvcConfigurer
 *
 * <pre>
 * &#064;Configuration
 * public class AwesomeConfig implements WebMvcConfigurer {
 *   &#064;Override
 *   public void addArgumentResolvers(List&lt;HandlerMethodArgumentResolver> resolvers) {
 *     resolvers.add(
 *         CountParameterResolver.builder().defaultCount(15).maxCount(20).build());
 *   }
 * }
 * </pre>
 *
 * <p>See {@link CountParameter}.
 */
@Builder
@Value
public class CountParameterResolver implements HandlerMethodArgumentResolver {

  private static final String COUNT_PARAMETER = "_count";

  int defaultCount;
  int maxCount;

  private int asInt(MethodParameter methodParameter, String maybeCount) {
    if (maybeCount == null) {
      return defaultCount;
    }
    try {
      return Integer.parseInt(maybeCount);
    } catch (NumberFormatException e) {
      throw new MethodArgumentTypeMismatchException(
          maybeCount, int.class, "_count", methodParameter, e);
    }
  }

  @Override
  public Object resolveArgument(
      MethodParameter methodParameter,
      ModelAndViewContainer unusedModelAndViewContainer,
      NativeWebRequest nativeWebRequest,
      WebDataBinderFactory unusedWebDataBinderFactory) {
    String maybeCount = nativeWebRequest.getParameter(COUNT_PARAMETER);
    int count = asInt(methodParameter, maybeCount);
    return count > maxCount ? maxCount : count;
  }

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    Class<?> parameterType = methodParameter.getParameterType();
    CountParameter parameterAnnotation =
        methodParameter.getParameterAnnotation(CountParameter.class);
    return parameterType.equals(int.class) && parameterAnnotation != null;
  }
}
