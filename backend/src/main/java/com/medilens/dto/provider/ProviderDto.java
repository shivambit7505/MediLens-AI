package com.medilens.dto.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProviderDto {
    private UUID id;
    private String name;
    private String title; // "MD, FACP", "MD, FACC", etc.
    private String specialty;
    private String clinicName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private Double rating;
    private Integer reviewCount;
    private Double distanceMiles;
    private boolean telehealthAvailable;
    private boolean acceptingNewPatients;
    private List<String> affiliatedHospitals = new ArrayList<>();
    private List<String> clinicalInterests = new ArrayList<>();

    public ProviderDto() {}

    public ProviderDto(UUID id, String name, String title, String specialty, String clinicName,
                       String address, String city, String state, String zipCode, String phone,
                       Double rating, Integer reviewCount, Double distanceMiles,
                       boolean telehealthAvailable, boolean acceptingNewPatients,
                       List<String> affiliatedHospitals, List<String> clinicalInterests) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.specialty = specialty;
        this.clinicName = clinicName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.phone = phone;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.distanceMiles = distanceMiles;
        this.telehealthAvailable = telehealthAvailable;
        this.acceptingNewPatients = acceptingNewPatients;
        this.affiliatedHospitals = affiliatedHospitals;
        this.clinicalInterests = clinicalInterests;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Double getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(Double distanceMiles) {
        this.distanceMiles = distanceMiles;
    }

    public boolean isTelehealthAvailable() {
        return telehealthAvailable;
    }

    public void setTelehealthAvailable(boolean telehealthAvailable) {
        this.telehealthAvailable = telehealthAvailable;
    }

    public boolean isAcceptingNewPatients() {
        return acceptingNewPatients;
    }

    public void setAcceptingNewPatients(boolean acceptingNewPatients) {
        this.acceptingNewPatients = acceptingNewPatients;
    }

    public List<String> getAffiliatedHospitals() {
        return affiliatedHospitals;
    }

    public void setAffiliatedHospitals(List<String> affiliatedHospitals) {
        this.affiliatedHospitals = affiliatedHospitals;
    }

    public List<String> getClinicalInterests() {
        return clinicalInterests;
    }

    public void setClinicalInterests(List<String> clinicalInterests) {
        this.clinicalInterests = clinicalInterests;
    }
}
