package com.poryectojpa.demo.dto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatoEstadisticoDTO {
    private String label;
    private Double valor;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public DatoEstadisticoDTO(String label, Double valor) {
        this.label = label;
        this.valor = valor;
    }

    public String getLabel() { return label; }
    public Double getValor() { return valor; }

    public List<DatoEstadisticoDTO> obtenerConteoPersonasPorGenero() {

    String sql = """
        SELECT genero, COUNT(*) AS total
        FROM persona
        GROUP BY genero
    """;

    return jdbcTemplate.query(sql, (rs, rowNum) ->
            new DatoEstadisticoDTO(
                    rs.getString("genero"),
                    rs.getDouble("total")
            )
    );
}

}