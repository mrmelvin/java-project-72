package hexlet.code;

import io.javalin.Javalin;

public class App {

    public static Javalin getApp() {
        return Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"));
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start();
    }
}
