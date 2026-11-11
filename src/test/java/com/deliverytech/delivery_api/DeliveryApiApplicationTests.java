package com.deliverytech.delivery_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = "jwt.secret=chave-de-teste-ficticia-com-pelo-menos-32-bytes!!")
class DeliveryApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
