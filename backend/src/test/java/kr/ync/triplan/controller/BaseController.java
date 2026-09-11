package kr.ync.triplan.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@Transactional
public abstract class BaseController {

	@Autowired
	protected WebApplicationContext webApplicationContext;
	// Spring Web 애플리케이션 컨텍스트

	protected MockMvc mockMvc;
	// Controller 테스트를 위한 MockMVC 객체

	@Autowired
	protected ObjectMapper objectMapper;
	// JSON 직렬화 및 역직렬화

	@BeforeEach
	void setUpMockMvc() {
		// Spring MVC 처럼 test환경의 MVC
		this.mockMvc =
				MockMvcBuilders
						.webAppContextSetup(webApplicationContext)
						.build();
	}
}
