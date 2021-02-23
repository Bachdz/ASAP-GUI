package com.example.demo.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class OutputCollector extends OutputStream {
    private final List<String> lines = new ArrayList<>();
    private StringBuilder buffer = new StringBuilder();
    @Override
    public void write(int b) throws IOException {

        if (b == '\n') {

            lines.add(buffer.toString());
            buffer = new StringBuilder();
        } else if (b != '\r'){
            buffer.append((char) b);
        }
    }

    public List<String> getLines() {
        return lines;
    }

}

