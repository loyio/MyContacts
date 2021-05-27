package me.loyio.mycontacts.VO;

public class Contact {
    private String name,phone,email,qq;
    private Boolean isChecked;

    public Contact(String name, String phone, String email, String qq, Boolean isChecked) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.qq = qq;
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }
}
