package com.protean.moneymaker.rin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "budget_category_type")
public class BudgetCategoryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_category_type_id")
    private Integer id;

    @Column(name = "category_type_name")
    private String categoryType;

    public BudgetCategoryType() {
    }

    public BudgetCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetCategoryType that = (BudgetCategoryType) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(categoryType, that.categoryType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryType);
    }

    @Override
    public String toString() {
        return "BudgetCategoryType{" +
                "id=" + id +
                ", categoryType='" + categoryType + '\'' +
                '}';
    }
}