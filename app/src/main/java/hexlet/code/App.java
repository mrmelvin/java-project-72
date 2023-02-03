package hexlet.code;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import hexlet.code.controllers.UrlController;

public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8099");
        return Integer.valueOf(port);
    }
    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            ctx.render("index.html");
        });
        app.routes(() -> {
            path("urls", () -> {
                get(UrlController::getAllUrls);
                post(UrlController::addUrl);
                path("{id}", () -> {
                    get(UrlController::getUrl);
                    path("checks", () -> {
                        post(UrlController::checkUrl);
                    });
                });
            });
        });
    }


    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            JavalinThymeleaf.configure(getTemplateEngine());
            config.enableWebjars();
            if (!isProduction()) {
                config.enableDevLogging();
            }
        });

        addRoutes(app);

        app.before(context -> {
            context.attribute("context", context);
        });
        return app;
    }
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}
