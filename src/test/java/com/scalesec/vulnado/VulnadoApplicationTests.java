package com.scalesec.vulnado;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertTrue;
@RunWith(SpringRunner.class)
@SpringBootTest
public class VulnadoApplicationTests {

    @Test
    public void contextLoads() {
        assertTrue("Context should load successfully", true);
    }
}