package sbs.account;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import sbs.config.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;

/*
 * This component provides all functionality that is related with Sportsbook Betslip
 * betslipView, amountField and placeButton come from configuration and give the needfull xpaths so as
 * module be able to give bet placement, add betslip and display betslip details features.
 *
 * You create one instance for each sportsbook site you want to parse.
 */
public class SportsbookBetslip {
	private WebDriver webDriver;
	private SportsbookBetslipJsonObject betslipConfig;

	private SportsbookBetslip() {}

	public SportsbookBetslip(WebDriver webDriver, String driverNameConfig, JSONCfg cfg) {
		this.webDriver = webDriver;
		this.betslipConfig = cfg.driver(driverNameConfig).betslip();
	}

	/*
	 * A helper method that will place a bet only selection identifier is an id html attribute
	 */
	public boolean addToBetslip(String selectionIdentifier) {
		return this.addToBetslip("div", "id", selectionIdentifier);
	}

	/*
	 * This method is responsible to add into betslip the selections you've chosen.
	 */
	public boolean addToBetslip(String htmlElement, String htmlAttribute, String selectionIdentifier) {
		//Construct the xpath
		WebElement selection = webDriver.findElement(By.xpath("//"+htmlElement+"[@"+htmlAttribute+" = "+"'"+selectionIdentifier+"']"));
		
		//Find the button, we use findElements to determine if the element was found or not so as we continue with another element
		// which may represents the button inot defined selection. We don't check for submit because all betslip are js based so these
		// are the elements that use as clickable thingies.
		List<WebElement> canditateLinks = selection.findElements(By.tagName("a"));
		List<WebElement> canditateButtons = selection.findElements(By.tagName("button"));
		if(canditateLinks.size() == 1) {
			canditateLinks.get(0).click();
		} else if (canditateButtons.size() == 1) {
			canditateButtons.get(0).click();
		} else {
			// Error out this crap shoudldn't happen
			System.out.println("ERROR: No button identified for given id: " + selectionIdentifier);
			return false;
		}
		return true;
	}

	/*
	 * This method gets from config the placeBetButton xpath and clicks on that element
	 */
	public boolean placeBet() {
		List<WebElement> button = webDriver.findElements(By.xpath(betslipConfig.placeBetButton()));
		if(button.size() == 0) {
			System.out.println("WARNING: betslip button not found, please check your config, or your betslip");
			return false;
		}
		button.get(0).click();
		return true;
	}

	/*
	 * This method will return a list of Strings that each one will represent selection's description.
	 * With that way will be able to choose the selection you want to put the amount on.
	 */
	public List<String> getSelectionDescriptions() {
		List<String> result = new ArrayList<String>();
		
		List<String> selectionDescriptionXpaths = betslipConfig.selectionDescription();
		for(int i = 0; i < selectionDescriptionXpaths.size(); i++) {
			String xpath = selectionDescriptionXpaths.get(i);
			List<WebElement> elements = webDriver.findElements(By.xpath(xpath));
			for(int j = 0; j < elements.size(); j++) {
				WebElement element = elements.get(j);
				if(i > 0) {
					if(j < result.size()) {
						result.set(j, result.get(j) + " " + element.getText());
					} else {
						result.add(j, element.getText());
					}
				} else {
					result.add(j, element.getText());
				}
			}
		}
		return result;
	}

	/*
	 * This method will add the amount you want in the selection you've chosen.
	 */
	public boolean addAmount(int index, float amount) {
		List<WebElement> amounts = webDriver.findElements(By.xpath(betslipConfig.amount()));
		if(amounts.size() == 0) {
			System.out.println("ERROR: No amounts where found");
			return false;
		} else if(index > amounts.size() || index < 1) {
			System.out.println("ERROR: wrong index number of amount set");
		}
		amounts.get(index-1).sendKeys(Float.toString(amount));
		return true;
	}

	/*
	 * A replacement of drivers regular click, in case click method is not working on PhantomJs
	 * This is a workaround so as I can make the click operational.
	 */
	private void actionClickOn(WebElement element) {
		Actions act = new Actions(webDriver);
		act.moveToElement(element).click().perform();
	}
}
