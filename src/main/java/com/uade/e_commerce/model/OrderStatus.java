package com.uade.e_commerce.model;

/**
 * The lifecycle of an order. An order is born as PENDING and from there it
 * can only move forward along the flow, or be cancelled while it hasn't
 * been shipped yet:
 *
 * PENDING -> PAID -> SHIPPED -> DELIVERED
 *    |         |
 *    +---------+--> CANCELLED
 *
 * DELIVERED and CANCELLED are final states: nothing comes after them.
 */
public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    // The rule about which transitions are allowed lives here and not in the
    // service, because it's knowledge that belongs to the state itself. This
    // way there's a single place to look at (or to change) when the flow of
    // the order changes.
    public boolean canTransitionTo(OrderStatus target) {

        // Moving to the same state isn't an error worth applying: it changes
        // nothing, so it's rejected to avoid silent no-op updates.
        if (target == null || target == this) {
            return false;
        }

        return switch (this) {
            case PENDING -> target == PAID || target == CANCELLED;
            case PAID -> target == SHIPPED || target == CANCELLED;
            case SHIPPED -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    // Once the order is shipped the stock already left the warehouse, so
    // cancelling stops being possible.
    public boolean isCancellable() {
        return canTransitionTo(CANCELLED);
    }
}
