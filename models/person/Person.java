package models.person;
public abstract class Person {
    protected String personId;
    protected String fullName;
    protected String phoneNumber;
    protected String email;
    protected String address;
    
    public Person(String personId, String fullName, String phoneNumber, 
                  String email, String address) {
        if (personId == null || personId.trim().isEmpty()) {
            throw new IllegalArgumentException("Person ID cannot be empty");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        
        this.personId = personId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }
    
    // Getters and setters 
    public String getPersonId() { return personId; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    
    
    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        this.fullName = fullName;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        this.phoneNumber = phoneNumber;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    // Abstract method each person type describes their role differently
    public abstract String getRole();
    
    @Override
    public String toString() {
        return "ID: " + personId + 
               ", Name: " + fullName + 
               ", Phone: " + phoneNumber + 
               ", Email: " + (email != null ? email : "N/A") +
               ", Address: " + (address != null ? address : "N/A");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return this.personId.equals(person.personId);
    }
    
    @Override
    public int hashCode() {
        return personId.hashCode();
    }
}
