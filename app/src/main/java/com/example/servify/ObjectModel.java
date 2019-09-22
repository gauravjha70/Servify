package com.example.servify;

public class ObjectModel {

    private String ipAddress;
    private String macAddress;
    private String host;
    private String vendor;

    private String type;
    private String name;

    public ObjectModel() {
    }

    public ObjectModel(String type) {
        this.type = type;
    }

    public ObjectModel(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public ObjectModel(String ipAddress, String macAddress, String host, String vendor, String name) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.host = host;
        this.vendor = vendor;
        this.name = name;
    }

    public ObjectModel(String ipAddress, String macAddress, String host, String vendor, String type, String name) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.host = host;
        this.vendor = vendor;
        this.type = type;
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
