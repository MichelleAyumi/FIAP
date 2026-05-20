package br.com.fiap.locatech.locatech.entities;

import br.com.fiap.locatech.locatech.dtos.AluguelRequestDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString

public class Aluguel {

    private Long id;

    private Long pessoaId;
    private Long veiculoId;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    private BigDecimal valorTotal;

    private String pessoaNome;
    private String pessoaCpf;

    private String veiculoModelo;
    private String veiculoPlaca;

    public Aluguel(AluguelRequestDTO aluguelDTO, BigDecimal valor){
        this.pessoaId = aluguelDTO.pessoaId();
        this.veiculoId = aluguelDTO.veiculoId();
        this.dataInicio = aluguelDTO.dataInicio();
        this.dataFim = aluguelDTO.dataFim();
        this.valorTotal = valor;
    }

}