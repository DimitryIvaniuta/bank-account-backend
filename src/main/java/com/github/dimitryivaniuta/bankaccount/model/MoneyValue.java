package com.github.dimitryivaniuta.bankaccount.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Convert;
import lombok.Getter;
import lombok.Setter;
import org.javamoney.moneta.Money;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;

/**
 * Embeddable representation of a monetary amount consisting of
 * a numeric value and an ISO-4217 currency.
 */
@Embeddable
@Getter
@Setter
public class MoneyValue {

    /**
     * Numeric value of the monetary amount.
     * Stored with precision suitable for currency (e.g., DECIMAL(19,4)).
     */
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    /**
     * ISO-4217 currency unit for this amount.
     * Stored as a 3-character code using a JPA converter.
     */
    @Convert(converter = CurrencyUnitConverter.class)
//    @Column(name = "currency", length = 3, nullable = false)
    @Column(
            name = "currency",
            nullable = false,
            columnDefinition = "CHAR(3)"
    )
    private CurrencyUnit currency;

    /**
     * Default constructor for JPA.
     * Initializes with zero amount in EUR.
     */
    public MoneyValue() {
        this.amount = BigDecimal.ZERO;
        this.currency = Monetary.getCurrency("EUR");
    }

    /**
     * Constructs a MoneyValue from given amount and currency code.
     *
     * @param amount   numeric value (≥ 0)
     * @param currency ISO-4217 currency code
     */
    public MoneyValue(BigDecimal amount, CurrencyUnit currency) {
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * Retrieves the embedded monetary amount as a JSR-354 MonetaryAmount.
     *
     * @return MonetaryAmount instance
     */
    public javax.money.MonetaryAmount toMonetaryAmount() {
        return Money.of(amount, currency);
    }

    /** Create a zero-EUR default. */
    public static MoneyValue zero() {
        return new MoneyValue(BigDecimal.ZERO, Monetary.getCurrency("EUR"));
    }

    /** Build from a JSR-354 MonetaryAmount. */
    public static MoneyValue from(MonetaryAmount amt) {
        return new MoneyValue(
                amt.getNumber().numberValueExact(BigDecimal.class),
                amt.getCurrency()
        );
    }
}
