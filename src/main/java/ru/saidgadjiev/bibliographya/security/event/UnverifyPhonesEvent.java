package ru.saidgadjiev.bibliographya.security.event;

public class UnverifyPhonesEvent {

    private String phone;

    public UnverifyPhonesEvent(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
