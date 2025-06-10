package com.github.dimitryivaniuta.bankaccount.model;

/**
 * Enumeration of supported bank account operation types.
 * <p>
 * Defines the kinds of transactions that can be performed on an account:
 * <ul>
 *   <li>{@link #DEPOSIT}: adds funds to the account balance.</li>
 *   <li>{@link #WITHDRAWAL}: subtracts funds from the account balance.</li>
 * </ul>
 * </p>
 */
public enum OperationType {

    /**
     * Represents a deposit operation, increasing the account balance.
     */
    DEPOSIT,

    /**
     * Represents a withdrawal operation, decreasing the account balance.
     */
    WITHDRAWAL;
}