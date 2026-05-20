package br.com.fiap.locatech.locatech.controllers;
import br.com.fiap.locatech.locatech.entities.Veiculo;
import br.com.fiap.locatech.locatech.services.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

//Recurso da API
@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veiculo", description = "Controller para CRUD de veículos.")

public class VeiculoController {

    private static final Logger logger = LoggerFactory.getLogger(VeiculoController.class);
    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    //Implementando os métodos
    //http://localhost:8080/veiculos?=1&size=10
    //paginacao 1 concatenação size=10

    @Operation(
            description = "Busca todos os veículos paginados.",
            summary = "Busca veículos.",
            responses = {
                    @ApiResponse(description = "OK", responseCode = "200")
            }
    )
    @GetMapping
    public ResponseEntity<List<Veiculo>> findAllVeiculos(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        logger.info("/veiculos");
        var veiculos = this.veiculoService.findAllVeiculos(page, size);
        return ResponseEntity.ok(veiculos);
    }
    //http://localhost:8080/veiculos/1
    //path variable - 1
    //Capturar o ID que vai na nossa URl

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Veiculo>> findVeiculos(
            @PathVariable("id") Long id
    ){
        logger.info("/veiculos/" + id);
        var veiculo = this.veiculoService.findVeiculoById(id);
        return ResponseEntity.ok(veiculo);
    }

    @PostMapping
    public ResponseEntity<Void> saveVeiculo(
            @RequestBody Veiculo veiculo
    ){
        logger.info("POST -> /veiculos");
        this.veiculoService.saveVeiculo(veiculo);
        return ResponseEntity.status(201).build();
        //Status 201 - staus de retorno
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateVeiculo(
            @PathVariable("id") Long id,
            @RequestBody Veiculo veiculo
    ){
        logger.info("PUT -> /veiculos/" + id);
        this.veiculoService.updateVeiculo(veiculo, id);
        var status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status.value()).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeiculo(
            @PathVariable("id") Long id
    ){
        logger.info("DELETE -> /veiculos");
        this.veiculoService.delete(id);
        return ResponseEntity.ok().build();
    }

    //Usando o ResponseEntity retorna o status correto p usuário
}
