package com.aidom.api.global.error;

public record ErrorResponse(String type, String title, int status, String detail) {}
