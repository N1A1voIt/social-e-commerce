package com.itu.socialcom.demo.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse {
    int status;
    Object data;
    List<Exception> errors;
}
