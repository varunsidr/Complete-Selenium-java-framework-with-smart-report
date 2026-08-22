package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Base {

    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();
    public static final WebDriver driver = createThreadBoundDriverProxy();
    public static Properties prop;
    public static int WAIT_TIMEOUT;

    private static final List<String> AD_BLOCK_URL_PATTERNS = List.of(
            "*://*.doubleclick.net/*",
            "*://*.googlesyndication.com/*",
            "*://*.googleadservices.com/*",
            "*://*.googletagservices.com/*",
            "*://*.adservice.google.com/*",
            "*://*.adnxs.com/*",
            "*://*.adsystem.com/*",
            "*://*.criteo.com/*",
            "*://*.moatads.com/*",
            "*://*.outbrain.com/*",
            "*://*.taboola.com/*");

    private static final String AD_REMOVAL_SCRIPT = """
            (() => {
                const adSelectors = [
                    '.adsbygoogle',
                    '[id^="google_ads"]',
                    '[id*="google_ads"]',
                    '[class*="google-ad"]',
                    '[class*="ad-container"]',
                    '[id*="ad-container"]',
                    '[class*="advertisement"]',
                    '[id*="advertisement"]',
                    '[class*="sponsored"]',
                    '[id*="sponsored"]',
                    '[class*="banner-ad"]',
                    '[id*="banner-ad"]',
                    '[class*="ad-banner"]',
                    '[id*="ad-banner"]',
                    '[aria-label="Advertisement"]',
                    '[data-ad]',
                    'iframe[src*="doubleclick.net"]',
                    'iframe[src*="googlesyndication.com"]',
                    'iframe[src*="googleadservices.com"]',
                    'iframe[src*="googletagservices.com"]',
                    'iframe[src*="adservice.google.com"]',
                    'iframe[src*="taboola.com"]',
                    'iframe[src*="outbrain.com"]'
                ];

                const removeAds = () => {
                    document.querySelectorAll(adSelectors.join(',')).forEach((element) => element.remove());
                    document.querySelectorAll('iframe').forEach((frame) => {
                        const frameInfo = `${frame.src || ''} ${frame.id || ''} ${frame.name || ''} ${frame.className || ''}`;
                        if (/doubleclick|googlesyndication|googleadservices|adservice|adnxs|adsystem|taboola|outbrain|advertisement/i.test(frameInfo)) {
                            frame.remove();
                        }
                    });
                };

                const startAdCleaner = () => {
                    const root = document.documentElement || document.body;
                    if (!root) {
                        window.requestAnimationFrame(startAdCleaner);
                        return;
                    }
                    removeAds();
                    new MutationObserver(() => window.setTimeout(removeAds, 0))
                        .observe(root, { childList: true, subtree: true });
                };

                startAdCleaner();
            })();
            """;

    public static void loadConfig() throws IOException {
        prop = new Properties();
        try (FileInputStream fis = new FileInputStream(
                System.getProperty("user.dir") + "/config/config.properties")) {
            prop.load(fis);
        }
    }

    public static void openBrowser(boolean navigateDefaultUrl) throws IOException {
        loadConfig();

        String browser = resolveSetting("browser", "chrome");
        boolean headless = Boolean.parseBoolean(resolveSetting("headless", "false"));
        boolean remote = Boolean.parseBoolean(resolveSetting("remote", "false"));
        String url = resolveSetting("url", prop.getProperty("url"));
        int pageLoadTimeout = Integer.parseInt(prop.getProperty("pageload.timeout", "30"));
        WAIT_TIMEOUT = Integer.parseInt(prop.getProperty("implicit.wait", "10"));

        WebDriver rawDriver = remote
                ? createRemoteDriver(browser, headless)
                : createLocalDriver(browser, headless);

        DRIVER_THREAD_LOCAL.set(rawDriver);
        rawDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        rawDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(pageLoadTimeout));
        rawDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(WAIT_TIMEOUT));
        rawDriver.manage().window().maximize();
        configureAdBlocking(rawDriver);
        if (navigateDefaultUrl) {
            rawDriver.get(url);
            removeAdsFromCurrentPage();
        }
    }

    public static void removeAdsFromCurrentPage() {
        WebDriver currentDriver = DRIVER_THREAD_LOCAL.get();
        if (currentDriver instanceof JavascriptExecutor javascriptExecutor) {
            javascriptExecutor.executeScript(AD_REMOVAL_SCRIPT);
        }
    }

    public static WebDriver getDriver() {
        WebDriver currentDriver = DRIVER_THREAD_LOCAL.get();
        if (currentDriver == null) {
            throw new IllegalStateException("WebDriver is not initialized for thread: " + Thread.currentThread().getName());
        }
        return currentDriver;
    }

    public static boolean hasDriver() {
        return DRIVER_THREAD_LOCAL.get() != null;
    }

    public static void quitDriver() {
        WebDriver currentDriver = DRIVER_THREAD_LOCAL.get();
        if (currentDriver != null) {
            currentDriver.quit();
            DRIVER_THREAD_LOCAL.remove();
        }
    }

    private static Map<String, Object> blockedContentPreferences() {
        return Map.of(
                "profile.default_content_setting_values.popups", 2,
                "profile.default_content_setting_values.notifications", 2);
    }

    private static WebDriver createLocalDriver(String browser, boolean headless) {
        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions opt = new ChromeOptions();
            configureChromiumOptions(opt, headless);
            return new ChromeDriver(opt);
        }
        if (browser.equalsIgnoreCase("edge")) {
            EdgeOptions opt = new EdgeOptions();
            configureChromiumOptions(opt, headless);
            return new EdgeDriver(opt);
        }
        if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions opt = new FirefoxOptions();
            opt.addArguments("-lang=en-IN");
            if (headless) {
                opt.addArguments("-headless");
            }
            return new FirefoxDriver(opt);
        }
        throw new IllegalArgumentException("Unsupported browser: " + browser);
    }

    private static WebDriver createRemoteDriver(String browser, boolean headless) throws IOException {
        String remoteUrl = resolveRemoteUrl(browser);

        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions opt = new ChromeOptions();
            configureChromiumOptions(opt, headless);
            return new RemoteWebDriver(new URL(remoteUrl), opt);
        }
        if (browser.equalsIgnoreCase("edge")) {
            EdgeOptions opt = new EdgeOptions();
            configureChromiumOptions(opt, headless);
            return new RemoteWebDriver(new URL(remoteUrl), opt);
        }
        if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions opt = new FirefoxOptions();
            opt.addArguments("-lang=en-IN");
            if (headless) {
                opt.addArguments("-headless");
            }
            return new RemoteWebDriver(new URL(remoteUrl), opt);
        }
        throw new IllegalArgumentException("Unsupported browser: " + browser);
    }

    private static void configureChromiumOptions(ChromeOptions options, boolean headless) {
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--lang=en-IN");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.setExperimentalOption("prefs", blockedContentPreferences());
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
    }

    private static void configureChromiumOptions(EdgeOptions options, boolean headless) {
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--lang=en-IN");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.setExperimentalOption("prefs", blockedContentPreferences());
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
    }

    private static String resolveRemoteUrl(String browser) {
        String browserKey = browser == null ? "" : browser.toLowerCase();
        String remoteUrl = prop == null ? null : prop.getProperty("remote.url");

        if ("chrome".equals(browserKey)) {
            remoteUrl = resolveSetting("remote.chrome.url", remoteUrl);
        } else if ("firefox".equals(browserKey)) {
            remoteUrl = resolveSetting("remote.firefox.url", remoteUrl);
        } else if ("edge".equals(browserKey)) {
            remoteUrl = resolveSetting("remote.edge.url", remoteUrl);
        }

        if (remoteUrl == null || remoteUrl.isBlank()) {
            throw new IllegalArgumentException("Remote browser mode is enabled but no remote URL is configured for browser: " + browser);
        }

        return remoteUrl.trim();
    }

    private static String resolveSetting(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        if (prop != null) {
            String configuredValue = prop.getProperty(key);
            if (configuredValue != null && !configuredValue.isBlank()) {
                return configuredValue.trim();
            }
        }

        return defaultValue;
    }

    private static void configureAdBlocking(WebDriver webDriver) {
        if (!(webDriver instanceof ChromiumDriver chromiumDriver)) {
            return;
        }

        try {
            chromiumDriver.executeCdpCommand("Network.enable", Map.of());
            chromiumDriver.executeCdpCommand("Network.setBlockedURLs", Map.of("urls", AD_BLOCK_URL_PATTERNS));
            chromiumDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", Map.of("source", AD_REMOVAL_SCRIPT));
        } catch (RuntimeException e) {
            System.out.println("[Base] Unable to configure browser ad blocking: " + e.getMessage());
        }
    }

    private static WebDriver createThreadBoundDriverProxy() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "ThreadBoundWebDriverProxy(thread=" + Thread.currentThread().getName() + ")";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException("Unsupported Object method: " + method.getName());
                };
            }
            try {
                return method.invoke(getDriver(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };

        return (WebDriver) Proxy.newProxyInstance(
                Base.class.getClassLoader(),
            new Class<?>[] { WebDriver.class, JavascriptExecutor.class, TakesScreenshot.class, HasCapabilities.class,
                Interactive.class },
                handler);
    }
}
