package com.factotum.rin.controller;

import com.factotum.rin.dto.BudgetDto;
import com.factotum.rin.dto.TransactionBudgetSummary;
import com.factotum.rin.model.Budget;
import com.factotum.rin.repository.BudgetRepository;
import com.factotum.rin.service.BudgetService;
import com.factotum.rin.util.BudgetUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetRepository budgetRepository;

    public BudgetController(BudgetService budgetService, BudgetRepository budgetRepository) {
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
    }

    @GetMapping("")
    public Set<BudgetDto> getActiveBudgets() {

        return BudgetUtil.convertBudgetsToDto(
                budgetService.getAllActiveBudgets()
        );

    }

    @GetMapping("/{id}")
    public BudgetDto getBudgetById(@PathVariable(name = "id") long id) {

        Budget budget = budgetRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Unable to find budget with id %s", id)));

        return BudgetUtil.convertBudgetToDto(budget);
    }

    @PostMapping("")
    public Set<BudgetDto> createNewBudgets(@RequestBody Set<BudgetDto> newBudgets) {

        return budgetService.createNewBudgets(newBudgets);

    }

    @PatchMapping("/{id}")
    public BudgetDto updateBudget(
            @PathVariable(name = "id") @Min(1) long id, @RequestBody BudgetDto budgetDto) {

        if (budgetDto.getId() == null) {
            throw new IllegalArgumentException("Budget id must be provided in the body.");
        }
        if (id != budgetDto.getId()) {
            throw new IllegalArgumentException("Path id and body id were not equal.");
        }

        return BudgetUtil.convertBudgetToDto(
                budgetService.updateBudget(budgetDto)
        );

    }

    @GetMapping("/summary")
    public List<TransactionBudgetSummary> getBudgetSummary(
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month) {

        return budgetService.getBudgetSummary(year, month);
    }

}
