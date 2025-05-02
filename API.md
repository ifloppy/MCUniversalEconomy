# MCUniversalEconomy API Documentation

This document provides comprehensive guidance on integrating with MCUniversalEconomy using its native API. The API is designed to work seamlessly across both FabricMC and PaperMC platforms.

## Getting Started

### Adding as a Dependency

#### For Gradle Projects

**Step 1: Add JitPack repository to your `settings.gradle`**

```groovy
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2: Add the dependency to your `build.gradle`**

```groovy
dependencies {
    implementation 'com.github.ifloppy:MCUniversalEconomy:-SNAPSHOT'
}
```

#### For Maven Projects

**Step 1: Add JitPack repository to your `pom.xml`**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**Step 2: Add the dependency to your `pom.xml`**

```xml
<dependencies>
    <dependency>
        <groupId>com.github.ifloppy</groupId>
        <artifactId>MCUniversalEconomy</artifactId>
        <version>-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## API Usage

### Accessing the API

The UniversalEconomyAPI can be accessed through its singleton instance:

```java
import com.iruanp.mcuniversaleconomy.api.UniversalEconomyAPI;
import com.iruanp.mcuniversaleconomy.api.UniversalEconomyAPIImpl;

UniversalEconomyAPI economyAPI = UniversalEconomyAPIImpl.getInstance();
```

### Available Methods

#### Get Player Balance
```java
BigDecimal balance = economyAPI.getBalance(playerUUID);
```

#### Set Player Balance
```java
boolean success = economyAPI.setBalance(playerUUID, new BigDecimal("1000.0"));
```

#### Deposit Money
```java
boolean success = economyAPI.depositPlayer(playerUUID, new BigDecimal("100.0"));
```

#### Withdraw Money
```java
boolean success = economyAPI.withdrawPlayer(playerUUID, new BigDecimal("50.0"));
```

#### Transfer Money Between Players
```java
boolean success = economyAPI.transferMoney(senderUUID, receiverUUID, new BigDecimal("75.0"));
```

#### Check if Player Has Enough Money
```java
boolean hasEnough = economyAPI.hasEnough(playerUUID, new BigDecimal("500.0"));
```

#### Get Currency Symbol
```java
String symbol = economyAPI.getCurrencySymbol();
```

#### Format Amount
```java
String formatted = economyAPI.formatAmount(new BigDecimal("1234.56"));
```

## Example Implementation

Here's a complete example showing how to use the API in your plugin or mod:

```java
import com.iruanp.mcuniversaleconomy.api.UniversalEconomyAPI;
import com.iruanp.mcuniversaleconomy.api.UniversalEconomyAPIImpl;

import java.util.UUID;

public class EconomyExample {
    private final UniversalEconomyAPI economyAPI;

    public EconomyExample() {
        this.economyAPI = UniversalEconomyAPIImpl.getInstance();
    }

    public void processPayment(UUID fromPlayer, UUID toPlayer, BigDecimal amount) {
        economyAPI.hasEnough(fromPlayer, amount).thenAcceptAsync(hasEnough -> {
            if (hasEnough) {
                economyAPI.transferMoney(fromPlayer, toPlayer, amount).thenAcceptAsync(result -> {
                    if (result.isSuccess()) {
                        String formattedAmount = economyAPI.formatAmount(amount);
                        System.out.println("Successfully transferred " + formattedAmount + " to " + toPlayer);
                        // Optionally notify the receiving player
                    } else {
                        System.out.println("Transfer failed: " + result.getMessage());
                        // Optionally notify the sending player about the failure
                    }
                });
            } else {
                System.out.println("Transfer failed: Insufficient funds.");
                // Optionally notify the sending player
            }
        });
    }

    public void displayBalance(UUID playerUUID) {
        BigDecimal balance = economyAPI.getBalance(playerUUID);
        String formatted = economyAPI.formatAmount(balance);
        System.out.println("Current balance: " + formatted);
    }
}
```
