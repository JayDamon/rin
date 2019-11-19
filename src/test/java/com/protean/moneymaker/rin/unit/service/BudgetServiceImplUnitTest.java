package com.protean.moneymaker.rin.unit.service;

import com.protean.moneymaker.rin.dto.*;
import com.protean.moneymaker.rin.model.*;
import com.protean.moneymaker.rin.repository.*;
import com.protean.moneymaker.rin.service.*;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplUnitTest {

    @Mock private BudgetCategoryRepository budgetCategoryRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private FrequencyTypeRepository frequencyTypeRepository;
    @Mock private TransactionRepository transactionRepository;

    private BudgetService budgetService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");

    @BeforeEach
    void setUp() {
        FrequencyService frequencyService = new FrequencyServiceImpl(frequencyTypeRepository);
        TransactionService transactionService = new TransactionServiceImpl(transactionRepository, null, null);
        budgetService = new BudgetServiceImpl(
                budgetRepository, mock(BudgetSubCategoryRepository.class),
                budgetCategoryRepository, frequencyService, transactionService);
    }

//    getAllBudgetCategoryDtos
    @Test
    void getAllBudgetCategories_GivenValidReturn_ThenCategoriesComplete() {

        // Arrange
        when(budgetCategoryRepository.findAll()).thenReturn(createBudgetCategories());

        // Act
        Set<BudgetCategory> budgetCategories = budgetService.getAllBudgetCategories();

        // Assert
        assertThat(budgetCategories, hasSize(1));
        int budgetCategoriesChecked = 0;
        for (BudgetCategory budgetCategory : budgetCategories) {
            assertThat(budgetCategory.getId(), is(equalTo(3)));
            assertThat(budgetCategory.getType().getId(), is(equalTo(1)));
            assertThat(budgetCategory.getType().getName(), is(equalTo("TestBudgetCategoryType")));
            assertThat(budgetCategory.getName().getId(), is(equalTo(2)));
            assertThat(budgetCategory.getName().getName(), is(equalTo("TestBudgetCategoryName")));
            assertThat(budgetCategory.getBudgetItems(), hasSize(1));
            int budgetItemsChecked = 0;
            for (BudgetItem item : budgetCategory.getBudgetItems()) {
                assertThat(item.getId(), is(equalTo(4L)));
                assertThat(item.getName(), is("TestBudgetItemName"));
                assertThat(item.getBudgetCategory(), is(equalTo(budgetCategory)));
                budgetItemsChecked++;
            }
            assertThat(budgetItemsChecked, is(equalTo(1)));
            budgetCategoriesChecked++;
        }
        assertThat(budgetCategoriesChecked, is(equalTo(1)));
    }

    @Test
    void getAllBudgetCategoryDtos_GivenValidReturn_ThenCategoriesComplete() {

        // Arrange
        when(budgetCategoryRepository.findAll()).thenReturn(createBudgetCategories());

        // Act
        Set<BudgetCategoryDto> budgetCategories = budgetService.getAllBudgetCategoryDtos();

        // Assert
        validateBudgetCategoriesIsCorrect(budgetCategories);

    }

    private void validateBudgetCategoriesIsCorrect(Set<BudgetCategoryDto> budgetCategories) {
        assertThat(budgetCategories, hasSize(1));
        int budgetCategoriesChecked = 0;
        for (BudgetCategoryDto budgetCategory : budgetCategories) {
            assertThat(budgetCategory.getTypeName(), is(equalTo("TestBudgetCategoryType")));
            budgetCategoriesChecked = validateIndividualBudgetCategory(budgetCategoriesChecked, budgetCategory);
        }
        assertThat(budgetCategoriesChecked, is(equalTo(1)));
    }

    private int validateIndividualBudgetCategory(int budgetCategoriesChecked, BudgetCategoryDto budgetCategory) {
        assertThat(budgetCategory.getId(), is(equalTo(3)));
        assertThat(budgetCategory.getName(), is(equalTo("TestBudgetCategoryName")));
        assertThat(budgetCategory.getBudgetItems(), hasSize(1));
        int budgetItemsChecked = 0;
        for (BudgetItemDto item : budgetCategory.getBudgetItems()) {
            assertThat(item.getId(), is(equalTo(4)));
            assertThat(item.getName(), is("TestBudgetItemName"));
            assertThat(item.getCategoryName(), is(equalTo("TestBudgetCategoryName")));
            budgetItemsChecked++;
        }
        assertThat(budgetItemsChecked, is(equalTo(1)));
        budgetCategoriesChecked++;
        return budgetCategoriesChecked;
    }

    //    getAllBudgetCategoriesByType
    @Test
    void getAllBudgetCategoriesByType_GivenDataReturned_ThenProduceValidtBdugetTypeDto() {

        // Arrange
        List<BudgetCategory> budgetCategories = new ArrayList<>();
        budgetCategories.add(getBudgetCategory("Type1", 1));
        budgetCategories.add(getBudgetCategory("Type2", 2));
        budgetCategories.add(getBudgetCategory("Type3", 3));
        when(budgetCategoryRepository.findAll()).thenReturn(budgetCategories);

        // Act
        Set<BudgetTypeDto> budgetTypes = budgetService.getAllBudgetCategoriesByType();

        // Assert
        assertThat(budgetTypes, is(not(nullValue())));
        assertThat(budgetTypes, hasSize(3));
        int budgetTypesChecked = 0;
        for (BudgetTypeDto type : budgetTypes) {

            assertThat(type.getId(), is(oneOf(1, 2, 3)));
            assertThat(type.getType(), is(oneOf("Type1", "Type2", "Type3")));

            for (BudgetCategoryDto cat : type.getBudgetCategories()) {
                assertThat(cat.getTypeName(), is(nullValue()));
                validateIndividualBudgetCategory(1, cat);
            }

            budgetTypesChecked++;
        }
        assertThat(budgetTypesChecked, is(equalTo(3)));

    }

    @Test
    void getAllBudgetCategoriesByType_GivenNoDataReturned_ThenReturnEmptySet() {
        // Arrange
        when(budgetCategoryRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Set<BudgetTypeDto> budgetTypes = budgetService.getAllBudgetCategoriesByType();

        // Assert
        assertThat(budgetTypes, is(not(nullValue())));
        assertThat(budgetTypes, hasSize(0));
    }

//    createNewBudgets
    @Test
    @SuppressWarnings("unchecked")
    void createNewBudgets_GivenBudgetsProvided_ThenSaveAndReturnWithIds() {

        // Arrange
        BudgetDto budgetDto = getBudgetDto();

        when(budgetRepository.saveAll(any())).thenAnswer(i -> {
            List<Budget> budgets = (ArrayList<Budget>)i.getArguments()[0];
            long id = 1;
            for (Budget b : budgets) {
                b.setId(id);
                id++;
            }
            return budgets;
        });

        // Act
        Set<BudgetDto> budgetDtos = budgetService.createNewBudgets(new HashSet<>(Collections.singletonList(budgetDto)));

        int i = 0;
        for (BudgetDto dto : budgetDtos) {
            assertThat(dto.getId(), is(not(nullValue())));
        }
    }

    @Test
    void createNewBudgets_GivenBudgetsIsNull_ThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> budgetService.createNewBudgets(null));
    }

//    updateBudget
    @Test
    void updateBudget_GivenBudgetExistsAndAllValuesChanged_ThenUpdateAllValues() {

        // Arrange
        Budget currentBudget = createBudget(
                "A Budget Name", ZonedDateTime.now(), ZonedDateTime.now().plusDays(5),
                35.02, true, 1L);

        when(budgetRepository.findById(eq(1L))).thenReturn(Optional.of(currentBudget));
        when(budgetRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        BudgetCategory budgetCategory = createBudgetCategory("TestType", 2, "TestCatName", 3, 5);
        FrequencyType frequencyType = createFrequencyType("TestFrequency", 10);

        when(budgetCategoryRepository.findById(eq(5))).thenReturn(Optional.of(budgetCategory));
        when(frequencyTypeRepository.findById(eq(10))).thenReturn(Optional.of(frequencyType));

        BudgetDto budgetDto = getBudgetDto();

        // Act
        Budget budget = budgetService.updateBudget(budgetDto);

        // Assert
        assertThat(budget.getId(), is(equalTo(1L)));
        assertThat(budget.getName(), is(equalTo("TestName")));
        assertThat(budget.getStartDate().getDayOfMonth(), is(equalTo(LocalDate.now().getDayOfMonth())));
        assertThat(budget.getEndDate().getDayOfMonth(), is(equalTo(ZonedDateTime.now().plusDays(6).getDayOfMonth())));
        assertThat(budget.getFrequencyType().getId(), is(equalTo(10)));
        assertThat(budget.getFrequencyType().getName(), is(equalTo("TestFrequency")));
        assertThat(budget.getAmount(), is(equalTo(BigDecimal.valueOf(54.21))));
        assertThat(budget.getInUse(), is(false));

        BudgetCategory cat = budget.getBudgetCategory();
        assertThat(cat.getId(), is(equalTo(5)));
        assertThat(cat.getType().getName(), is(equalTo("TestType")));
        assertThat(cat.getName().getName(), is(equalTo("TestCatName")));

    }

    @Test
    void updateBudget_GivenDtoWithOnlyPartialValuesProvided_ThenUpdateOnlyValuesProvided() {

        // Arrange
        Budget currentBudget = createBudget(
                "A Budget Name", ZonedDateTime.now(), ZonedDateTime.now().plusDays(5),
                35.02, true, 1L);

        when(budgetRepository.findById(eq(1L))).thenReturn(Optional.of(currentBudget));
        when(budgetRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        BudgetCategory budgetCategory = createBudgetCategory("TestType", 2, "TestCatName", 3, 5);

        when(budgetCategoryRepository.findById(eq(5))).thenReturn(Optional.of(budgetCategory));

        BudgetDto budgetDto = getBudgetDto();
        budgetDto.setFrequencyTypeId(null);
        budgetDto.setFrequencyTypeName(null);
        budgetDto.setAmount(null);

        // Act
        Budget budget = budgetService.updateBudget(budgetDto);

        // Assert
        verify(frequencyTypeRepository, times(0)).findById(any());

        assertThat(budget.getId(), is(equalTo(1L)));
        assertThat(budget.getName(), is(equalTo("TestName")));
        assertThat(budget.getStartDate().getDayOfMonth(), is(equalTo(LocalDate.now().getDayOfMonth())));
        assertThat(budget.getEndDate().getDayOfMonth(), is(equalTo(ZonedDateTime.now().plusDays(6).getDayOfMonth())));
        assertThat(budget.getFrequencyType().getId(), is(equalTo(3)));
        assertThat(budget.getFrequencyType().getName(), is(equalTo("TestFrequencyTypeName")));
        assertThat(budget.getAmount(), is(equalTo(BigDecimal.valueOf(35.02))));
        assertThat(budget.getInUse(), is(false));

        BudgetCategory cat = budget.getBudgetCategory();
        assertThat(cat.getId(), is(equalTo(5)));
        assertThat(cat.getType().getName(), is(equalTo("TestType")));
        assertThat(cat.getName().getName(), is(equalTo("TestCatName")));

    }

    @Test
    void updateBudget_GivenDtoIsNull_ThenThrowIllegalArgumentException() {

        assertThrows(IllegalArgumentException.class, () -> budgetService.updateBudget(null));

    }

    @Test
    void updateBudget_GivenDtoIdIsNull_ThenThrowIllegalArgumentException() {

        BudgetDto budgetDto = getBudgetDto();
        budgetDto.setId(null);

        assertThrows(IllegalArgumentException.class, () -> budgetService.updateBudget(budgetDto));

    }

    @Test
    void updateBudget_GivenBudgetWithIdDoesNotExist_ThenThrowIllegalArgumentException() {

        when(budgetRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> budgetService.updateBudget(getBudgetDto()));

    }

    @Test
    void getBudgetSummary_GivenTransactionsForBudgetsExist_ThenReturnBudgetSummary() {

        // Arrange
        Budget budget = createBudget("TestBudgetOne", ZonedDateTime.now(), null, 25, true, 1L);
        Budget budgetTwo = createBudget("TestBudgetTwo", ZonedDateTime.now(), null, 50, true, 1L);

        Transaction transactionOne = createTransaction(1L, budget, BigDecimal.valueOf(25));
        Transaction transactionTwo = createTransaction(2L, budgetTwo, BigDecimal.valueOf(10));
        Transaction transactionThree = createTransaction(3L, budgetTwo, BigDecimal.valueOf(20));

        Set<Transaction> transactions = new HashSet<>(Arrays.asList(transactionOne, transactionTwo, transactionThree));

        when(transactionRepository.findAllByDateAfterAndDateBefore(any(), any())).thenReturn(transactions);

        ZonedDateTime localDateTime = ZonedDateTime.now();
        int month = localDateTime.getDayOfMonth();
        int year = localDateTime.getYear();
        String text = localDateTime.format(formatter);

        // Act
        Set<BudgetSummary> budgetSummaries = budgetService.getBudgetSummary(new int[]{month}, new int[]{year});

        // Assert
        assertThat(budgetSummaries, hasSize(1));
        List<BudgetSummary> summaryList = new ArrayList<>(budgetSummaries);
        BudgetSummary summary = summaryList.get(0);
        assertThat(summary.getCategory(), is(equalTo("TestBudgetCategoryName")));
        assertThat(summary.getMonth(), is(equalTo(month)));
        assertThat(summary.getMonthText(), is(equalTo(text)));
        assertThat(summary.getYear(), is(equalTo(year)));
        assertThat(summary.getPlanned(), is(equalTo(BigDecimal.valueOf(75))));
        assertThat(summary.getActual(), is(equalTo(BigDecimal.valueOf(55))));
        assertThat(summary.isExpected(), is(equalTo(false))); // TODO need to make sure everything works if it is income as well

    }

    private Transaction createTransaction(long transactionId, Budget budget, BigDecimal amount) {
        Account account = new Account();
        account.setId(1L);

        Transaction transaction = new Transaction(account, budget, null, null, null, ZonedDateTime.now(), "TestDescription", amount);
        transaction.setId(transactionId);

        return transaction;

    }

    private FrequencyType createFrequencyType(String testFrequency, int id) {
        FrequencyType frequencyType = new FrequencyType(testFrequency);
        frequencyType.setId(id);
        return frequencyType;
    }

    private BudgetCategory createBudgetCategory(String testType, int typeId, String testCatName, int nameId, int categoryId) {
        BudgetCategoryType budgetCategoryType = new BudgetCategoryType(testType);
        budgetCategoryType.setId(typeId);

        BudgetCategoryName budgetCategoryName = new BudgetCategoryName(testCatName);
        budgetCategoryName.setId(nameId);

        BudgetCategory budgetCategory = new BudgetCategory(budgetCategoryType, budgetCategoryName);
        budgetCategory.setId(categoryId);
        return budgetCategory;
    }


    private BudgetDto getBudgetDto() {
        BudgetCategoryDto budgetCategoryDto = new BudgetCategoryDto(5, "TestType", "TestCatName");
        return new BudgetDto(1L, "TestName", budgetCategoryDto, ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(6), 10, "TestFrequency", BigDecimal.valueOf(54.21), false);
    }

    private List<BudgetCategory> createBudgetCategories() {

        BudgetCategory budgetCategory = getBudgetCategory("TestBudgetCategoryType", 1);

        return Collections.singletonList(budgetCategory);

    }

    private BudgetCategory getBudgetCategory(String type, int typeId) {
        BudgetCategory budgetCategory = createBudgetCategory(type, typeId, "TestBudgetCategoryName", 2, 3);

        Set<BudgetItem> budgetItems = new HashSet<>();
        BudgetItem budgetItem = new BudgetItem(budgetCategory, "TestBudgetItemName");
        budgetItem.setId(4L);
        budgetItems.add(budgetItem);

        budgetCategory.setBudgetItems(budgetItems);
        return budgetCategory;
    }

    private Budget createBudget(String name, ZonedDateTime startDate, ZonedDateTime endDate, double amount, boolean inUse, long id) {
        BudgetCategory budgetCategory = createBudgetCategory("TestBudgetCategoryType", 1, "TestBudgetCategoryName", 2, 1);

        BudgetItem itemOne = new BudgetItem(budgetCategory, "Item Name");
        itemOne.setId(6L);

        BudgetItem itemTwo = new BudgetItem(budgetCategory, "Item Name Two");
        itemTwo.setId(7L);

        itemOne.setBudgetCategory(budgetCategory);

        budgetCategory.getBudgetItems().add(itemOne);
        budgetCategory.getBudgetItems().add(itemTwo);

        FrequencyType frequencyType = createFrequencyType("TestFrequencyTypeName", 3);

        Budget budget = new Budget(
                budgetCategory, name, startDate,
                endDate, frequencyType, BigDecimal.valueOf(amount), inUse);
        budget.setId(id);

        return budget;
    }

}
