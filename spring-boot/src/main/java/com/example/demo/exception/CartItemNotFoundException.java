package com.example.demo.exception;

import java.util.List;

public class CartItemNotFoundException extends RuntimeException {
    private final List<Long> missingIds;

    public CartItemNotFoundException(List<Long> missingIds) {
        super("購物車無該商品: " + missingIds);
        this.missingIds = missingIds;
    }

    public List<Long> getMissingIds() {
        return missingIds;
    }
}
