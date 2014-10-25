package sbs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import sbs.config.*;


import java.util.List;
import java.util.ArrayList;

import java.util.Properties;

public class SportsbookCrawler {
    private WebDriver driver;

    public SportsbookCrawler() {
    //TODO Add automated config handler to instantiate a variaty of drivers based on Sportsbook
    /*
	driverName: sportingbet
	EvClassXpath:{name:[], url:[] }
	EvTypeXpath:{name:[], url:[]}
	EvXpath:
	EvMarketXpath:
	EvOutcomeXpath:
    */
      DesiredCapabilities caps = new DesiredCapabilities();
      caps.setJavascriptEnabled(true);
      caps.setCapability("takesScreenshot", false);
      caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"phantomJS/phantomjs-1.9.7-linux-x86_64/bin/phantomjs");
      driver = new PhantomJSDriver(caps);
    }
    
    //Test the cfg package
    public static void main(String[] args) {
		Properties p=System.getProperties();
		System.out.println(p.stringPropertyNames().toString());
		System.out.println(p.getProperty("argLine"));
		JSONCfg cfg = JSONCfg.create(p.getProperty("app.config"));
		if (cfg != null) {
		  String json = cfg.getJSONObject().toString();
		  System.out.println(json);
		}
    }

    /*public static void main(String[] args) {
    
	// getTypes
        SportsbookCrawler cr = new SportsbookCrawler();
        cr.driver.get("http://www.sportingbet.gr/sports-football/0-102.html");
        
        // Wait till js load the types
	(new WebDriverWait(cr.driver, 10)).until(new ExpectedCondition<Boolean>() {
		public Boolean apply(WebDriver d) {
			if (d.findElements(By.id("events")).size() != 0) {
				return  true;
			}
			return false;
		}
	});

        List<String> evTypeElements = cr.driver.findElements(By.xpath("//div[@class='dd']/a"));
        for (WebElement e: evTypeElements) {
		//String html = e.getAttribute("innerHTML"));
		//JavascriptExecutor jsDriver = (JavascriptExecutor)cr.dirver;
		String name = e.getText();
		//String url  = e.getAttribute("href");
		
		System.out.println("EvType with name " + name +" and url " + url);
        }
        //List<EvType> evTypes = cr.driver.processEvTypes(evTypeElements); TODO
        //element.sendKeys("Cheese!");
        //element.submit();
 
        System.out.println("Page title is: " + cr.driver.getTitle());
         
        cr.driver.quit();
    }*/
    
   /* public List<EvType> getTypes(EvClass parent) {
    
    }*/
}
