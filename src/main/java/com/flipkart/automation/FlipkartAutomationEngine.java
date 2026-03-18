package com.flipkart.automation;

import com.flipkart.model.CartItem;
import com.flipkart.model.OrderDetails;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * FlipkartAutomationEngine
 *
 * Drives Chrome through the full Flipkart Clone shopping flow:
 *  1. Open home page
 *  2. Click a category
 *  3. Add selected products to cart
 *  4. Go to cart and review
 *  5. Proceed to checkout
 *  6. Fill shipping address + payment
 *  7. Place the order
 *  8. Confirm the order on confirmation page
 */
public class FlipkartAutomationEngine {

    private WebDriver     driver;
    private WebDriverWait wait;
    private final String  baseUrl;

    private Consumer<String> logger;
    private Consumer<String> statusUpdater;
    private volatile boolean cancelled = false;

    public FlipkartAutomationEngine(String baseUrl) { this.baseUrl = baseUrl; }

    public void setLogger(Consumer<String> l)        { this.logger = l; }
    public void setStatusUpdater(Consumer<String> s) { this.statusUpdater = s; }
    public void cancel()                             { this.cancelled = true; }

    private void log(String msg) {
        if (logger != null) logger.accept(msg);
        System.out.println("[FK-BOT] " + msg);
    }
    private void status(String msg) { if (statusUpdater != null) statusUpdater.accept(msg); }

    // -----------------------------------------------------------------------
    public String runFullAutomation(OrderDetails order, String[] productIds, boolean headless)
            throws Exception {
        cancelled = false;
        initDriver(headless);
        try {
            status("Step 1/6: Opening Flipkart Clone...");
            openHomePage();
            checkCancelled();

            status("Step 2/6: Browsing Electronics category...");
            clickCategory("cat-electronics");
            checkCancelled();

            status("Step 3/6: Adding products to cart...");
            List<CartItem> addedItems = new ArrayList<>();
            for (String pid : productIds) {
                checkCancelled();
                CartItem item = addProductToCart(pid);
                if (item != null) {
                    addedItems.add(item);
                    log("  + Added: " + item.getName() + " @ " + item.getPrice());
                    sleep(700);
                }
            }
            order.setItems(addedItems);

            status("Step 4/6: Reviewing cart...");
            goToCart();
            checkCancelled();
            sleep(1000);

            status("Step 5/6: Proceeding to checkout...");
            proceedToCheckout();
            checkCancelled();
            sleep(800);

            status("Step 6/6: Filling address, payment & placing order...");
            fillCheckoutAndPlaceOrder(order);
            checkCancelled();

            sleep(1500);
            String orderId = getConfirmedOrderId();
            status("Order confirmed! ID: " + orderId);
            log("==========================================");
            log("  FLIPKART ORDER CONFIRMED: " + orderId);
            log("==========================================");
            sleep(3000);
            return orderId;

        } finally {
            if (driver != null) { driver.quit(); driver = null; }
        }
    }

    // -----------------------------------------------------------------------
    private void initDriver(boolean headless) {
        log("Initialising ChromeDriver...");
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        if (headless) opts.addArguments("--headless=new");
        opts.addArguments("--start-maximized", "--disable-extensions");
        driver = new ChromeDriver(opts);
        wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        log("Chrome browser launched.");
    }

    private void openHomePage() throws Exception {
        log("Opening: " + baseUrl + "/index.html");
        driver.get(baseUrl + "/index.html");
        wait.until(ExpectedConditions.titleContains("Flipkart"));
        sleep(800);
        log("Flipkart home page loaded.");
    }

    private void clickCategory(String categoryId) throws Exception {
        log("Clicking category: " + categoryId);
        try {
            WebElement cat = wait.until(
                ExpectedConditions.elementToBeClickable(By.id(categoryId))
            );
            cat.click();
            sleep(500);
            log("  Category clicked: " + cat.getText() + " ✓");
        } catch (Exception e) {
            log("  WARNING: Category click failed, continuing...");
        }
    }

    private CartItem addProductToCart(String buttonId) throws Exception {
        log("Adding product: " + buttonId);
        try {
            WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id(buttonId))
            );
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn
            );
            sleep(300);

            String cardId = buttonId.replace("add-to-cart-", "product-");
            WebElement card  = driver.findElement(By.id(cardId));
            String name      = card.findElement(By.className("p-name")).getText();
            String price     = card.findElement(By.className("p-price")).getText().replaceAll("\\s.*","");

            btn.click();
            sleep(500);

            // Check notification
            try {
                WebElement notif = driver.findElement(By.id("cart-notification"));
                wait.until(ExpectedConditions.visibilityOf(notif));
            } catch (Exception ignored) {}

            return new CartItem(name, price, 1);

        } catch (NoSuchElementException | TimeoutException e) {
            log("  WARNING: Could not find " + buttonId);
            return null;
        }
    }

    private void goToCart() throws Exception {
        log("Navigating to cart...");
        WebElement cartLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("cart-link"))
        );
        cartLink.click();
        wait.until(ExpectedConditions.titleContains("Cart"));
        sleep(600);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".cart-item")));
            int count = driver.findElements(By.cssSelector(".cart-item")).size();
            log("  Cart has " + count + " item(s) ✓");
        } catch (TimeoutException e) {
            log("  WARNING: Cart items not visible");
        }
    }

    private void proceedToCheckout() throws Exception {
        log("Clicking 'PLACE ORDER' / 'Proceed to Checkout'...");
        WebElement btn = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("proceed-to-checkout"))
        );
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});", btn
        );
        sleep(300);
        btn.click();
        wait.until(ExpectedConditions.titleContains("Checkout"));
        sleep(800);
        log("  Checkout page loaded ✓");
    }

    private void fillCheckoutAndPlaceOrder(OrderDetails order) throws Exception {
        log("Filling delivery address...");
        fillField("first-name", order.getFirstName());
        fillField("last-name",  order.getLastName());
        fillField("phone",      order.getPhone());
        fillField("zip",        order.getZip());
        fillField("address1",   order.getAddress1());
        if (order.getAddress2() != null && !order.getAddress2().isEmpty())
            fillField("address2", order.getAddress2());
        fillField("city", order.getCity());

        // State dropdown
        new Select(driver.findElement(By.id("state"))).selectByValue(order.getState());
        sleep(200);
        log("  Address filled ✓");

        // Delivery option
        String radioId = "delivery-express".equals(order.getDeliveryOption())
                         ? "delivery-express" : "delivery-standard";
        WebElement radio = driver.findElement(By.id(radioId));
        if (!radio.isSelected())
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radio);
        log("  Delivery: " + order.getDeliveryOption() + " ✓");

        // Payment - card
        log("Filling payment details...");
        fillField("card-name",   order.getCardName());
        fillField("card-number", formatCard(order.getCardNumber()));
        fillField("card-expiry", order.getCardExpiry());
        fillField("card-cvv",    order.getCardCvv());
        log("  Payment filled ✓");

        sleep(400);

        // Place order
        log("Clicking 'CONFIRM ORDER'...");
        WebElement placeBtn = driver.findElement(By.id("place-order-btn"));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});", placeBtn
        );
        sleep(500);
        placeBtn.click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.titleContains("Confirmed"),
            ExpectedConditions.urlContains("confirmation.html")
        ));
        log("  Order placed ✓");
    }

    private String getConfirmedOrderId() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("confirm-order-id")));
            sleep(2000);
            String text = driver.findElement(By.id("confirm-order-id")).getText();
            log("  Order ID element text: " + text);
            return text.isBlank() ? "FK-CONFIRMED" : text;
        } catch (Exception e) { return "FK-CONFIRMED"; }
    }

    // -----------------------------------------------------------------------
    private void fillField(String id, String value) throws Exception {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
        el.clear();
        el.sendKeys(value);
        sleep(100);
    }

    private String formatCard(String raw) {
        String d = raw.replaceAll("\\D","");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void checkCancelled() throws Exception {
        if (cancelled) throw new Exception("Automation cancelled by user.");
    }
}
