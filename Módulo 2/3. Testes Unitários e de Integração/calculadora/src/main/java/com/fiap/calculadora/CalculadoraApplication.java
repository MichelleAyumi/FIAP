package com.fiap.calculadora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CalculadoraApplication {

	
	public int somar(int a, int b){
		return a+b;
	}

	public int subtrair(int a, int b){
		return a-b;
	}

	public int multiplicar(int a, int b){
		return a*b;
	}

	public int dividir(int a, int b){
		return a/b;
	}
}
