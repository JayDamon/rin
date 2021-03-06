package com.factotum.rin.service;

import com.factotum.rin.dto.BudgetCategoryDto;
import com.factotum.rin.dto.BudgetDto;
import com.factotum.rin.dto.BudgetSummary;
import com.factotum.rin.dto.BudgetTypeDto;
import com.factotum.rin.dto.TransactionBudgetSummary;
import com.factotum.rin.dto.TransactionTotal;
import com.factotum.rin.http.TransactionService;
import com.factotum.rin.model.Budget;
import com.factotum.rin.model.BudgetCategory;
import com.factotum.rin.model.BudgetCategoryType;
import com.factotum.rin.model.FrequencyType;
import com.factotum.rin.repository.BudgetCategoryRepository;
import com.factotum.rin.repository.BudgetRepository;
import com.factotum.rin.util.BudgetUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final ModelMapper modelMapper = new ModelMapper();

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final FrequencyService frequencyService;
    private final TransactionService transactionService;

    public BudgetServiceImpl(
            BudgetRepository budgetRepository,
            BudgetCategoryRepository budgetCategoryRepository,
            FrequencyService frequencyService,
            TransactionService transactionService) {

        this.budgetRepository = budgetRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.frequencyService = frequencyService;
        this.transactionService = transactionService;
    }

    @Override
    public Set<Budget> getAllActiveBudgets() {
        return new LinkedHashSet<>(budgetRepository.findBudgetsByInUseTrue());
    }

    @Override
    public Set<Budget> getAllBudgets() {

        return new HashSet<>(budgetRepository.findAll());
    }

    @Override
    public Set<Budget> getAllInactiveBudgets() {
        return new LinkedHashSet<>(budgetRepository.findBudgetsByInUseFalse());
    }

    @Override
    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public Set<Budget> saveBudgets(Set<Budget> budgets) {

        return new HashSet<>(budgetRepository.saveAll(budgets));
    }

    @Override
    public Set<Budget> deactivateBudgets(Set<Budget> budgets) {


        budgets.forEach(budget -> budget.setInUse(false));

        return new HashSet<>(budgetRepository.saveAll(budgets));
    }

    @Override
    public void deleteUserDefinedBudgets(Set<Budget> budgets) {
        budgetRepository.deleteAll(budgets);
    }

    @Override
    public void deleteUserDefinedBudget(Budget budget) {
        budgetRepository.delete(budget);
    }

    @Override
    @Transactional
    public List<TransactionBudgetSummary> getBudgetSummary(int year, int month) {

        ZonedDateTime startDate = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime endDate = startDate.withDayOfMonth(startDate.plusMonths(1).minusDays(1).getDayOfMonth());

        List<BudgetSummary> summaries = getBudgetSummaries(startDate, endDate);

        return summaries.stream().map(s -> {

            Set<Long> budgetIds = budgetRepository
                    .queryAllBudgetIdsForSummary(
                            s.getTransactionTypeId(),
                            s.getCategoryId(),
                            startDate,
                            endDate);

            TransactionTotal total = transactionService.getTransactionTotal(year, month, s.getTransactionTypeId(), budgetIds);

            return TransactionBudgetSummary.builder()
                    .transactionType(total.getTransactionType())
                    .category(s.getCategory())
                    .month(month)
                    .monthText(LocalDateTime.now().withMonth(month).format(DateTimeFormatter.ofPattern("MMMM")))
                    .year(year)
                    .planned(s.getPlanned())
                    .actual(total.getTotal())
                    .expected(totalIsExpected(total.getTransactionType(), total.getTotal(), s.getPlanned()))
                    .build();

        }).collect(Collectors.toList());

    }

    private boolean totalIsExpected(String transactionType, BigDecimal actual, BigDecimal expected) {

        if (actual == null) {
            return !(expected.doubleValue() > 0);
        }

        if ("Income".equalsIgnoreCase(transactionType)) {
            return expected.compareTo(actual) > 0;
        } else if ("Expense".equalsIgnoreCase(transactionType)) {
            return expected.compareTo(actual) < 0;
        }

        return true;
    }

    @Override
    public List<BudgetSummary> getBudgetSummaries(ZonedDateTime startDate, ZonedDateTime endDate) {

        List<BudgetSummary> summaries = budgetRepository.getBudgetSummaries(startDate, endDate);

        return summaries;
    }

    @Override
    public List<BudgetCategory> getAllBudgetCategories() {
        return budgetCategoryRepository.findAll();
    }

    @Override
    public Set<BudgetCategoryDto> getAllBudgetCategoryDtos() {

        Set<BudgetCategoryDto> budgetCategoryDtos = new HashSet<>();
        for (BudgetCategory budgetCategory : getAllBudgetCategories()) {
            budgetCategoryDtos.add(modelMapper.map(budgetCategory, BudgetCategoryDto.class));
        }
        return budgetCategoryDtos;
    }

    @Override
    public Set<BudgetTypeDto> getAllBudgetCategoriesByType() {

        Map<Integer, BudgetTypeDto> budgetTypeMap = new HashMap<>();
        for (BudgetCategory budgetCategory : getAllBudgetCategories()) {

            BudgetTypeDto budgetTypeDto;

            BudgetCategoryType type = budgetCategory.getType();
            if (budgetTypeMap.containsKey(type.getId())) {

                budgetTypeDto = budgetTypeMap.get(type.getId());

            } else {

                budgetTypeDto = new BudgetTypeDto();
                budgetTypeDto.setId(type.getId());
                budgetTypeDto.setType(type.getName());

            }

            BudgetCategoryDto cat = modelMapper.map(budgetCategory, BudgetCategoryDto.class);
            cat.setTypeName(null);
            budgetTypeDto.getBudgetCategories().add(cat);

            budgetTypeMap.put(type.getId(), budgetTypeDto);
        }

        return new HashSet<>(budgetTypeMap.values());
    }

    @Override
    public Set<BudgetDto> createNewBudgets(Set<BudgetDto> newBudgets) {

        if (newBudgets == null) {
            throw new IllegalArgumentException("Budget Dtos must not be null");
        }

        List<Budget> budgetList = new ArrayList<>(BudgetUtil.convertBudgetDtosToBudgetIncludeOnlyIdForChildEntity(newBudgets));

        budgetList = budgetRepository.saveAll(budgetList);

        return BudgetUtil.convertBudgetsToDto(budgetList);
    }

    @Override
    public Budget updateBudget(BudgetDto budgetDto) {

        if (budgetDto == null) {
            throw new IllegalArgumentException("Budget must not be null.");
        }
        if (budgetDto.getId() == null) {
            throw new IllegalArgumentException("Budget must have a valid id.");
        }

        Budget budget = budgetRepository.findById(budgetDto.getId()).orElseThrow(
                () -> new NoResultException("No budget with id <" + budgetDto.getId() + "> was found."));

        if (budgetDto.getFrequencyTypeId() != null) {
            FrequencyType frequencyType = frequencyService.getFrequencyTypeById(budgetDto.getFrequencyTypeId());
            budget.setFrequencyType(frequencyType);
        }
        if (budgetDto.getBudgetCategory() != null && budgetDto.getBudgetCategory().getId() != null) {
            BudgetCategory budgetCategory = budgetCategoryRepository.findById(
                    budgetDto.getBudgetCategory().getId()).orElseThrow(
                    () -> new NoResultException("Budget category with id <" + budgetDto.getBudgetCategory().getId() + "> was not found."));
            budget.setBudgetCategory(budgetCategory);
        }
        if (budgetDto.getName() != null) {
            budget.setName(budgetDto.getName());
        }
        if (budgetDto.getStartDate() != null) {
            budget.setStartDate(budgetDto.getStartDate());
        }
        if (budgetDto.getEndDate() != null) {
            budget.setEndDate(budgetDto.getEndDate());
        }
        if (budgetDto.getAmount() != null) {
            budget.setAmount(budgetDto.getAmount());
        }
        if (budgetDto.getInUse() != null) {
            budget.setInUse(budgetDto.getInUse());
        }

        return budgetRepository.save(budget);
    }

}
