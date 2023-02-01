package hexlet.code;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

class AppTest {

    private static Javalin app;
    private static String baseUrl;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }


    @Nested
    class BasicTests {

        @Test
        void testCreate() {
            String someUrl = "https://www.youtube.com/watch?v=iJ_2fJWfh1c";
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                            .field("url", someUrl).asEmpty();

            assertThat(response.getStatus()).isEqualTo(302);
            Url urlFromDB = new QUrl().name.contains("https://www.youtube.com").findOne();
            assertThat(urlFromDB.getName()).isNotNull();
            assertThat(urlFromDB.getName()).isEqualTo("https://www.youtube.com/");
        }

        @Test
        void testCreateUrlWithPort() {
            String someUrl = "https://ru.hexlet.io:12345/courses/java-basics";
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", someUrl).asEmpty();

            assertThat(response.getStatus()).isEqualTo(302);

            Url urlFromDB = new QUrl().name.contains("https://ru.hexlet.io:12345").findOne();
            assertThat(urlFromDB.getName()).isNotNull();
            assertThat(urlFromDB.getName()).isEqualTo("https://ru.hexlet.io:12345/");
        }

        @Test
        void testShowAll() {
            String someUrl = "https://foobar.com/stydies";
            Unirest.post(baseUrl + "/urls").field("url", someUrl).asEmpty();

            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://foobar.com");

        }

        @Test
        void testShowOne() {
            String someUrl = "https://en.wikipedia.org/wiki/Java_(programming_language)";
            Unirest.post(baseUrl + "/urls").field("url", someUrl).asEmpty();

            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/4").asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("https://en.wikipedia.org");
        }
    }


}
