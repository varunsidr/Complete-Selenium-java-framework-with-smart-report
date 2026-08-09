package utils;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class Reporter {
    static ExtentReports report;
    static TakesScreenshot screenshot;

    public static class BrokenLinkResult {
        private final String url;
        private final String status;
        private final boolean passed;
        private final String details;
        private final String screenshotPath;

        public BrokenLinkResult(String url, String status, boolean passed, String details) {
            this(url, status, passed, details, null);
        }

        public BrokenLinkResult(String url, String status, boolean passed, String details, String screenshotPath) {
            this.url = url;
            this.status = status;
            this.passed = passed;
            this.details = details;
            this.screenshotPath = screenshotPath;
        }

        public String getUrl() {
            return url;
        }

        public String getStatus() {
            return status;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getDetails() {
            return details;
        }

        public String getScreenshotPath() {
            return screenshotPath;
        }

        public BrokenLinkResult withScreenshotPath(String screenshotPath) {
            return new BrokenLinkResult(url, status, passed, details, screenshotPath);
        }
    }
    
    public static void attachInfoScreenshotToReport(String name, ExtentTest test, String message) {
        test.info(message, MediaEntityBuilder.createScreenCaptureFromPath(captureScreenShot(name)).build());
    }

    public static ExtentReports generateExtentReport(String reportName) {
        if (report == null) {
            File reportsDir = new File(System.getProperty("user.dir") + "/reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            String timestampedReportName = reportsDir.getAbsolutePath() + "/" + reportName + "_" + timestamp + ".html";

            ExtentSparkReporter reporter = new ExtentSparkReporter(timestampedReportName);
            reporter.config().setDocumentTitle("Hybrid Framework Automation Report");
            reporter.config().setReportName("Hybrid Framework Execution Report");
            reporter.config().setTheme(Theme.STANDARD);
            reporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
            reporter.config().setOfflineMode(true);

            report = new ExtentReports();
            report.attachReporter(reporter);
            report.setSystemInfo("Framework", "Hybrid Test Automation");
            report.setSystemInfo("Runner", "TestNG");
            report.setSystemInfo("Build Tool", "Maven");
            report.setSystemInfo("Java", System.getProperty("java.version"));
            report.setSystemInfo("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
            report.setSystemInfo("User", System.getProperty("user.name"));
        }
        return report;
    }

    public ExtentTest createTest(String testName) {
        return report.createTest(testName);
    }

    public static String captureScreenShot(String filename) {
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String name = sanitizeFileName(filename) + timestamp + ".png";
        screenshot = (TakesScreenshot) Base.driver;
        File file = screenshot.getScreenshotAs(OutputType.FILE);
        File screenshotsDestination = getScreenshotsDestination();

        File target = new File(screenshotsDestination, name);
        try {
            FileUtils.copyFile(file, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "./screenshots/" + name;
    }

    public static String captureElementScreenShot(WebElement element, String filename) {
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String name = sanitizeFileName(filename) + timestamp + ".png";
        File target = new File(getScreenshotsDestination(), name);
        try {
            ((JavascriptExecutor) Base.driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                    element);
            Object rawMetrics = ((JavascriptExecutor) Base.driver).executeScript(
                    "const target=arguments[0];"
                            + "const isUsable=(node,rect)=>{"
                            + "const style=getComputedStyle(node);"
                            + "return style.display!=='none'&&style.visibility!=='hidden'&&style.opacity!=='0'"
                            + "&&rect.width>0&&rect.height>0&&rect.right>0&&rect.bottom>0"
                            + "&&rect.left<window.innerWidth&&rect.top<window.innerHeight;"
                            + "};"
                            + "let node=target;"
                            + "let best=target.getBoundingClientRect();"
                            + "for(let i=0;node&&i<8;i++,node=node.parentElement){"
                            + "const r=node.getBoundingClientRect();"
                            + "if(!isUsable(node,r)){continue;}"
                            + "const area=r.width*r.height;"
                            + "const bestArea=Math.max(best.width*best.height,1);"
                            + "if((r.width>=260&&r.height>=120)||(area>bestArea&&r.width>=120&&r.height>=40)){"
                            + "best=r;"
                            + "if(r.width>=260&&r.height>=120){break;}"
                            + "}"
                            + "}"
                            + "const viewportW=Math.max(window.innerWidth,1);"
                            + "const viewportH=Math.max(window.innerHeight,1);"
                            + "const maxW=Math.min(560,viewportW);"
                            + "const maxH=Math.min(320,viewportH);"
                            + "const cropW=Math.min(maxW,Math.max(Math.min(320,viewportW),best.width+64));"
                            + "const cropH=Math.min(maxH,Math.max(Math.min(160,viewportH),best.height+64));"
                            + "const centerX=best.left+(best.width/2);"
                            + "const centerY=best.top+(best.height/2);"
                            + "const left=Math.max(0,Math.min(centerX-(cropW/2),viewportW-cropW));"
                            + "const top=Math.max(0,Math.min(centerY-(cropH/2),viewportH-cropH));"
                            + "return [left,top,cropW,cropH,viewportW,viewportH];",
                    element);
            @SuppressWarnings("unchecked")
            List<Number> metrics = (List<Number>) rawMetrics;
            File viewport = ((TakesScreenshot) Base.driver).getScreenshotAs(OutputType.FILE);
            BufferedImage image = ImageIO.read(viewport);

            double scaleX = image.getWidth() / metrics.get(4).doubleValue();
            double scaleY = image.getHeight() / metrics.get(5).doubleValue();
            int x = clamp((int) Math.floor(metrics.get(0).doubleValue() * scaleX), 0, image.getWidth() - 1);
            int y = clamp((int) Math.floor(metrics.get(1).doubleValue() * scaleY), 0, image.getHeight() - 1);
            int width = clamp((int) Math.ceil(metrics.get(2).doubleValue() * scaleX), 1, image.getWidth() - x);
            int height = clamp((int) Math.ceil(metrics.get(3).doubleValue() * scaleY), 1, image.getHeight() - y);

            BufferedImage crop = image.getSubimage(x, y, width, height);
            ImageIO.write(crop, "png", target);
            return "./screenshots/" + name;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void attachScreenshotToReport(String name, ExtentTest test, String message) {
        test.pass(MediaEntityBuilder.createScreenCaptureFromPath(captureScreenShot(name), message).build());
    }

    public static void attachFailureScreenshotToReport(String name, ExtentTest test, String message) {
        test.fail(MediaEntityBuilder.createScreenCaptureFromPath(captureScreenShot(name), message).build());
    }

    public static String generateBrokenLinkReport(String reportName, String pageUrl, List<BrokenLinkResult> results) {
        File reportsDir = new File(System.getProperty("user.dir") + "/reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String safeReportName = (reportName == null ? "broken-link-report" : reportName).replaceAll("[^a-zA-Z0-9.-]", "_");
        Path outputPath = reportsDir.toPath().resolve(safeReportName + "_" + timestamp + ".html");

        int passedCount = 0;
        int failedCount = 0;
        int redirectCount = 0;
        int clientErrorCount = 0;
        int serverErrorCount = 0;
        int otherErrorCount = 0;
        StringBuilder rows = new StringBuilder();
        for (BrokenLinkResult result : results) {
            String cssClass = result.isPassed() ? "pass" : "fail";
            String badgeText = result.isPassed() ? "PASS" : "FAIL";
            String status = result.getStatus() == null ? "" : result.getStatus();
            String categoryClass = "";
            if (status.startsWith("HTTP 3")) {
                categoryClass = "redirect";
                redirectCount++;
            } else if (status.startsWith("HTTP 4")) {
                categoryClass = "client-error error";
                clientErrorCount++;
            } else if (status.startsWith("HTTP 5")) {
                categoryClass = "server-error error";
                serverErrorCount++;
            } else if (status.startsWith("ERROR")) {
                categoryClass = "other-error error";
                otherErrorCount++;
            }
            if (result.isPassed()) {
                passedCount++;
            } else {
                failedCount++;
            }
            rows.append("<tr class=\"result-row ").append(cssClass).append(" ").append(categoryClass).append("\">")
                    .append("<td><span class=\"status-badge ").append(cssClass).append("\">")
                    .append(badgeText).append("</span></td>")
                    .append("<td>").append(buildUrlCell(result.getUrl())).append("</td>")
                    .append("<td>").append(escapeHtml(result.getStatus())).append("</td>")
                    .append("<td>").append(buildDetailsCell(result)).append("</td>")
                    .append("</tr>");
        }

        String generatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html lang=\"en\">")
                .append("<head>")
                .append("<meta charset=\"UTF-8\" />")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
                .append("<title>Broken Link Report</title>")
                .append("<style>")
                .append(":root{--bg:#0b1020;--surface:#111827;--surface-2:#172033;--text:#f8fafc;--muted:#94a3b8;--line:#263349;--blue:#60a5fa;--green:#34d399;--red:#fb7185;--amber:#fbbf24;}")
                .append("*{box-sizing:border-box;}")
                .append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;margin:0;background:radial-gradient(circle at top left,#18243a 0,#0b1020 34%,#070b15 100%);color:var(--text);}")
                .append(".shell{max-width:1280px;margin:0 auto;padding:32px 24px 40px;}")
                .append(".hero{display:flex;justify-content:space-between;align-items:flex-start;gap:24px;flex-wrap:wrap;margin-bottom:24px;}")
                .append(".hero h1{margin:0;font-size:34px;line-height:1.1;font-weight:700;letter-spacing:0;}")
                .append(".hero p{margin:10px 0 0;color:var(--muted);font-size:15px;}")
                .append(".meta{text-align:right;color:var(--muted);font-size:13px;line-height:1.6;}")
                .append(".summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:12px;margin:0 0 20px;}")
                .append(".card{appearance:none;width:100%;padding:18px;border-radius:8px;background:rgba(17,24,39,.86);border:1px solid var(--line);box-shadow:0 16px 36px rgba(0,0,0,.22);transition:transform .16s ease,box-shadow .16s ease,border-color .16s ease;background-color .16s ease;color:var(--text);font:inherit;text-align:left;cursor:pointer;backdrop-filter:blur(14px);}")
                .append(".card:hover{transform:translateY(-2px);box-shadow:0 18px 40px rgba(0,0,0,.34);border-color:#3b4c67;}")
                .append(".card.active{border-color:var(--blue);box-shadow:0 0 0 3px rgba(96,165,250,.18),0 18px 40px rgba(0,0,0,.30);}")
                .append(".card:focus-visible{outline:2px solid var(--blue);outline-offset:3px;}")
                .append(".label{display:block;color:var(--muted);font-size:11px;font-weight:700;letter-spacing:.12em;text-transform:uppercase;}")
                .append(".value{display:block;margin-top:12px;font-size:34px;font-weight:750;line-height:1;letter-spacing:0;}")
                .append(".card.pass .value{color:var(--green);}.card.fail .value{color:var(--red);}.card.redirect .value{color:var(--amber);}.card.error .value{color:#cbd5e1;}")
                .append(".detail-text{display:block;margin-bottom:10px;}.row-evidence{display:inline-grid;gap:6px;color:var(--muted);text-decoration:none;font-size:12px;font-weight:650;}.row-evidence img{display:block;width:180px;max-height:96px;object-fit:contain;border-radius:8px;border:1px solid #3b4c67;background:#0f172a;}")
                .append(".toolbar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin:18px 0 12px;}")
                .append(".search{display:flex;align-items:center;gap:10px;width:min(420px,100%);min-height:44px;padding:0 14px;border-radius:8px;background:rgba(17,24,39,.88);border:1px solid var(--line);box-shadow:0 12px 28px rgba(0,0,0,.20);}")
                .append(".search span{color:var(--muted);font-size:13px;font-weight:650;}.search input{border:none;outline:none;background:transparent;color:var(--text);width:100%;font-size:14px;}")
                .append(".muted{color:var(--muted);font-size:13px;}")
                .append(".table-wrap{overflow:auto;border:1px solid var(--line);border-radius:8px;background:rgba(17,24,39,.92);box-shadow:0 20px 44px rgba(0,0,0,.26);}")
                .append("table{width:100%;border-collapse:separate;border-spacing:0;min-width:820px;}")
                .append("th,td{padding:14px 16px;border-bottom:1px solid var(--line);text-align:left;vertical-align:top;font-size:14px;}")
                .append("th{position:sticky;top:0;background:#111827;color:#cbd5e1;font-size:11px;font-weight:750;letter-spacing:.08em;text-transform:uppercase;z-index:1;}")
                .append("tbody tr:last-child td{border-bottom:none;}tbody tr{transition:background .14s ease;}tbody tr:hover{background:#162033;}")
                .append("tbody tr.fail{background:rgba(251,113,133,.10);}tbody tr.fail:hover{background:rgba(251,113,133,.16);}")
                .append(".status-badge{display:inline-flex;align-items:center;justify-content:center;min-width:54px;padding:6px 10px;border-radius:999px;font-weight:750;font-size:12px;}")
                .append(".status-badge.pass{background:rgba(52,211,153,.16);color:#bbf7d0;}.status-badge.fail{background:rgba(251,113,133,.18);color:#fecdd3;}")
                .append(".result-link{color:var(--blue);text-decoration:none;word-break:break-word;}.result-link:hover{text-decoration:underline;}")
                .append("@media(max-width:760px){.shell{padding:22px 14px 30px;}.hero h1{font-size:28px;}.meta{text-align:left;}.summary{grid-template-columns:repeat(2,minmax(0,1fr));}.value{font-size:30px;}.row-evidence img{width:140px;}}")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"shell\">")
                .append("<div class=\"hero\">")
                .append("<div><h1>Broken Link Checker</h1><p>Scanned page: ").append(escapeHtml(pageUrl == null ? "N/A" : pageUrl)).append("</p></div>")
                .append("<div class=\"meta\"><div>Generated: ").append(escapeHtml(generatedAt)).append("</div><div>Click a metric to filter results</div></div>")
                .append("</div>")
                .append("<div class=\"summary\">")
                .append("<button type=\"button\" class=\"card active\" data-filter=\"all\" aria-pressed=\"true\"><span class=\"label\">Total Links</span><span class=\"value\">")
                .append(results.size()).append("</span></button>")
                .append("<button type=\"button\" class=\"card pass\" data-filter=\"pass\" aria-pressed=\"false\"><span class=\"label\">Passed</span><span class=\"value\">")
                .append(passedCount).append("</span></button>")
                .append("<button type=\"button\" class=\"card fail\" data-filter=\"fail\" aria-pressed=\"false\"><span class=\"label\">Failed</span><span class=\"value\">")
                .append(failedCount).append("</span></button>")
                .append("<button type=\"button\" class=\"card redirect\" data-filter=\"redirect\" aria-pressed=\"false\"><span class=\"label\">Redirects</span><span class=\"value\">")
                .append(redirectCount).append("</span></button>")
                .append("<button type=\"button\" class=\"card error\" data-filter=\"client-error\" aria-pressed=\"false\"><span class=\"label\">Client Errors</span><span class=\"value\">")
                .append(clientErrorCount).append("</span></button>")
                .append("<button type=\"button\" class=\"card error\" data-filter=\"server-error\" aria-pressed=\"false\"><span class=\"label\">Server Errors</span><span class=\"value\">")
                .append(serverErrorCount).append("</span></button>")
                .append("<button type=\"button\" class=\"card error\" data-filter=\"other-error\" aria-pressed=\"false\"><span class=\"label\">Other Errors</span><span class=\"value\">")
                .append(otherErrorCount).append("</span></button>")
                .append("</div>")
                .append("<div class=\"toolbar\">")
                .append("<div class=\"search\"><span aria-hidden=\"true\">Search</span><input id=\"searchInput\" type=\"text\" placeholder=\"Search by URL, status, or details...\" /></div>")
                .append("<div class=\"muted\"><span id=\"visibleCount\">").append(results.size()).append("</span> of ").append(results.size()).append(" rows visible</div>")
                .append("</div>")
                .append("<div class=\"table-wrap\"><table id=\"table\">")
                .append("<thead><tr><th>Status</th><th>URL</th><th>HTTP Result</th><th>Details</th></tr></thead>")
                .append("<tbody>")
                .append(rows)
                .append("</tbody></table></div>")
                .append("</div>")
                .append("<script>")
                .append("const searchInput=document.getElementById('searchInput');const visibleCount=document.getElementById('visibleCount');const rows=[...document.querySelectorAll('.result-row')];const filters=[...document.querySelectorAll('[data-filter]')];let currentFilter='all';function refreshRows(){const q=searchInput.value.toLowerCase().trim();let count=0;rows.forEach(row=>{const matchesFilter=currentFilter==='all'||row.classList.contains(currentFilter);const text=row.textContent.toLowerCase();const matchesSearch=!q||text.includes(q);const visible=matchesFilter&&matchesSearch;row.style.display=visible?'':'none';if(visible){count++;}});visibleCount.textContent=count;}function setFilter(filter){currentFilter=filter||'all';filters.forEach(control=>{const active=control.dataset.filter===currentFilter;control.classList.toggle('active',active);control.setAttribute('aria-pressed',String(active));});refreshRows();}searchInput.addEventListener('input',refreshRows);filters.forEach(control=>{control.addEventListener('click',function(){searchInput.value='';setFilter(this.dataset.filter);});});refreshRows();")
                .append("</script>")
                .append("</body>")
                .append("</html>");

        try {
            Files.writeString(outputPath, html.toString(), StandardCharsets.UTF_8);
            return outputPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write broken link report", e);
        }
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String buildDetailsCell(BrokenLinkResult result) {
        StringBuilder cell = new StringBuilder();
        cell.append("<span class=\"detail-text\">").append(escapeHtml(result.getDetails())).append("</span>");
        String screenshotPath = result.getScreenshotPath();
        if (!result.isPassed() && screenshotPath != null && !screenshotPath.trim().isEmpty()) {
            String safePath = escapeHtml(screenshotPath.trim());
            cell.append("<a class=\"row-evidence\" href=\"").append(safePath)
                    .append("\" target=\"_blank\" rel=\"noopener noreferrer\">")
                    .append("<img src=\"").append(safePath).append("\" alt=\"Failed link screenshot\" />")
                    .append("<span>Open element screenshot</span>")
                    .append("</a>");
        }
        return cell.toString();
    }

    private static File getScreenshotsDestination() {
        File screenshotsDestination = new File(System.getProperty("user.dir") + "/reports/screenshots");
        if (!screenshotsDestination.exists()) {
            screenshotsDestination.mkdirs();
        }
        return screenshotsDestination;
    }

    private static String sanitizeFileName(String filename) {
        String safeName = filename == null || filename.trim().isEmpty() ? "screenshot" : filename.trim();
        return safeName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private static String buildUrlCell(String url) {
        String safeUrl = escapeHtml(url);
        if (url == null || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            return safeUrl;
        }
        return "<a class=\"result-link\" href=\"" + safeUrl + "\" target=\"_blank\" rel=\"noopener noreferrer\">" + safeUrl + "</a>";
    }
}
