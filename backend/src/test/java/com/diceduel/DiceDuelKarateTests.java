package com.diceduel;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "server.port=3001"
)
class DiceDuelKarateTests {

    @Karate.Test
    Karate runDiceDuelApiFeature() {
        return Karate.run("diceduel_api_tests_final").relativeTo(getClass());
    }
}
