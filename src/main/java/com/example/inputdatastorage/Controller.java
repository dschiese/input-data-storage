package com.example.inputdatastorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

@RestController
public class Controller {

    Controller() {}

    @Autowired
    private Service service;


    @GetMapping("/test")
    public ResponseEntity<?> test() throws Exception {
        service.fetchComponentNames();
        return new ResponseEntity<>(null, HttpStatusCode.valueOf(200));
    }

}
