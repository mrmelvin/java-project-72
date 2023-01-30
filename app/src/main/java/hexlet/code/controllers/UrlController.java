package hexlet.code.controllers;

import hexlet.code.domain.UrlEntity;
import hexlet.code.domain.query.QUrlEntity;
import io.ebean.PagedList;
import io.javalin.http.Context;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import java.time.Instant;


public class UrlController {
    public static void getAllUrls(Context context) {

        int page = context.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<UrlEntity> pagedUrlEntity = new QUrlEntity()
                                                    .setFirstRow(page * rowsPerPage)
                                                    .setMaxRows(rowsPerPage)
                                                    .orderBy()
                                                    .id.asc()
                                                    .findPagedList();

        List<UrlEntity> urlsList = pagedUrlEntity.getList();

        int lastPage = pagedUrlEntity.getTotalPageCount() + 1;
        int currentPage = pagedUrlEntity.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        context.attribute("pages", pages);
        context.attribute("currentPage", currentPage);
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
                context.attribute("flash-type", "warning");
            } else {
                UrlEntity newUrl = new UrlEntity(host);
                newUrl.save();
                context.attribute("flash", "Страница успешно добавлена");
                context.attribute("flash-type", "success");
            }
        } catch (MalformedURLException e) {
            context.attribute("flash", "Некорректный URL");
        }

        context.redirect("/urls");

    }
}
