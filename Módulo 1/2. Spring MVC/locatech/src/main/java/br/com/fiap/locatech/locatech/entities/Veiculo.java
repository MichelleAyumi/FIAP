package br.com.fiap.locatech.locatech.entities;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor // Cria construtor sem argumento
@AllArgsConstructor //Cria um construtor com todos os argumentos
@EqualsAndHashCode
@ToString

public class Veiculo {
    private long id;
    private String marca;
    private String modelo;
    private String placa;
    private int ano;
    private String cor;
    private BigDecimal valorDiaria;
}
