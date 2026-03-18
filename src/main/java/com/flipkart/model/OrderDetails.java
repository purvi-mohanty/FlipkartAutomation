package com.flipkart.model;

import java.util.List;

public class OrderDetails {
    private String firstName, lastName;
    private String address1, address2;
    private String city, state, zip, phone;
    private String cardName, cardNumber, cardExpiry, cardCvv;
    private String deliveryOption;
    private List<CartItem> items;

    public String getFirstName()     { return firstName; }
    public void   setFirstName(String v) { firstName = v; }
    public String getLastName()      { return lastName; }
    public void   setLastName(String v)  { lastName = v; }
    public String getAddress1()      { return address1; }
    public void   setAddress1(String v)  { address1 = v; }
    public String getAddress2()      { return address2; }
    public void   setAddress2(String v)  { address2 = v; }
    public String getCity()          { return city; }
    public void   setCity(String v)      { city = v; }
    public String getState()         { return state; }
    public void   setState(String v)     { state = v; }
    public String getZip()           { return zip; }
    public void   setZip(String v)       { zip = v; }
    public String getPhone()         { return phone; }
    public void   setPhone(String v)     { phone = v; }
    public String getCardName()      { return cardName; }
    public void   setCardName(String v)  { cardName = v; }
    public String getCardNumber()    { return cardNumber; }
    public void   setCardNumber(String v){ cardNumber = v; }
    public String getCardExpiry()    { return cardExpiry; }
    public void   setCardExpiry(String v){ cardExpiry = v; }
    public String getCardCvv()       { return cardCvv; }
    public void   setCardCvv(String v)   { cardCvv = v; }
    public String getDeliveryOption()         { return deliveryOption; }
    public void   setDeliveryOption(String v) { deliveryOption = v; }
    public List<CartItem> getItems()          { return items; }
    public void           setItems(List<CartItem> v) { items = v; }
    public String getFullName() { return firstName + " " + lastName; }
}
