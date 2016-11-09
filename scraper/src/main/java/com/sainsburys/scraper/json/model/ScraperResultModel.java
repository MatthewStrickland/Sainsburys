package com.sainsburys.scraper.json.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The JSON model for the results to display.
 */
@JsonPropertyOrder(value = { ScraperResultModel.TITLE, ScraperResultModel.SIZE, ScraperResultModel.UNIT_PRICE,
        ScraperResultModel.DESCRIPTION })
public class ScraperResultModel {

    /** Title JSON name. */
    protected static final String TITLE = "title";

    /** Size JSON name. */
    protected static final String SIZE = "size";

    /** Unit price JSON name. */
    protected static final String UNIT_PRICE = "unit_price";

    /** Description JSON name. */
    protected static final String DESCRIPTION = "description";

    /** The title. */
    @NotNull
    @JsonProperty(TITLE)
    private String title;

    /** The size. */
    @NotNull
    @JsonProperty(SIZE)
    private String size;

    /** The unit price. */
    @NotNull
    @JsonProperty(UNIT_PRICE)
    private BigDecimal unitPrice;

    /** The description. */
    @NotNull
    @JsonProperty(DESCRIPTION)
    private String description;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return the size
     */
    public String getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(final String size) {
        this.size = size;
    }

    /**
     * @return the unitPrice
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * @param unitPrice the unitPrice to set
     */
    public void setUnitPrice(final BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(title).append(size).append(unitPrice).append(description).toHashCode();
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
        final ScraperResultModel rhs = (ScraperResultModel) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(title, rhs.title).append(size, rhs.size)
                .append(unitPrice, rhs.unitPrice).append(description, rhs.description).isEquals();
    }

}
