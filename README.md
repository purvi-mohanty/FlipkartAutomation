# 🛒 Flipkart Clone – Full Selenium Automation App
### Java Swing + Selenium 4 + NetBeans

![Java](https://img.shields.io/badge/Java-11%2B-orange?style=flat-square&logo=java)
![Selenium](https://img.shields.io/badge/Selenium-4.x-green?style=flat-square&logo=selenium)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square&logo=apachemaven)
![NetBeans](https://img.shields.io/badge/NetBeans-12%2B-blue?style=flat-square)
![Chrome](https://img.shields.io/badge/Chrome-Latest-yellow?style=flat-square&logo=googlechrome)

---

## 📁 Project Structure
```
FlipkartAutomation/
├── pom.xml
├── webapp/                              ← Flipkart Clone web pages
│   ├── index.html                       ← Home page with Add to Cart buttons
│   ├── cart.html                        ← Shopping cart page
│   ├── checkout.html                    ← Checkout form (shipping + payment)
│   ├── confirmation.html                ← Order confirmation page
│   ├── css/style.css
│   ├── js/app.js
│   └── img/                             ← All product images
└── src/main/java/com/flipkart/
    ├── app/
    │   ├── MainApp.java                 ← 🖥 Java Swing desktop application (MAIN)
    │   └── LocalWebServer.java          ← Embedded HTTP server (serves webapp)
    ├── automation/
    │   └── FlipkartAutomationEngine.java  ← All Selenium automation logic
    └── model/
        ├── CartItem.java                ← Cart item data model
        └── OrderDetails.java            ← Order details data model
```

---

## 🚀 How to Run in NetBeans

### Step 1: Open the Project
```
File → Open Project → Select FlipkartAutomation folder
```

### Step 2: Build with Dependencies
```
Right-click pom.xml → Build with Dependencies
```
> Maven will automatically download: **Selenium 4**, **WebDriverManager**

### Step 3: Run the App
```
Right-click MainApp.java → Run File
```
OR
```
Right-click project → Run
```

---

## 🖥 App Interface
```
┌──────────────────────────────────────────────────────────────┐
│  🛒 Flipkart Clone – Full Selenium Automation                │
├───────────────────────────┬──────────────────────────────────┤
│  ⚙ Webapp Configuration  │  📋 Automation Log               │
│  [Browse] [▶ Start Server]│  (green console output)          │
│                           │                                  │
│  🛍 Select Products       │                                  │
│  ☑ Wireless Earbuds       │                                  │
│  ☐ Smart Watch            │  ████████████ 67% Running...     │
│                           │                                  │
│  📦 Shipping Address      │  [▶ RUN] [■ STOP] [✕ CLEAR]     │
│  💳 Payment Details       │                                  │
└───────────────────────────┴──────────────────────────────────┘
```

### Steps to Use:

| # | Step | Description |
|---|------|-------------|
| 1 | **Set webapp path** | Type the path to the `webapp` folder or click **Browse** |
| 2 | **Start Server** | Click **▶ Start Server** to launch the local HTTP server |
| 3 | **Select products** | Tick which products to add to cart |
| 4 | **Fill shipping address** | Pre-filled with test data — edit as needed |
| 5 | **Fill payment details** | Pre-filled with test card `4111 1111 1111 1111` |
| 6 | **Run Automation** | Click **▶ RUN AUTOMATION** — browser opens and runs! |

---

## 🤖 What the Automation Does

| Step | Action |
|------|--------|
| 1 | Opens `http://localhost:8765/index.html` |
| 2 | Selects category from department dropdown |
| 3 | Clicks **Add to Cart** for each selected product |
| 4 | Navigates to cart page and verifies items |
| 5 | Clicks **Proceed to Checkout** |
| 6 | Fills in the complete shipping address form |
| 7 | Fills in credit card payment details |
| 8 | Clicks **Place Order** |
| 9 | Reads Order ID from the confirmation page |
| 10 | Shows success dialog with the Order ID |

---

## ⚙ Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 11+ | Must be set in `JAVA_HOME` |
| NetBeans IDE | 12+ | Any edition |
| Google Chrome | Latest | Auto-matched by WebDriverManager |
| Internet | Required | For CDN assets (jQuery / Bootstrap) |

> ✅ **ChromeDriver is downloaded automatically** by WebDriverManager — no manual setup needed.

---

## 🧩 File Reference

| File | Path |
|------|------|
| **Main App** *(run this)* | `src/main/java/com/flipkart/app/MainApp.java` |
| Automation Engine | `src/main/java/com/flipkart/automation/FlipkartAutomationEngine.java` |
| HTTP Server | `src/main/java/com/flipkart/app/LocalWebServer.java` |
| Cart Item Model | `src/main/java/com/flipkart/model/CartItem.java` |
| Order Details Model | `src/main/java/com/flipkart/model/OrderDetails.java` |
| Home Page | `webapp/index.html` |
| Cart Page | `webapp/cart.html` |
| Checkout Page | `webapp/checkout.html` |
| Confirmation Page | `webapp/confirmation.html` |
| Maven Config | `pom.xml` |

---

## 📦 Maven Dependencies (`pom.xml`)
```xml
<dependencies>
    <!-- Selenium WebDriver -->
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.x.x</version>
    </dependency>

    <!-- WebDriverManager (auto ChromeDriver) -->
    <dependency>
        <groupId>io.github.bonigarcia</groupId>
        <artifactId>webdrivermanager</artifactId>
        <version>5.x.x</version>
    </dependency>
</dependencies>
```

---

## 🐛 Troubleshooting

| Issue | Fix |
|-------|-----|
| Chrome doesn't open | Ensure Chrome is installed and up to date |
| Port 8765 already in use | Change port in `LocalWebServer.java` |
| Build fails | Run `mvn clean install` from the terminal |
| Elements not found | Check that the local server started successfully |
| WebDriver timeout | Increase implicit wait in `FlipkartAutomationEngine.java` |

---

## 📄 License

This project is for **educational and testing purposes only.**
The Flipkart clone web app is a local simulation — no real transactions are made.

---

> Built with ☕ Java, 🤖 Selenium 4, and 💙 NetBeans
