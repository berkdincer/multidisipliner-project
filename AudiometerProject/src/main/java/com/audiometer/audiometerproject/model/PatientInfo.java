package com.audiometer.audiometerproject.model;

import java.time.LocalDate;

/**
 * Stores patient demographic information for an audiometric test session.
 * Uses the Builder pattern for flexible, readable object construction.
 * Instances are immutable once built.
 */
public final class PatientInfo {
    private final String firstName;
    private final String lastName;
    private final int age;
    private final LocalDate testDate;
    private final String notes;

    private PatientInfo(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.age = builder.age;
        this.testDate = builder.testDate;
        this.notes = builder.notes;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the full name (first + last).
     *
     * @return concatenated full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getAge() {
        return age;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return String.format("Patient: %s, Age: %d, Date: %s", getFullName(), age, testDate);
    }

    /**
     * Builder class for constructing PatientInfo instances.
     * Ensures required fields are provided and defaults are sensible.
     */
    public static class Builder {
        private String firstName = "";
        private String lastName = "";
        private int age = 0;
        private LocalDate testDate = LocalDate.now();
        private String notes = "";

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder testDate(LocalDate testDate) {
            this.testDate = testDate;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        /**
         * Builds and returns an immutable PatientInfo instance.
         *
         * @return new PatientInfo
         * @throws IllegalArgumentException if required fields are missing
         */
        public PatientInfo build() {
            if (firstName == null || firstName.trim().isEmpty()) {
                throw new IllegalArgumentException("First name is required.");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                throw new IllegalArgumentException("Last name is required.");
            }
            if (age < 0 || age > 150) {
                throw new IllegalArgumentException("Age must be between 0 and 150.");
            }
            return new PatientInfo(this);
        }
    }
}
