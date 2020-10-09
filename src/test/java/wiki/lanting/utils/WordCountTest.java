package wiki.lanting.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordCountTest {

    @Test
    void wordCount() {
        assertEquals(1, WordCount.wordCount("a"));
    }
}