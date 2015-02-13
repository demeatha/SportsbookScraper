package sbs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.Cookie;
import java.util.List;
import sbs.config.*;
import sbs.model.*;
import sbs.crawler.*;
import sbs.account.*;
import org.openqa.selenium.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import sportingbet.SportingBetDataMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.File;
import org.openqa.selenium.TakesScreenshot;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;


import java.util.List;
import java.util.ArrayList;

import java.util.Properties;

public class SportsbookCrawlerMain {
    private static WebDriver driver;
    private static JSONCfg cfg;
    
    //Test the cfg package
	public static void main(String[] args) {
		//==================================================================Setup the driver==================================================================
		DesiredCapabilities caps = DesiredCapabilities.phantomjs();
		caps.setJavascriptEnabled(true);
		caps.setCapability("takesScreenshot", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {"--web-security=false", "--ssl-protocol=any", "--ignore-ssl-errors=true"});
		//caps.setCapability("acceptSslCerts", true);
		//This line will start phantomjs service
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"phantomJS/phantomjs-1.9.7-linux-x86_64/bin/phantomjs");
		driver = new PhantomJSDriver(caps);
		// Configuration to get better screenshots
		driver.manage().window().maximize();

		//===============================================================Load the configuration===============================================================
		cfg = JSONCfg.create("resources/crawler.json");

		driver.get(cfg.driver("SportingBetDriver").url());

		// Login the customer
		SportsbookLogin sportingbetLogin = new SportsbookLogin(driver, "SportingBetDriver", cfg);
		sportingbetLogin.doSportsbookLogin();
		
		//================================================================Initialise crawling algorithm========================================
		// Create the mapper for Sportingbet
		DataMapper sportingbet = new SportingBetDataMapper(cfg);

		// Get the crawler singleton
		SportsbookCrawler crawler = SportsbookCrawler.create(driver,cfg);

		// Register the data mapper into the crawler component
		crawler.addDataMapper("SportingBetDriver", sportingbet);		

		//================================================================= Get the classes=====================================================
		List<Object> results = crawler.crawlHTML("EvClass","SportingBetDriver",cfg.driver("SportingBetDriver").url());
		Map<String,List<String>> resultData = (Map<String,List<String>>)results.get(0);
		int count = 1;
		System.out.println("Available classes");
		for(String name : resultData.get("name")) {
			System.out.println(count + ": " + name);
			++count;
		}
		
		int index = 0;
		String input = SportsbookCrawlerMain.readInput("Choose class");
		index = Integer.parseInt(input) - 1;
		
		String url = resultData.get("url").get(index);

		//================================================================= Get the types=====================================================
		System.out.println("Available Types");
		results = crawler.crawlHTML("EvType","SportingBetDriver",url);
		resultData = (Map<String,List<String>>)results.get(0);
		count = 1;
		System.out.println("Choose type");
		for(String name : resultData.get("name")) {
			System.out.println(count + ": " + name);
			++count;
		}
		
		input = SportsbookCrawlerMain.readInput("Choose Type");
		index = Integer.parseInt(input) - 1;
		
		url = resultData.get("url").get(index);
		
		//================================================================= Get the events=====================================================
		results = crawler.crawlHTML("Event","SportingBetDriver",url);
		resultData = (Map<String,List<String>>) results.get(0);
		count = 1;
		System.out.println("Available Events");
		for(String name : resultData.get("name")) {
			System.out.println(count + ": " + name);
			++count;
		}

		input = SportsbookCrawlerMain.readInput("Choose Event");
		index = Integer.parseInt(input) - 1;
		
		url = resultData.get("url").get(index);
		
		//==================================================== Get the market selection pairs=====================================================
		System.out.println("Available Market selections");
		results = crawler.crawlHTML("EvMarketSelection","SportingBetDriver",url);
		System.out.println(Arrays.toString(results.toArray()));


		//=================================================================Prepare bet placement=====================================================
		SportsbookBetslip bslip = new SportsbookBetslip(driver, "SportingBetDriver", cfg);
		input = SportsbookCrawlerMain.readInput("Choose selection id for the betslip");
		bslip.addToBetslip(input);

		// Wait 2 secs to load into betslip before you continue
		SportsbookCrawlerMain.wait(2000);
		
		SportsbookCrawlerMain.printScreen("betPlacement.png");

		// Get the description
		System.out.println(bslip.getSelectionDescriptions().get(0));

		// Add the amount you want to bet on
		input = SportsbookCrawlerMain.readInput("Please add the amount");
		bslip.addAmount(1,Float.parseFloat(input));
		
		SportsbookCrawlerMain.printScreen("amount.png");
		SportsbookCrawlerMain.wait(1000);

		// And finally place the bet
		bslip.placeBet();
		
		SportsbookCrawlerMain.wait(1000);
		SportsbookCrawlerMain.printScreen("result.png");

		driver.quit();
    }

    private static String readInput(String description) {
	    System.out.print(description + ": ");
	    try{
		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    String input;
		    if((input=br.readLine())!=null) {
			    return input;
		    }
	    }catch(IOException io){
		    io.printStackTrace();
	    }
	    return null;
    }

    private static void wait(int m) {
	    try {
		    Thread.sleep(m);
	    } catch(Exception ex) {
		    ex.printStackTrace();
	    }
    }

    private static void printScreen(String fname) {
	    try {
		    File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		    FileUtils.copyFile(scrFile, new File(fname), true);
	    } catch(IOException e) {
		    e.printStackTrace();
	    }
    }
}
