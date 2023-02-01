package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import hexlet.code.domain.query.QUrlCheck;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.ebean.PagedList;
import io.javalin.http.Context;

public class UrlController {

    private static final Map<String, String> PATTERNS = Map.of(
            "title", "(?<=<title>)(.*)(?=</title>)",
            "h1", "(?<=<h1>)(.*)(?=</h1>)",
            "description", "(?<=<meta name=\"description\" content=\")(.*?)(?=\"/>)");

    private static String getHostAndPort(String url) {
        String urlWithHostAndPort = "";
        try {
            new URL(url);
            Pattern pattern = Pattern.compile("^(http|https)://(.*?)/");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                urlWithHostAndPort = matcher.group(0);
            }
        } catch (MalformedURLException e) {
            urlWithHostAndPort = "incorrect";
        }
        return urlWithHostAndPort;
    }

    private static Map<String, String> findTextInHTML(String htmlBody, Map<String, String> patterns) {
        Map<String, String> outputMap = new HashMap<>();
        for (var pattern: patterns.entrySet()) {
            Pattern currentPattern = Pattern.compile(pattern.getValue(), Pattern.CASE_INSENSITIVE);
            Matcher currentMatcher = currentPattern.matcher(htmlBody);
            if (currentMatcher.find()) {
                outputMap.put(pattern.getKey(), currentMatcher.group(0));
            } else {
                outputMap.put(pattern.getKey(), "");
            }
        }
        return outputMap;
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
        List<UrlCheck> checks = new QUrlCheck().url.equalTo(oneUrl).findList();
        context.attribute("url", oneUrl);
        context.attribute("checks", checks);
        context.render("urls/show.html");

    }

    public static void addUrl(Context context) {
        String url = context.formParam("url");
        System.out.println(url);

        String siteAddress = getHostAndPort(url);
        System.out.println(siteAddress);
        if (siteAddress.equals("incorrect")) {
            context.attribute("flash", "Некорректный URL");
            context.attribute("flash-type", "danger");
        } else {
            Url checkedUrl = new QUrl().name.equalTo(siteAddress).findOne();
            if (checkedUrl != null) {
                context.attribute("flash", "Страница уже существует");
                context.attribute("flash-type", "warning");
            } else {
                Url newUrl = new Url(siteAddress);
                newUrl.save();
                context.attribute("flash", "Страница успешно добавлена");
                context.attribute("flash-type", "success");
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
            Map<String, String> seoData = findTextInHTML(pageContent, PATTERNS);
            title = seoData.get("title");
            h1 = seoData.get("h1");
            description = seoData.get("description");
        }
        UrlCheck newCheck = new UrlCheck(statusCode, title, h1, description, checkedUrl);
        newCheck.save();
        String currentPath = "/urls/" + id;
        context.redirect(currentPath);
    }
}
