package com.project.taskmanagement.config;

import com.project.taskmanagement.controller.AttachmentController;
import com.project.taskmanagement.controller.AuthController;
import com.project.taskmanagement.controller.ReportExcelExportController;
import com.project.taskmanagement.controller.ReportPdfExportController;
import com.project.taskmanagement.controller.SprintController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiDocumentationTest {

    private static final String CONTROLLER_PACKAGE =
            "com.project.taskmanagement.controller";

    @Test
    void openApiConfigShouldExposeProjectInfoAndBearerJwt() {
        OpenAPI openAPI = new OpenApiConfig().taskManagementOpenAPI();

        assertThat(openAPI.getInfo().getTitle())
                .isEqualTo("Task Management Agile/Scrum API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getServers())
                .extracting(server -> server.getUrl())
                .contains("/api", "http://localhost:8080/api");

        SecurityScheme bearerAuth = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertThat(bearerAuth.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(bearerAuth.getScheme()).isEqualTo("bearer");
        assertThat(bearerAuth.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void everyControllerAndMappedEndpointShouldBeDocumented() throws Exception {
        for (Class<?> controller : controllerClasses()) {
            assertThat(controller.getAnnotation(Tag.class))
                    .as("@Tag của %s", controller.getSimpleName())
                    .isNotNull();

            if (!controller.equals(AuthController.class)) {
                assertThat(controller.getAnnotation(SecurityRequirement.class))
                        .as("bearerAuth của %s", controller.getSimpleName())
                        .isNotNull();
            }

            for (Method method : controller.getDeclaredMethods()) {
                if (isMappedEndpoint(method)) {
                    assertThat(method.getAnnotation(Operation.class))
                            .as("@Operation của %s#%s", controller.getSimpleName(), method.getName())
                            .isNotNull();
                }
            }
        }
    }

    @Test
    void multipartEndpointsShouldDeclareMultipartAndFileParameter() throws Exception {
        assertMultipartEndpoint(
                AttachmentController.class.getDeclaredMethod(
                        "upload",
                        java.util.UUID.class,
                        com.project.taskmanagement.enums.AttachmentEntityType.class,
                        java.util.UUID.class,
                        MultipartFile.class
                )
        );
        assertMultipartEndpoint(
                SprintController.class.getDeclaredMethod(
                        "importTasksFromExcel",
                        java.util.UUID.class,
                        java.util.UUID.class,
                        MultipartFile.class
                )
        );
    }

    @Test
    void downloadEndpointsShouldRemainBinaryResponses() throws Exception {
        List<Method> downloads = List.of(
                AttachmentController.class.getDeclaredMethod(
                        "download",
                        java.util.UUID.class,
                        java.util.UUID.class
                ),
                SprintController.class.getDeclaredMethod(
                        "downloadTaskExcelTemplate",
                        java.util.UUID.class,
                        java.util.UUID.class
                ),
                ReportExcelExportController.class.getDeclaredMethod(
                        "exportSprintReport",
                        java.util.UUID.class,
                        java.util.UUID.class
                ),
                ReportPdfExportController.class.getDeclaredMethod(
                        "exportSprintReportPdf",
                        java.util.UUID.class,
                        java.util.UUID.class
                )
        );

        for (Method method : downloads) {
            assertThat(method.getGenericReturnType())
                    .as("Response file của %s", method.getName())
                    .isInstanceOf(ParameterizedType.class);
            assertThat(method.getAnnotationsByType(ApiResponse.class))
                    .as("Binary response documentation của %s", method.getName())
                    .isNotEmpty();
        }
    }

    private static List<Class<?>> controllerClasses() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        return scanner.findCandidateComponents(CONTROLLER_PACKAGE).stream()
                .<Class<?>>map(component -> loadClass(component.getBeanClassName()))
                .toList();
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static boolean isMappedEndpoint(Method method) {
        return Arrays.stream(method.getAnnotations())
                .map(annotation -> annotation.annotationType())
                .anyMatch(type -> type.equals(RequestMapping.class)
                        || type.isAnnotationPresent(RequestMapping.class));
    }

    private static void assertMultipartEndpoint(Method method) {
        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertThat(mapping).isNotNull();
        assertThat(mapping.consumes())
                .contains("multipart/form-data");
        assertThat(method.getParameterTypes())
                .contains(MultipartFile.class);
    }
}
