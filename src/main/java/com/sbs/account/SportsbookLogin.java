package sbs.account;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import sbs.config.*;
import org.openqa.selenium.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SportsbookLogin {
	SportsbookLoginJsonObject loginData = null;
	WebDriver webDriver = null;

	public SportsbookLogin(WebDriver webDriver, String driverNameConfig, JSONCfg cfg) {
		// Get the config xpaths and credentials
		loginData = cfg.driver(driverNameConfig).login();
		this.webDriver = webDriver;
	}

	/*
	 * This is a simple login method which takes all xpath elemetns from configuration and put the
	 * right data into login form. 
	 * Details are submited and customer gets logged in.
	 */
	public boolean doSportsbookLogin() {
		// Get the login form
		WebElement element = webDriver.findElement(By.xpath(loginData.loginStartButton()));
		element.click();

		// Write the username 
		element = webDriver.findElement(By.xpath(loginData.loginUsername("xpath")));
		element.sendKeys(loginData.loginUsername("credentials"));

		// Write the password
		element = webDriver.findElement(By.xpath(loginData.loginPassword("xpath")));
		element.sendKeys(loginData.loginPassword("credentials"));

		// Click the login button
		element = webDriver.findElement(By.xpath(loginData.loginButton()));
		element.click();


		// Confirm that customer has logged in
		List<WebElement> elements =  webDriver.findElements(By.xpath(loginData.proofOfLogin()));
		if(elements.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
}