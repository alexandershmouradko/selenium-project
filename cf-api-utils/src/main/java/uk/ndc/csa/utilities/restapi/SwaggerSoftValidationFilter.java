package uk.ndc.csa.utilities.restapi;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Locale;
import java.util.Objects;
import uk.ndc.csa.utilities.common.Reporter;
import uk.ndc.csa.utilities.common.ThreadContext;

/**
 * Backward-compatible soft assertion wrapper around Atlassian's modern
 * {@link OpenApiValidationFilter}. The legacy class name is retained so
 * existing feature steps do not have to change during the 3.0 migration.
 */
public final class SwaggerSoftValidationFilter implements Filter {
    private final String mode;
    private final OpenApiValidationFilter delegate;

    public SwaggerSoftValidationFilter(String mode, String openApiDefinition) {
        this.mode = normalizeMode(mode);
        this.delegate = new OpenApiValidationFilter(
                Objects.requireNonNull(openApiDefinition, "An OpenAPI definition is required"));
    }

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext context) {
        try {
            // OpenApiValidationFilter validates the complete request/response interaction.
            // The mode is retained for source compatibility and diagnostic visibility.
            if (!"request and response".equals(mode)) {
                Reporter.log("OpenAPI validator 3.x validates the full interaction; requested legacy mode: " + mode);
            }
            return delegate.filter(requestSpec, responseSpec, context);
        } catch (AssertionError | RuntimeException validationFailure) {
            ThreadContext.getInstance().sa().fail(
                    "OpenAPI contract validation failed: " + validationFailure.getMessage(),
                    validationFailure);
            Reporter.recordError(validationFailure);
            // The delegate normally obtains the response before validating it. Re-executing
            // the request here would be unsafe, so fail immediately after recording the soft
            // assertion when no response is available to return.
            throw validationFailure;
        }
    }

    private static String normalizeMode(String value) {
        String normalized = Objects.requireNonNullElse(value, "request and response")
                .trim()
                .toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "request", "response", "request and response" -> normalized;
            default -> throw new IllegalArgumentException("Unknown OpenAPI validation mode: " + value);
        };
    }
}
