package com.fiap.calculadora;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class CalculadoraApplicationTests {

	public CalculadoraApplication calculadoraApplication = new CalculadoraApplication();
	private int valorA = 2;
	private int valorB = 3;

	//Deve permitir somar
	void somar(){
		//fail("Código não implementado");
		int resultado = calculadoraApplication.somar(valorA, valorB);
		Assertions.assertEquals(resultado, 5);
	}

	//Deve permitir subtrair
	void subtrair(){
		//fail("Código não implementado");
		int resultado = calculadoraApplication.subtrair(valorA, valorB);
		Assertions.assertEquals(resultado, -1);

	}

	//Deve permitir multiplicar
	void multiplicar(){
		//fail("Código não implementado");
		int resultado = calculadoraApplication.multiplicar(valorA, valorB);
		Assertions.assertEquals(resultado, 6);
	}


	//Deve permitir dividir
	void dividir(){
		//fail("Código não implementado");
		int resultado = calculadoraApplication.dividir(valorA, valorB);
		//Assertions.assertEquals(resultado, -1);

	}


	//Deve gerar erro ao dividir por 0
	void dividirPorZero(){
		//fail("Código não implementado");
	}

}
