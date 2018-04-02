package ru.ifmo.ctddev.khovanskiy.compilers;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Victor Khovanskiy
 * @since 1.0.0
 */
@Slf4j
public class SystemService {
    public boolean execute(String command) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(e.getLocalizedMessage(), e);
            }
            return (process.exitValue() == 0);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public <T> T executeForRead(String[] command, Function<InputStream, T> consumer) {
        Process process;
        try {
            if (log.isTraceEnabled()) {
                log.trace("exec: " + Arrays.toString(command));
            }
            process = Runtime.getRuntime().exec(command);
            try {
                process.waitFor();
                T t = consumer.apply(process.getInputStream());
                return t;
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(e.getLocalizedMessage(), e);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
