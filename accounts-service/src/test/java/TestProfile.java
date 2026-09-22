import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

public class TestProfile {
    private static final String BASE_URI = "http://localhost:8080";

    static {
        RestAssured.baseURI = BASE_URI;
    }

    @DisplayName("CP-PER-001 - Consultar perfil")
    @Test
    void shouldGetProfileSuccessfully() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/accounts/user/11")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("userId", equalTo("11"))
                .body("balance", notNullValue())
                .body("cvu", notNullValue())
                .body("alias", notNullValue());
    }

    @DisplayName("CP-PER-002 - Usuario inexistente")
    @Test
    void shouldReturn404WhenUserDoesNotExist() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/accounts/user/999999")
                .then()
                .statusCode(404);
    }

    @DisplayName("CP-PER-003 - Visualizar CVU")
    @Test
    void shouldReturnValidCVU() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/accounts/user/11")
                .then()
                .statusCode(200)
                .body("cvu", matchesPattern("\\d{22}"));
    }

    @DisplayName("CP-PER-004 - Visualizar Alias")
    @Test
    void shouldReturnValidAlias() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/accounts/user/11")
                .then()
                .statusCode(200)
                .body("alias", matchesPattern("^[a-zA-Z]+\\.[a-zA-Z]+\\.[a-zA-Z]+$"));
    }

    @DisplayName("CP-PER-005 - Actualizar alias")
    @Test
    void shouldUpdateAliasSuccessfully() {

        String token = loginAndGetToken();

        String request = """
        {
           "alias":"sol.luna.campo"
        }
        """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/accounts/1")
                .then()
                .statusCode(200)
                .body("alias", equalTo("sol.luna.campo"));
    }

    @DisplayName("CP-PER-006 - Alias duplicado")
    @Test
    void shouldReturn409WhenAliasAlreadyExists() {

        String token = loginAndGetToken();

        String request = """
        {
           "alias":"granizo.pixel.alegre"
        }
        """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/accounts/1")
                .then()
                .statusCode(409);
    }

    @DisplayName("CP-PER-007 - Cuenta inexistente")
    @Test
    void shouldReturn404WhenAccountDoesNotExist() {

        String token = loginAndGetToken();

        String request = """
        {
           "alias":"nuevo.alias"
        }
        """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/accounts/999999")
                .then()
                .statusCode(404);
    }

    private String loginAndGetToken() {

        RestAssured.baseURI = BASE_URI;

        return given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "email": "valentina@test.com",
                      "password": "Valen1234"
                    }
                    """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }
}
