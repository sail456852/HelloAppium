package app;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidTouchAction;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.touch.offset.PointOption;
import io.appium.java_client.windows.PressesKeyCode;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by IntelliJ IDEA.<br/>
 * <p>
 * author: Eugene_Wang<br />
 * Date: 8/4/2020<br/>
 * Time: 2:38 PM<br/>
 * To change this template use File | Settings | File Templates.
 * // working simple demo
 * //        String appPackage = "com.android.browser";
 * //        String appActivity = "com.android.browser.BrowserActivity";
 * private static void demoFindOperation(AppiumDriver driver)throws InterruptedException{
 * System.out.println("search1");
 * String searchBox1Id="com.ophone.reader.ui:id/recom_bookstore_default_search_text";
 * driver.findElement(By.id(searchBox1Id)).click();
 * Thread.sleep(2000);
 * System.out.println("search2");
 * String searchBox2Id="com.ophone.reader.ui:id/etSearch";
 * driver.findElement(By.id(searchBox2Id)).sendKeys("天天爱阅读");
 * }
 * passed sonarLint and code inspection
 * powershell
 * gci env:* | sort-object name
 * https://account.jetbrains.com/a/p8qpblcx
 * {
 *   "platformName": "Android",
 *   "platformVersion": "7.1.2",
 *   "deviceName": "127.0.0.1:21503",
 *   "noReset": true,
 *   "noSign": true,
 *   "unicodeKeyboard": true,
 *   "appPackage": "com.ophone.reader.ui",
 *   "appActivity": "com.cmread.bplusc.bookshelf.LocalMainActivity"
 * }
 */
public class SimulateAndroid {

    private static final Logger logger = getLogger(SimulateAndroid.class);
    private static final String SKIP_ID = "com.ophone.reader.ui:id/tv_classification";
    public static final int APPIUM_PORT = 4237;
    public static final int EMULATOR_PORT = 25001;
    private static final Random random = new Random();
    private static boolean initialState = true;
    private static Robot robot;

    /**
     * @param args
     * @throws InterruptedException
     * @throws IOException          start Appium and MeMu before running this
     */
    public static void main(String[] args) throws InterruptedException, IOException, AWTException {
        for (int i = 0; i < 3; i++) {
            tryDailyTasks();
        }
    }

    private static void tryDailyTasks() throws AWTException, IOException, InterruptedException {
        if (!checkIfServerIsRunning(EMULATOR_PORT)) {
            // Appium Server
            startAndroidEmulator();
        }
        if (!checkIfServerIsRunning(APPIUM_PORT)) {
            // Appium Server
            startAppiumServer();
        }
        Runtime.getRuntime().exec("adb connect 127.0.0.1:21503");
        AndroidDriver<MobileElement> driver = getDriver("com.ophone.reader.ui", "com.cmread.bplusc.bookshelf.LocalMainActivity", "7.1.2");
        if (driver == null) {
            logger.info("driver={} empty", driver);
            return;
        } else {
            logger.info("driver={} found", driver);
        }
        AndroidTouchAction touchAction = new AndroidTouchAction(driver);
        clickById(driver);
        try {
            MobileElement backArrowElement = driver.findElement(By.id("com.ophone.reader.ui:id/titlebar_level_2_back_button"));
            if (backArrowElement != null) {
                logger.info("backArrowElement={}", backArrowElement);
                backArrowElement.click();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        delayTouch("天天爱阅读", 568, 1206, touchAction, true);
        delayTouch("我知道了", 520, 1492, touchAction, true);
        delayTouch("去阅读", 876, 1188, touchAction, true);
        delayTouch("正在读的一本书", 169, 1557, touchAction, true);
        initialState = false; // start random space timing paging
        Instant now = Instant.now();
        Instant nowPlus15minsEpoch = now.plus(15, ChronoUnit.MINUTES);
        while (now.isBefore(nowPlus15minsEpoch)) {
            now = Instant.now();
            delayTouch("翻页", 1000, 1210, touchAction, false);
        }
        driver.pressKey(new KeyEvent(AndroidKey.BACK)); // Back Key in the home menu
        Thread.sleep(2000);
        driver.pressKey(new KeyEvent(AndroidKey.BACK)); // Back Key in the home menu
        Thread.sleep(2000); // should in the home directory now
        delayTouch("天天爱阅读", 568, 1206, touchAction, true);
        delayTouch("签到", 867, 1371, touchAction, true);
    }

    /**
     * start MEMU if port is not in used
     */
    public static void startAndroidEmulator() throws AWTException {
        robot = new Robot();
        robot.keyPress(java.awt.event.KeyEvent.VK_WINDOWS);
        robot.keyRelease(java.awt.event.KeyEvent.VK_WINDOWS);
        robotSendString("MEMU");
    }

    private static void robotSendString(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);
        robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
        robot.keyPress(java.awt.event.KeyEvent.VK_V);
        robot.keyRelease(java.awt.event.KeyEvent.VK_V);
        robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.keyPress(java.awt.event.KeyEvent.VK_ENTER);
        robot.keyRelease(java.awt.event.KeyEvent.VK_ENTER);
    }

    /**
     * start the default appium server
     * just like running appium in CLI (powershell)
     * or start appium desktop server by clicking the UI
     * it automatically enables the debug level log
     */
    public static void startAppiumServer() {
        AppiumDriverLocalService server = AppiumDriverLocalService.buildDefaultService();
        server.start();
    }

    public static AndroidDriver<MobileElement> getDriver(String appPackage, String appActivity, String version) {
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability("noReset", true);
        cap.setCapability("unicodeKeyboard", true);
        cap.setCapability("resetKeyboard", true);
        cap.setCapability("noSign", true);
        cap.setCapability("deviceName", "127.0.0.1:21503"); // adb devices
        cap.setCapability("platformName", "Android");
        cap.setCapability("platformVersion", version);
        cap.setCapability("appPackage", appPackage);
        cap.setCapability("appActivity", appActivity);
        URL url;
        AndroidDriver<MobileElement> driver = null;
        try {
            url = new URL("http://127.0.0.1:4723/wd/hub");
            driver = new AndroidDriver<>(url, cap);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (driver != null) {
            logger.info("Appium Virtual Device Created at={}", LocalTime.now());
            return driver;
        } else {
            return null;
        }
    }

    public static boolean checkIfServerIsRunning(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)
        ) {
            logger.info("serverSocket is  available!");
            return false;
        } catch (IOException e) {
            logger.info("serverSocket is not available!");
            return true;
        }
    }

    private static void clickById(AndroidDriver<MobileElement> driver) {
        try {
            MobileElement element = driver.findElement(By.id(SKIP_ID));
            if (element != null) {
                logger.info("clicked skip found={} ", SKIP_ID);
                element.click();
            } else {
                logger.info("skip failed no found={} ", SKIP_ID);
            }
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void delayTouch(String msg, int xOffset, int yOffset, AndroidTouchAction action, boolean fixedDelay) throws InterruptedException {
        logger.info("click {} touch at coordinates x={}, y={}", msg, xOffset, yOffset);
        int delaySeconds = random.nextInt(15000);
        logger.info("delaySeconds={}", delaySeconds);
        if (fixedDelay) {
            Thread.sleep(1000);
        } else {
            Thread.sleep(delaySeconds);
        }
        action.tap(PointOption.point(xOffset, yOffset)).release().perform();
    }

}
