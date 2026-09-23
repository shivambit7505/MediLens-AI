package com.medilens.dto;

import com.medilens.model.Role;

import java.time.LocalDate;
import java.util.UUID;

public class UserProfileResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private Role role;
    private boolean active;

    public UserProfileResponse() {}

    public UserProfileResponse(UUID id, String email, String firstName, String lastName,
                               LocalDate dateOfBirth, String gender, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.role = role;
        this.active = active;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static UserProfileResponseBuilder builder() {
        return new UserProfileResponseBuilder();
    }

    public static class UserProfileResponseBuilder {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String gender;
        private Role role;
        private boolean active;

        public UserProfileResponseBuilder id(UUID id) { this.id = id; return this; }
        public UserProfileResponseBuilder email(String email) { this.email = email; return this; }
        public UserProfileResponseBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public UserProfileResponseBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public UserProfileResponseBuilder dateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; return this; }
        public UserProfileResponseBuilder gender(String gender) { this.gender = gender; return this; }
        public UserProfileResponseBuilder role(Role role) { this.role = role; return this; }
        public UserProfileResponseBuilder active(boolean active) { this.active = active; return this; }

        public UserProfileResponse build() {
            return new UserProfileResponse(id, email, firstName, lastName, dateOfBirth, gender, role, active);
        }
    }
}
