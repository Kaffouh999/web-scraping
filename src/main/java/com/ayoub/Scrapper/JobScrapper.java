package com.ayoub.Scrapper;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;

public class JobScrapper {

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Removed headless for debugging
        // options.addArguments("--headless");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.5735.199 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://ma.indeed.com/jobs?q=&l=Morocco");

            // Wait and handle potential cookie consent
            try {
                WebElement cookieButton = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler")));
                cookieButton.click();
            } catch (Exception ignored) {}

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            List<WebElement> jobElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("[class*='job_seen_beacon'], [data-testid='jobsearch-ResultsList'] > li")
            ));

            System.out.println("Found " + jobElements.size() + " job listings");

            for (WebElement job : jobElements) {
                try {
                    String jobTitle = findElementText(job,
                            By.cssSelector("[data-testid='job-title']"),
                            By.xpath(".//h2[contains(@class, 'jobTitle')]"),
                            By.cssSelector("h2 > a"),
                            By.cssSelector(".title")
                    );

                    String companyName = findElementText(job,
                            By.cssSelector("[data-testid='company-name']"),
                            By.xpath(".//span[contains(@class, 'companyName')]"),
                            By.cssSelector(".companyName"),
                            By.cssSelector("[class*='company']")
                    );

                    String location = findElementText(job,
                            By.cssSelector("[data-testid='location']"),
                            By.xpath(".//div[contains(@class, 'companyLocation')]"),
                            By.cssSelector(".companyLocation"),
                            By.cssSelector("[class*='location']")
                    );

                    String summary = findElementText(job,
                            By.cssSelector("[data-testid='job-snippet']"),
                            By.xpath(".//div[contains(@class, 'job-snippet')]"),
                            By.cssSelector(".job-snippet"),
                            By.cssSelector("[class*='snippet']")
                    );

                    String postedDate = findElementText(job,
                            By.xpath(".//div[contains(@class, 'metadata')]/div[contains(@class, 'date')]"),
                            By.cssSelector("[data-testid='posted-date']"),
                            By.xpath(".//span[contains(@class, 'date')]"),
                            By.cssSelector(".date"),
                            By.cssSelector("[class*='date']"),
                            By.xpath(".//div[contains(text(), 'jour')]"),
                            By.xpath(".//div[contains(text(), 'days')]")
                    );


                    System.out.println("Full Job Element HTML:");
                    System.out.println(job.getAttribute("outerHTML"));

                    System.out.println("Job Title: " + jobTitle);
                    System.out.println("Company: " + companyName);
                    System.out.println("Location: " + location);
                    System.out.println("Summary: " + summary);
                    System.out.println("Posted Date: " + postedDate);
                    System.out.println("-----");
                } catch (Exception e) {
                    System.out.println("Error processing a job listing: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // Flexible method to find element text with multiple locator strategies
    private static String findElementText(WebElement parent, By... locators) {
        for (By locator : locators) {
            try {
                WebElement element = parent.findElement(locator);
                String text = element.getText().trim();
                return text.isEmpty() ? "N/A" : text;
            } catch (Exception ignored) {}
        }
        return "N/A"; // Return default if no elements found
    }
}