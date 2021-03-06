package com.factotum.rin.controller;

import com.factotum.rin.IntegrationTest;
import com.factotum.rin.dto.BudgetCategoryDto;
import com.factotum.rin.dto.BudgetDto;
import com.factotum.rin.dto.TransactionTotal;
import com.factotum.rin.http.TransactionService;
import com.factotum.rin.model.BudgetCategory;
import com.factotum.rin.repository.BudgetCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class BudgetControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BudgetCategoryRepository budgetCategoryRepository;

    @MockBean
    private TransactionService transactionService;

    private static final String URI = "/v1/budgets";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<BudgetDto> basicBudgetDtos = new HashSet<>();
    private BudgetDto basicBudgetDto;
    private BudgetDto completeBudgetDto;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();

        BudgetCategoryDto categoryDto = new BudgetCategoryDto(6, "flexible", "spending");
        basicBudgetDto = new BudgetDto();
        basicBudgetDto.setName("TestName");
        basicBudgetDto.setBudgetCategory(categoryDto);
        basicBudgetDto.setEndDate(ZonedDateTime.now());
        basicBudgetDto.setStartDate(ZonedDateTime.now());
        basicBudgetDtos.add(basicBudgetDto);

        BudgetCategoryDto budgetCategoryDto = new BudgetCategoryDto();
        budgetCategoryDto.setId(1);

        completeBudgetDto = new BudgetDto(20L, "newName", budgetCategoryDto, ZonedDateTime.now(), ZonedDateTime.now().plusDays(1),
                1, null, BigDecimal.valueOf(100), true);
    }

    @Test
    void getActiveBudgets_GivenBudgetsExist_ThenReturnOkWithBudgetDto() throws Exception {

        mockMvc.perform(
                get(URI))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].budgetCategory.id").exists())
                .andExpect(jsonPath("$[0].budgetCategory.type").exists())
                .andExpect(jsonPath("$[0].budgetCategory.name").exists())
                .andExpect(jsonPath("$[0].budgetCategory.budgetItems").exists())
                .andExpect(jsonPath("$[0].startDate").exists())
                .andExpect(jsonPath("$[0].frequencyType").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].inUse").exists());

    }

    @Test
    void getBudgetById_GivenBudgetExists_ThenReturnBudget() throws Exception {

        mockMvc.perform(
                get(URI + "/{id}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.budgetCategory").exists())
                .andExpect(jsonPath("$.budgetCategory.id").exists())
                .andExpect(jsonPath("$.budgetCategory.type").exists())
                .andExpect(jsonPath("$.budgetCategory.name").exists())
                .andExpect(jsonPath("$.budgetCategory.budgetItems").exists());

    }

    @Test
    void getBudgetById_GivenBudgetDoesNotExist_ThenReturnNotFound() throws Exception {
        mockMvc.perform(
                get(URI + "/{id}", 999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void createNewBudgets_GivenSimpleBudgetProvided_ThenCreateBudgetsAndReturnWithIds() throws Exception {

        MvcResult mv = mockMvc.perform(
                post(URI)
                        .content(objectMapper.writeValueAsString(basicBudgetDtos))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].budgetCategory.id").exists())
                .andReturn();

        ArrayNode node = (ArrayNode) objectMapper.readTree(mv.getResponse().getContentAsString());
        int id = node.get(0).get("budgetCategory").get("id").intValue();

        BudgetCategory b = budgetCategoryRepository.findById(id).orElse(null);
        assertThat(b, is(not(nullValue())));
        assertThat(b.getName().getName(), is(equalTo("spending")));
        assertThat(b.getType().getName(), is(equalTo("flexible")));

    }

    @Test
    void createNewBudgets_GivenNoBudgetProvided_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(
                post(URI))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBudget_GivenValidBudgetProvided_ThenReturnUpdatedBudget() throws Exception {

        mockMvc.perform(
                patch(URI + "/{id}", "20")
                        .content(objectMapper.writeValueAsString(completeBudgetDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(equalTo(20))))
                .andExpect(jsonPath("$.name", is(equalTo("newName"))))
                .andExpect(jsonPath("$.budgetCategory.id", is(equalTo(1))))
                .andExpect(jsonPath("$.budgetCategory.type", is(equalTo("fixed"))))
                .andExpect(jsonPath("$.budgetCategory.name", is(equalTo("income"))))
                .andExpect(jsonPath("$.budgetCategory.budgetItems").exists())
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists())
                .andExpect(jsonPath("$.frequencyTypeId", is(equalTo(1))))
                .andExpect(jsonPath("$.frequencyType", is(equalTo("Weekly"))))
                .andExpect(jsonPath("$.amount", is(equalTo(100))))
                .andExpect(jsonPath("$.inUse", is(true)));
    }

    @Test
    void updateBudget_GivenOnlySomeFieldsChanged_ThenReturnUpdatedBudgetWithNullFieldsUnchanced() throws Exception {

        basicBudgetDto.setId(20L);

        mockMvc.perform(
                patch(URI + "/{id}", "20")
                        .content(objectMapper.writeValueAsString(basicBudgetDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(equalTo(20))))
                .andExpect(jsonPath("$.name", is(equalTo("TestName"))))
                .andExpect(jsonPath("$.budgetCategory.id", is(equalTo(6))))
                .andExpect(jsonPath("$.budgetCategory.type", is(equalTo("flexible"))))
                .andExpect(jsonPath("$.budgetCategory.name", is(equalTo("spending"))))
                .andExpect(jsonPath("$.budgetCategory.budgetItems").exists())
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists())
                .andExpect(jsonPath("$.frequencyTypeId", is(equalTo(2))))
                .andExpect(jsonPath("$.frequencyType", is(equalTo("Monthly"))))
                .andExpect(jsonPath("$.amount", is(equalTo(3000.0))))
                .andExpect(jsonPath("$.inUse", is(true)));
    }

    @Test
    void updateBudget_GivenBudgetJsonIdAndPathIdDontMatch_TheReturnBadRequest() throws Exception {

        mockMvc.perform(
                patch(URI + "/{id}", "1")
                        .content(objectMapper.writeValueAsString(completeBudgetDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBudget_GivenBudgetIdNotInBody_TheReturnBadRequest() throws Exception {

        completeBudgetDto.setId(null);

        mockMvc.perform(
                patch(URI + "/{id}", "1")
                        .content(objectMapper.writeValueAsString(completeBudgetDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBudget_GivenPathIdLessThanOne_TheReturnBadRequest() throws Exception {

        mockMvc.perform(
                patch(URI + "/{id}", "0")
                        .content(objectMapper.writeValueAsString(completeBudgetDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBudgetSummary_GivenBudgetsExist_ThenReturnBudgetSummary() throws Exception {

        int expectedSize = 5;

        when(transactionService.getTransactionTotal(anyInt(), anyInt(), anyInt(), any())).thenAnswer(i -> new TransactionTotal("TransactionType", BigDecimal.ONE));

        mockMvc.perform(
                get(URI + "/summary")
                        .param("year", "2017")
                        .param("month", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]", hasSize(expectedSize)))
                .andExpect(jsonPath("$.[*].category", hasSize(expectedSize)))
                .andExpect(jsonPath("$.[*].month", hasSize(expectedSize)))
                .andExpect(jsonPath("$.[*].monthText", hasSize(expectedSize)))
                .andExpect(jsonPath("$.[*].year", hasSize(expectedSize)))
                .andExpect(jsonPath("$.[*].planned", hasSize(expectedSize)))
                .andExpect(jsonPath("$.[*].actual", hasSize(expectedSize)))
                .andExpect(jsonPath("$.[*].expected", hasSize(expectedSize)));

    }

    @Test
    void getBudgetSummary_GivenNoBudgetsExist_ThenReturnBudgetSummary() throws Exception {

        mockMvc.perform(
                get(URI + "/summary")
                        .param("year", "2016")
                        .param("month", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]", hasSize(0)));

    }

    @Test
    void getBudgetSummary_GivenYearProvidedWithNoMonth_ThenReturnBadRequest() throws Exception {

        mockMvc.perform(
                get(URI + "/summary")
                        .param("year", "2018"))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void getBudgetSummary_GivenMonthProvidedWithNoYear_ThenReturnBadRequest() throws Exception {

        mockMvc.perform(
                get(URI + "/summary")
                        .param("month", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

}
