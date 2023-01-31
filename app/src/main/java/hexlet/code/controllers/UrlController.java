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

public class UrlController {

    private static String getHostAndPort(String url) {
        try {
            URL preparedUrl = new URL(url);
            Integer port = preparedUrl.getPort();
            String portValue = (port != -1 ? Integer.toString(port) : "");
            String address = portValue.equals("") ? preparedUrl.getHost() : preparedUrl.getHost() + ":" + portValue;
            return address;
        } catch (MalformedURLException e) {
            return "incorrect";
        }
    }
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

        String siteAddress = getHostAndPort(url);
        if (siteAddress.equals("incorrect")) {
            context.attribute("flash", "Некорректный URL");
            context.attribute("flash-type", "danger");
        } else {
            UrlEntity checkedUrl = new QUrlEntity().name.equalTo(siteAddress).findOne();
            if (checkedUrl != null) {
                context.attribute("flash", "Страница уже существует");
                context.attribute("flash-type", "warning");
            } else {
                UrlEntity newUrl = new UrlEntity(siteAddress);
                newUrl.save();
                context.attribute("flash", "Страница успешно добавлена");
                context.attribute("flash-type", "success");
            }
        }
        context.redirect("/urls");
    }
}
