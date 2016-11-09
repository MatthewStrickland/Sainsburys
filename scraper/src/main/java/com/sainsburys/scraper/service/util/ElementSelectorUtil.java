package com.sainsburys.scraper.service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sainsburys.scraper.exception.ParsingFailureException;
import com.sainsburys.scraper.json.model.ScraperResultModel;

/**
 * Utility class for selecting elements from the page and parsing.
 */
public final class ElementSelectorUtil {

    /** The scale to represent the kilobytes. */
    private static final int SCALE_KB_DP = 2;

    /** The number of byts in a kb. */
    private static final int NUMBER_BYTES_IN_KB = 1024;

    /** The kilobyte suffix. */
    private static final String KB_SUFFIX = "kb";

    /** String description as constant. */
    private static final String DESCRIPTION = "Description";

    /** Price selector regex. */
    private static final String PRICE_REGEX = "\\d\\.\\d+";

    /** The product title selector. */
    private static final String TITLE_SELECTOR = "div.productSummary div.productTitleDescriptionContainer h1";

    /** The product price selector. */
    private static final String PRICE_SELECTOR = "div.priceTabContainer div.pricing p.pricePerUnit";

    /** The product description selector. */
    private static final String DESCRIPTION_SELECTOR = "h3.productDataItemHeader";

    /** P CSS selector, for description use. */
    private static final String P_SELECTOR = "p";

    /**
     * Private constructor.
     */
    private ElementSelectorUtil() {
    }

    /**
     * Select the title from the elements and set to the model.
     *
     * @param selectedUri the selected URI
     * @param productPage the product page
     * @param model the model to set onto
     * @throws ParsingFailureException if there was not a single title on the
     * page
     */
    public static void setTitleFromElements(final String selectedUri, final Document productPage,
            final ScraperResultModel model) throws ParsingFailureException {
        final Elements elements = productPage.select(TITLE_SELECTOR);
        if (elements.size() != 1) {
            throw new ParsingFailureException(
                    MessageFormat.format("Expected 1 title on page {0} but found {1}", selectedUri, elements.size()));
        }
        model.setTitle(elements.get(0).text());
    }

    /**
     * Calculate the size of the page, convert to kilobytes and set to the
     * model.
     *
     * @param productPage the product page
     * @param model the model to set onto
     */
    public static void setSizeFromElements(final Document productPage, final ScraperResultModel model) {
        // No assets needed, use the size of the page by itself
        final String sizeInKb = BigDecimal.valueOf(productPage.toString().getBytes().length)
                .divide(BigDecimal.valueOf(NUMBER_BYTES_IN_KB), SCALE_KB_DP, RoundingMode.HALF_UP).toString();
        final StringBuilder builder = new StringBuilder(sizeInKb);
        builder.append(KB_SUFFIX);
        model.setSize(builder.toString());
    }

    /**
     * Select the price from the page and set to the model.
     *
     * @param selectedUri the selected URI
     * @param productPage the product page
     * @param model the model to set onto
     * @throws ParsingFailureException if there was not a single price for this
     * product on the page
     */
    public static void setPriceFromElements(final String selectedUri, final Document productPage,
            final ScraperResultModel model) throws ParsingFailureException {
        final Elements elements = productPage.select(PRICE_SELECTOR);
        if (elements.size() != 1) {
            throw new ParsingFailureException(
                    MessageFormat.format("Expected 1 price on page {0} but found {1}", selectedUri, elements.size()));
        }

        // Match the price string with a regex and set this to the model
        final Pattern pattern = Pattern.compile(PRICE_REGEX);
        final Matcher matcher = pattern.matcher(elements.get(0).text());
        int count = 0;
        while (matcher.find()) {
            model.setUnitPrice(new BigDecimal(matcher.group()));
            if (++count > 1) {
                throw new ParsingFailureException(
                        MessageFormat.format("Found more than 1 matching price on page {0}", selectedUri));
            }
        }
    }

    /**
     * Select the description from the page and set to the model.
     *
     * @param selectedUri the selected URI
     * @param productPage the product page
     * @param model the model to set onto
     * @throws ParsingFailureException if there was not a description for this
     * product on the page
     */
    public static void setDescriptionFromElements(final String selectedUri, final Document productPage,
            final ScraperResultModel model) throws ParsingFailureException {
        final Elements elements = productPage.select(DESCRIPTION_SELECTOR);
        final List<Element> descriptions = elements.stream().filter(element -> DESCRIPTION.equals(element.text()))
                .collect(Collectors.toList());
        if (descriptions.size() != 1) {
            throw new ParsingFailureException(MessageFormat.format("Expected 1 description on page {0} but found {1}",
                    selectedUri, descriptions.size()));
        }
        final StringBuilder builder = new StringBuilder();
        descriptions.get(0).nextElementSibling().select(P_SELECTOR).forEach(element -> builder.append(element.text()));
        model.setDescription(builder.toString());
    }

}
