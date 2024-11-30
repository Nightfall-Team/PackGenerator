package com.arcanewarrior;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Arguments - pack name, namespace
        if (args.length < 2) {
            System.out.println("Requires at least 2 arguments for pack name and namespace.");
            System.exit(-1);
        }
        String packName = args[0];
        String namespace = args[1];
        ResourcePackGenerator generator = new ResourcePackGenerator(packName, namespace);
        try {
            generator.buildPack();
        } catch (IOException e) {
            System.err.println("Failed to generate pack: " + e.getMessage());
        }
    }
}