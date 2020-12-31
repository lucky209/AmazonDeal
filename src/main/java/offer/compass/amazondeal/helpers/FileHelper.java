package offer.compass.amazondeal.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class FileHelper {

    private void createNewDirectory(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            boolean isCreated = file.mkdir();
            if (isCreated)
                log.info("New folder created, path is " + folderPath);
        }
    }

    private void createTransparentImage(File file, String fileNameWithPath) throws IOException {
        BufferedImage image = ImageIO.read(file);
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.setColor(Color.WHITE); // Or what ever fill color you want...
        g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        ImageIO.write(copy, Constants.IMAGE_FORMAT_V2, new File(fileNameWithPath));
    }

    void savePriceHistorySS(WebDriver browser, String pathToSave) throws IOException {
        File srcFile = browser.findElement(By.className(PriceHistoryConstants.GRAPH_CLASS)).getScreenshotAs(OutputType.FILE);
        this.createTransparentImage(srcFile, pathToSave);
        log.info("Screenshot saved successfully in the path " + pathToSave);
    }

    void saveAmazonSS(WebDriver browser, String pathToSave, String folderPath) throws IOException {
        this.createNewDirectory(folderPath);
        File srcFile = ((TakesScreenshot) browser).getScreenshotAs(OutputType.FILE);
        this.createTransparentImage(srcFile, pathToSave);
        log.info("Screenshot saved successfully in the path " + pathToSave);
    }
}
