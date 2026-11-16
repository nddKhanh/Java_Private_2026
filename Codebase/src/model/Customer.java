package model;

public class Customer {
    private String id;
    private String name;
    private String phone;

    public Customer(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            System.out.println("Mã khách hàng không được rỗng");
            return;
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            System.out.println("Tên khách hàng không được rỗng");
            return;
        }
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            System.out.println("Số điện thoại không được rỗng");
            return;
        }
        this.phone = phone;
    }

    @Override
    public String toString() {
        return id
                + " | Tên: " + name
                + " | Số điện thoại: " + phone;
    }
}
