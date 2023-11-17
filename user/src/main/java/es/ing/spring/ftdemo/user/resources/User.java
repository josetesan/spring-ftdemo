package es.ing.spring.ftdemo.user.resources;

public record User(
    String user, String requestDuration, String error, int portfolioSize, Integer portfolioValue) {}
