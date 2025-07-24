package com.scrap.journal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class SeleniumService {
	
	public static void main(String[] args) throws InterruptedException {
		SeleniumService s = new SeleniumService();
		s.saudijournalOfAnesthesia();
	}
	
	
	public void saudijournalOfAnesthesia() throws InterruptedException {
		WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        
        driver.get("https://journals.lww.com/sjan/pages/default.aspx");
        Thread.sleep(2000);
        driver.findElement(By.xpath("/html/body/div[5]/div[2]/div/div/div[2]/div/div/button[2]")).click();
        Thread.sleep(2000);
        driver.findElement(By.id("ctl00_ctl51_Header_SearchTopBoxControl_txtKeywords")).sendKeys("Peracetamol"+Keys.ENTER);
        Thread.sleep(3000);
        //click on current ussue
        WebElement element = driver.findElement(By.xpath("/html/body/form/div[5]/div/div/div[4]/div[1]/div[3]/div/div/div/div/div/div[1]/div[2]/section/div[2]/div[1]/div[2]/ul/li[9]/div/div/ul/li[2]/label"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        js.executeScript("arguments[0].click();", element);
        
        Thread.sleep(3000);
        
        String totalResultString = driver.findElement(By.id("ctl00_ctl29_g_d7ffb995_7332_4a77_a0e7_695e0137e9d4_ctl00_ResultsStatus")).getText();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(totalResultString);

        int n = 0;
        if (matcher.find()) {
        	n = Integer.parseInt(matcher.group());
            System.out.println("Extracted number: " + n);
        } else {
            System.out.println("No number found.");
        }
        if(n > 20 ) n = 20;
        int j = 1;
        List<String> searchResults = new ArrayList<>();
        for(int i = 0; i < n; i++) {
        	String link = driver.findElement(By.xpath("/html/body/form/div[5]/div/div/div[4]/div[1]/div[3]/div/div/div/div/div/div[1]/div[2]/section/div[2]/div[2]/div/span/div[1]/div["+j+"]/article/div/div[2]/ul[2]/li/div/a")).getAttribute("href");
        	searchResults.add(link);
        	j = j +2;	
        }
      
        System.out.println(searchResults);
        
        for(String result: searchResults) {
        	driver.get(result);
        	Thread.sleep(5000);
        	try {
        		String doi  = driver.findElement(By.xpath("/html/body/form/div[5]/div/div/div[4]/div[1]/div[3]/div/div[1]/div/div/div/div/div[1]/div[1]/div[5]/article/section[4]/div")).getText();
        		System.out.println("Doi : "+doi);
        		
        	} catch(Exception e) {
        	
        	}
        	try {
        		String title  = driver.findElement(By.xpath("/html/body/form/div[5]/div/div/div[4]/div[1]/div[3]/div/div[1]/div/div/div/div/div[1]/div[1]/div[5]/article/header/h1")).getText();
        		System.out.println("Title : "+title);
        		
        	} catch(Exception e) {
        		
        	}
        	try {
        		String Abstract  = driver.findElement(By.xpath("/html/body/form/div[5]/div/div/div[4]/div[1]/div[3]/div/div[1]/div/div/div/div/div[1]/div[1]/div[5]/article/section[6]/div/div/div/p")).getText();
        		System.out.println("Abstract : " +Abstract);
        		
        	} catch(Exception e) {
        		
        	}
        	try {
        		String authors  = driver.findElement(By.xpath("/html/body/form/div[5]/div/div/div[4]/div[1]/div[3]/div/div[1]/div/div/div/div/div[1]/div[1]/div[5]/article/section[2]/p")).getText();
        		System.out.println("Authors : "+authors);
        		
        	} catch(Exception e) {
        		
        	}	
        }
	}
	
	

    private WebDriver driver;

    @PostConstruct
    public void setupDriver() {
    	WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
      
    }
    public String openJournal() {
    	WebElement TargetElement;
    	driver.get("https://onlinelibrary.wiley.com/index/3037");
    	driver.findElement(By.xpath("/html/body/div[2]/div/div[2]/main/div/div/section/div/div/div/div[1]/div/div/div[2]/div/form/div/input[3]")).sendKeys("Fever" + Keys.ENTER);
        
    	
    	return "done";
    }

    public String openGoogle() {
    	WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get("https://www.linkedin.com");
        WebElement element = driver.findElement(By.className("sign-in-form__sign-in-cta"));
        element.click();
        String email = "sifibe4560@dosonex.com";
        String password = "alite123";
        
        element = driver.findElement(By.id("username"));
        element.sendKeys(email);
        element = driver.findElement(By.id("password"));
        element.sendKeys(password);
        
        element = driver.findElement(By.className("login__form_action_container"));
        element.click();
        
        WebElement search = driver.findElement(By.className("search-global-typeahead__input"));
        search.sendKeys("Harman" + Keys.ENTER);
        
        
        return driver.getTitle();
    }

    @PreDestroy
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
