package com.fiap.calculadora;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class CalculadoraApplicationTests {
	public CalculadoraApplication calculadoraApplication;
	private int a;
	private int b;

	@BeforeEach
	void setUp() {
		calculadoraApplication = new CalculadoraApplication();
		a = 5;
		b = 2;
	}

	@Test
	void somar(){
		//Arrange - Preparar
		//Action - Agir/ Atuar
		//Assert - Validar
		int resultado = calculadoraApplication.somar(a,b);
		Assertions.assertEquals(resultado,7);
	}

	@Test
	void subtrair(){
		int resultado = calculadoraApplication.somar(a,b);
		Assertions.assertEquals(resultado,3);
	}

	@Test
	void multiplicar(){
		int resultado = calculadoraApplication.somar(a,b);
		Assertions.assertEquals(resultado,10);
	}

	@Test
	void dividir(){
		int a = 6;
		int resultado = calculadoraApplication.somar(a,b);
		Assertions.assertEquals(resultado,2.5);
	}

	@Test
	void dividirPorZero(){
		int a = 0;
		Exception exception = Assertions.assertThrows(ArithmeticException.class, () -> {
			calculadoraApplication.dividir(a,b);
		});
		String msg = "Não aceito valor igual a zero.";
		String msg2 = exception.getMessage();
		assertEquals(msg,msg2);

	}


}
