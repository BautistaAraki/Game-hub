package com.gamehub.gamehub;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class GamehubApplicationTests {

	@Test
	void applicationCanBeInstantiated() {
		assertDoesNotThrow(GamehubApplication::new);
	}

}
