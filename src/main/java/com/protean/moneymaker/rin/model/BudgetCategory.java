package com.protean.moneymaker.rin.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "budget_category")
public class BudgetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_category_id")
    private Integer budgetCategoryId;

    @Column(name = "category_name")
    private String category;

    @OneToMany(mappedBy = "budgetCategory", cascade = CascadeType.ALL)
    private Set<TransactionCategory> transactionCategories;

    public BudgetCategory() {
    }

    public BudgetCategory(String category, Set<TransactionCategory> transactionCategories) {
        this.category = category;
        this.transactionCategories = transactionCategories;
    }

    public Integer getBudgetCategoryId() {
        return budgetCategoryId;
    }

    public void setBudgetCategoryId(Integer budgetCategoryId) {
        this.budgetCategoryId = budgetCategoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Set<TransactionCategory> getTransactionCategories() {
        return transactionCategories;
    }

    public void setTransactionCategories(Set<TransactionCategory> transactionCategories) {
        this.transactionCategories = transactionCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetCategory that = (BudgetCategory) o;
        return Objects.equals(budgetCategoryId, that.budgetCategoryId) &&
                Objects.equals(category, that.category) &&
                Objects.equals(transactionCategories, that.transactionCategories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(budgetCategoryId, category, transactionCategories);
    }

    @Override
    public String toString() {
        return "BudgetCategory{" +
                "budgetCategoryId=" + budgetCategoryId +
                ", category='" + category + '\'' +
                ", transactionCategories=" + transactionCategories +
                '}';
    }
}
