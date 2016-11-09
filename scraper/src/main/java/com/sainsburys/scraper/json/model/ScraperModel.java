package com.sainsburys.scraper.json.model;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(value = { ScraperResultModel.TITLE, ScraperResultModel.SIZE, ScraperResultModel.UNIT_PRICE,
        ScraperResultModel.DESCRIPTION })
public class ScraperModel {

    /** Result JSON name. */
    protected static final String RESULTS = "results";

    /** Total JSON name. */
    protected static final String TOTAL = "total";

    /** The results. */
    @NotNull
    @NotEmpty
    @JsonProperty(RESULTS)
    private List<ScraperResultModel> results;

    /** The total. */
    @NotNull
    @JsonProperty(TOTAL)
    private BigDecimal total;

    /**
     * @return the results
     */
    public List<ScraperResultModel> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(final List<ScraperResultModel> results) {
        this.results = results;
    }

    /**
     * @return the total
     */
    public BigDecimal getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(final BigDecimal total) {
        this.total = total;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(results).append(total).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ScraperModel rhs = (ScraperModel) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(results, rhs.results).append(total, rhs.total)
                .isEquals();
    }

}
