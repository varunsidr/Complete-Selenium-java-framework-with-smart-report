package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
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
import org.openqa.selenium.interactions.Interactive;

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

        String browser = prop.getProperty("browser", "chrome");
        boolean headless = Boolean.parseBoolean(prop.getProperty("headless", "false"));
        String url = prop.getProperty("url");
        int pageLoadTimeout = Integer.parseInt(prop.getProperty("pageload.timeout", "30"));
        WAIT_TIMEOUT = Integer.parseInt(prop.getProperty("implicit.wait", "10"));

        WebDriver rawDriver;
        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions opt = new ChromeOptions();
            opt.addArguments("--disable-notifications");
            opt.addArguments("--disable-extensions");
            opt.addArguments("--disable-infobars");
            opt.addArguments("--remote-allow-origins=*");
            opt.addArguments("--lang=en-IN");
            opt.addArguments("--disable-gpu");
            opt.addArguments("--disable-dev-shm-usage");
            opt.addArguments("--no-sandbox");
            opt.setExperimentalOption("prefs", blockedContentPreferences());
            opt.setPageLoadStrategy(PageLoadStrategy.EAGER);
            if (headless) {
                opt.addArguments("--headless=new");
                opt.addArguments("--window-size=1920,1080");
            }
            rawDriver = new ChromeDriver(opt);
        } else if (browser.equalsIgnoreCase("edge")) {
            EdgeOptions opt = new EdgeOptions();
            opt.addArguments("--disable-notifications");
            opt.addArguments("--disable-extensions");
            opt.addArguments("--disable-infobars");
            opt.addArguments("--remote-allow-origins=*");
            opt.addArguments("--lang=en-IN");
            opt.addArguments("--disable-gpu");
            opt.addArguments("--disable-dev-shm-usage");
            opt.addArguments("--no-sandbox");
            opt.setExperimentalOption("prefs", blockedContentPreferences());
            opt.setPageLoadStrategy(PageLoadStrategy.EAGER);
            if (headless) {
                opt.addArguments("--headless=new");
                opt.addArguments("--window-size=1920,1080");
            }
            rawDriver = new EdgeDriver(opt);
        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

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
