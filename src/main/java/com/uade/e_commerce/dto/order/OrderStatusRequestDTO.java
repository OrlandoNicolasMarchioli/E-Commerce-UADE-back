package com.uade.e_commerce.dto.order;

import lombok.Data;

// The new status arrives as String and not as OrderStatus on purpose: if it
// were the enum, an unknown value would fail while Jackson deserializes,
// before reaching the service, and it would come out as a 500. Receiving
// text lets the service validate it and answer 400 with a clear message.
@Data
public class OrderStatusRequestDTO {

    private String status;
}
