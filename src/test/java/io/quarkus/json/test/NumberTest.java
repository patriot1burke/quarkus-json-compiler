package io.quarkus.json.test;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

public class NumberTest {
    @Test
    public void testNumberToString() {

        int x = Integer.MAX_VALUE;

        final int BILLION = 1000000000;

        Double.toString(34.34D);


        int places = 1;
        int n = x / 10;
        for (; n != 0; n /= 10) places *= 10;

        StringBuilder builder = new StringBuilder();
        if (x < 0) {
            builder.append('-');
            for (int place = places; place >=1; place /= 10) {
                int i = x / place;
                builder.append((char)('0' - i));
                x -= i * place;
            }
        } else {
            for (int place = places; place >=1; place /= 10) {
                int i = x / place;
                builder.append((char)('0' + i));
                x -= i * place;
            }

        }

        System.out.println(builder.toString());
    }
}
