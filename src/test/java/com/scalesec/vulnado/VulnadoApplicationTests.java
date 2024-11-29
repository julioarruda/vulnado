package com.scalesec.vulnado;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VulnadoApplicationTests {

	@Test
	public void contextLoads() {
		assertNotNull("Application context should load", SpringApplication.run(VulnadoApplication.class));
	}

}

