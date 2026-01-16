package com.assignment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatementRequest {

    @NotBlank
    private String customerName;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statementDate;

    @NotEmpty
    @Valid
    private List<Transaction> transactions;

}
