package com.sportsmanager.core.model;

/**
 * Abstract base class for all persons in the system.
 * Both Player and Coach extend this class.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public abstract class Person {

    private String firstName;
    private String lastName;
    private int age;

    protected Person(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public int getAge()          { return age; }
    public String getFullName()  { return firstName + " " + lastName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }
    public void setAge(int age)                { this.age = age; }

    @Override
    public String toString() {
        return getFullName() + " (age " + age + ")";
    }
}
