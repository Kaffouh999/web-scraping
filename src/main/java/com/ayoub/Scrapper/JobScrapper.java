package com.ayoub.Scrapper;

import com.ayoub.dao.DatabseManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobScrapper {
    private static final Logger LOGGER = Logger.getLogger(JobScrapper.class.getName());

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.5735.199 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            scrapeJobs(driver);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred during scraping", e);
        } finally {
            driver.quit();
        }
    }

    private static void scrapeJobs(WebDriver driver) {
        driver.get("https://ma.indeed.com/jobs?q=&l=Morocco");

        acceptCookies(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> jobElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[class*='job_seen_beacon'], [data-testid='jobsearch-ResultsList'] > li")
        ));

        LOGGER.info("Found " + jobElements.size() + " job listings");

        for (WebElement job : jobElements) {
            processJobElement(job);
        }
    }

    private static void acceptCookies(WebDriver driver) {
        try {
            WebElement cookieButton = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler")));
            cookieButton.click();
            LOGGER.info("Accepted cookies.");
        } catch (TimeoutException e) {
            LOGGER.warning("No cookie banner found or already accepted.");
        }
    }

    private static void processJobElement(WebElement job) {
        try {
            String jobTitle = findElementText(job,
                    By.cssSelector("[data-testid='job-title']"),
                    By.xpath(".//h2[contains(@class, 'jobTitle')]"),
                    By.cssSelector("h2 > a"),
                    By.cssSelector(".title"));

            String companyName = findElementText(job,
                    By.cssSelector("[data-testid='company-name']"),
                    By.xpath(".//span[contains(@class, 'companyName')]"),
                    By.cssSelector(".companyName"));

            String location = findElementText(job,
                    By.cssSelector("[data-testid='location']"),
                    By.xpath(".//div[contains(@class, 'companyLocation')]"),
                    By.cssSelector(".companyLocation"));

            String summary = findElementText(job,
                    By.cssSelector("[data-testid='job-snippet']"),
                    By.xpath(".//div[contains(@class, 'job-snippet')]"));

            String postedDate = findElementText(job,
                    By.cssSelector("[data-testid='posted-date']"),
                    By.xpath(".//div[contains(@class, 'metadata')]/div[contains(@class, 'date')]"),
                    By.xpath(".//span[contains(@class, 'date')]"));

            LOGGER.info("\nJob Title: " + jobTitle +
                    "\nCompany: " + companyName +
                    "\nLocation: " + location +
                    "\nSummary: " + summary +
                    "\nPosted Date: " + postedDate + "\n-----");

            // Save job data to the database
            DatabseManager.saveJob(jobTitle, companyName, location, summary, postedDate);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing a job listing", e);
        }
    }

    private static String findElementText(WebElement parent, By... locators) {
        for (By locator : locators) {
            try {
                WebElement element = parent.findElement(locator);
                String text = element.getText().trim();
                if (!text.isEmpty()) return text;
            } catch (NoSuchElementException ignored) {
            }
        }
        return "N/A";
    }
}