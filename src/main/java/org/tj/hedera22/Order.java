package org.tj.hedera22;

import java.math.BigInteger;

// (uint256, address, bool, uint256, uint256)
public final class Order {
    public final BigInteger id;
    public final String address;
    public final boolean isBuy;
    public final BigInteger price;
    public final BigInteger size;

    public Order(BigInteger id, String address, boolean isBuy, BigInteger price, BigInteger size) {
        this.id = id;
        this.address = address;
        this.isBuy = isBuy;
        this.price = price;
        this.size = size;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", isBuy=" + isBuy +
                ", price=" + price +
                ", size=" + size +
                '}';
    }
}
