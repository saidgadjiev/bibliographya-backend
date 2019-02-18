package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CodeGenerator {

    private Random random = new Random();

    public int generate() {
        return random.nextInt(9000) + 1000;
    }
}
