package com.uade.e_commerce.model;

/**
 * Product type: defines whether it's a physical item that gets shipped
 * (with stock) or a service (private lessons, courses) that doesn't
 * manage stock.
 */

public enum ProductType {
    PHYSICAL,
    SERVICE
}
