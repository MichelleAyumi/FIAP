package br.com.fiap.locatech.locatech.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

//Record é uma classe que facilita a criação de classes imutáveis, gera de maneira automática construtores e metodos, hashcode
//reduze a quatidade de codigo desnecessarios

public record AluguelRequestDTO(

        @Schema(description = "ID da pessoa que está alugando o veíuclo.")
        @NotNull(message = "O ID da pessoa não pode ser nulo.")
        Long pessoaId,

        @Schema(description = "ID do veículo que está sendo alugafo.")
        @NotNull(message = "O ID do veículo não pode ser nulo.")
        Long veiculoId,

        LocalDate dataInicio,
        LocalDate dataFim) {

}
