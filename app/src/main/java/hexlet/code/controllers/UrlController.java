package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import hexlet.code.domain.query.QUrlCheck;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.ebean.PagedList;
import io.javalin.http.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UrlController {


    private static String getHostAndPort(String url) {
        String urlWithHostAndPort = "";
        try {
            URL preparedUrl = new URL(url);
            String protocol = preparedUrl.getProtocol();
            String host = preparedUrl.getHost();
            String port = preparedUrl.getPort() == -1 ? "" : ":" + preparedUrl.getPort();
            urlWithHostAndPort = protocol + "://" + host + port;
        } catch (MalformedURLException e) {
            urlWithHostAndPort = "incorrect";
        }
        return urlWithHostAndPort;
    }

    public static void getAllUrls(Context context) {

        int page = context.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrl = new QUrl().setFirstRow(page * rowsPerPage)
                                                    .setMaxRows(rowsPerPage)
                                                    .orderBy()
                                                    .id.asc()
                                                    .findPagedList();

        List<Url> urlsList = pagedUrl.getList();

        int lastPage = pagedUrl.getTotalPageCount() + 1;
        int currentPage = pagedUrl.getPageIndex() + 1;
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
        Url oneUrl = new QUrl().id.equalTo(Integer.valueOf(id)).findOne();
        List<UrlCheck> checks = new QUrlCheck().url.equalTo(oneUrl).orderBy().createdAt.desc().findList();
        context.attribute("url", oneUrl);
        context.attribute("checks", checks);
        context.render("urls/show.html");

    }

    public static void addUrl(Context context) {
        String url = context.formParam("url");
        String siteAddress = getHostAndPort(url);

        if (siteAddress.equals("incorrect")) {
            context.sessionAttribute("flash", "Некорректный URL");
            context.sessionAttribute("flash-type", "danger");
        } else {
            Url checkedUrl = new QUrl().name.equalTo(siteAddress).findOne();
            if (checkedUrl != null) {
                context.sessionAttribute("flash", "Страница уже существует");
                context.sessionAttribute("flash-type", "warning");
            } else {
                Url newUrl = new Url(siteAddress);
                newUrl.save();
                context.sessionAttribute("flash", "Страница успешно добавлена");
                context.sessionAttribute("flash-type", "success");
            }
        }
        context.redirect("/urls");
    }

    public static void checkUrl(Context context) {
        Integer id = Integer.valueOf(context.path().replaceAll("([a-zA-Z]+|\\/)", ""));
        Url checkedUrl = new QUrl().id.equalTo(id).findOne();
        String url = checkedUrl.getName();
        HttpResponse<String> response = Unirest.get(url).asString();
        int statusCode = response.getStatus();
        String title = "";
        String h1 = "";
        String description = "";
        if (statusCode == 200) {
            String pageContent = response.getBody();
            Document doc = Jsoup.parse(pageContent);
            title = doc.title();
            h1 = doc.getElementsByTag("h1").text();
            description = doc.select("meta[name=description]").attr("content");
        }
        UrlCheck newCheck = new UrlCheck(statusCode, title, h1, description, checkedUrl);
        newCheck.save();
        String currentPath = "/urls/" + id;
        context.sessionAttribute("flash", "Страница успешно проверена");
        context.sessionAttribute("flash-type", "success");
        context.redirect(currentPath);
    }
}
