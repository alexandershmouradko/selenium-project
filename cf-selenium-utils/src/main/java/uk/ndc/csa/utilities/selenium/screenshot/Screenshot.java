package uk.ndc.csa.utilities.selenium.screenshot;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverFactory;

/**
 * Base class providing set of common selenium methods
 */

public class Screenshot {

    /**
     * capture displayed area or scrolling screenshot and return a file object.
     * to capture scrolling screenshot property scrollingScreenshot = true has to be set in runtime.properties file
     */
    public static File grabScreenshot() {

        String screenshotType = null;

        screenshotType = ThreadContext.getInstance().getSeleniumProps().getString("scrollingScreenshot");

        if (screenshotType != null) {
            return (screenshotType.equalsIgnoreCase("true") ? grabScrollingScreenshot() : grabDisplayedAreaScreenShot());
        } else {
            return grabDisplayedAreaScreenShot();
        }
    }

    /**
     * capture screenshot for the displayed area and return a file object
     */
    public static File grabDisplayedAreaScreenShot() {
        try {
            Thread.sleep(ThreadContext.getInstance().getSeleniumProps().getInt("screenshotDelay", 0));
        } catch (InterruptedException | NumberFormatException e) {
            e.printStackTrace();
        }
        File screenshot = ((TakesScreenshot) DriverFactory.getInstance().getDriver()).getScreenshotAs(OutputType.FILE);
        return screenshot;

    }

    /**
     * grab screenshot snippet
     */
    public static File snipScreenshot(File screenshot, By by, Dimension dim, Point point) {

        try {
            BufferedImage buffer = ImageIO.read(screenshot);
            // Crop the entire page screenshot to get only element screenshot
            BufferedImage snippet = buffer.getSubimage(0, point.getY(), point.getX() + dim.getWidth(), dim.getHeight());
            ImageIO.write(snippet, "png", screenshot);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenshot;
    }

    /**
     * capture screenshot and save to specified location
     */
    public static File saveScreenshot(File screenshot, String filePath) {
        UUID uuid = UUID.randomUUID();
        File file = new File(filePath + uuid + ".png");
        try {
            FileUtils.moveFile(screenshot, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static Boolean compareScreenshot(File fileExpected, File fileActual) throws IOException {

        BufferedImage bufileActual = ImageIO.read(fileActual);
        BufferedImage bufileExpected = ImageIO.read(fileExpected);

        DataBuffer dafileActual = bufileActual.getData().getDataBuffer();
        DataBuffer dafileExpected = bufileExpected.getData().getDataBuffer();

        int sizefileActual = dafileActual.getSize();

        Boolean matchFlag = true;

        for (int j = 0; j < sizefileActual; j++) {
            if (dafileActual.getElem(j) != dafileExpected.getElem(j)) {
                matchFlag = false;
                break;
            }
        }

        return matchFlag;
    }

    /**
     * capture scrolling screenshot and return a file object
     */
    public static File grabScrollingScreenshot() {
        try {
            Thread.sleep(ThreadContext.getInstance().getSeleniumProps().getInt("screenshotDelay", 0));
        } catch (InterruptedException | NumberFormatException e) {
            e.printStackTrace();
        }

        ru.yandex.qatools.ashot.Screenshot screenshot;

        if (System.getProperties().get("os.name").toString().contains("Mac")) {
            screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportRetina(100, 0, 0, 2)).takeScreenshot(DriverFactory.getInstance().getDriver());
        } else {
            screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(DriverFactory.getInstance().getDriver());
        }

        File file = new File("image.png");

        try {
            ImageIO.write(screenshot.getImage(), "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

}
