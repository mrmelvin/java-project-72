package hexlet.code.controllers;

import hexlet.code.domain.UrlEntity;
import hexlet.code.domain.query.QUrlEntity;
import io.javalin.http.Context;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.List;

import java.time.Instant;


public class UrlController {
    public static void getAllUrls(Context context) {
        List<UrlEntity> urlsList = new QUrlEntity().findList();
        context.attribute("urlsList", urlsList);
        context.render("urls/index.html");
    }

    public static void getUrl(Context context) {
        String id = context.pathParam("id");
        UrlEntity oneUrl = new QUrlEntity().id.equalTo(Integer.valueOf(id)).findOne();
        context.attribute("url", oneUrl);
        context.render("urls/show.html");

    }

    public static void addUrl(Context context) {
        String url = context.formParam("url");
        System.out.println(url);

        try {
            URL u = new URL(url);
            String host = u.getHost();
            UrlEntity checkedUrl = new QUrlEntity().name.equalTo(host).findOne();
            if (checkedUrl != null) {
                context.attribute("flash", "Страница уже существует");
            } else {
                String patternFormat = "dd/MM/yyyy HH:mm";
                Instant now = Instant.now();
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patternFormat)
//                                                                .withZone(ZoneId.systemDefault());
//                String currentTime = formatter.format(now);
                UrlEntity newUrl = new UrlEntity(host, now);
                newUrl.save();
                context.attribute("flash", "Страница успешно добавлена");
            }
        } catch (MalformedURLException e) {
            context.attribute("flash", "Некорректный URL");
        }

        context.redirect("/urls");

    }
}
