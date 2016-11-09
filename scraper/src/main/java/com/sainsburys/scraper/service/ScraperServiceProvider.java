package com.sainsburys.scraper.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sainsburys.scraper.exception.ParsingFailureException;
import com.sainsburys.scraper.json.model.ScraperModel;
import com.sainsburys.scraper.json.model.ScraperResultModel;
import com.sainsburys.scraper.service.api.ScraperService;
import com.sainsburys.scraper.service.util.ElementSelectorUtil;

/**
 * Scraper service provider implementation.
 */
@Service
public class ScraperServiceProvider implements ScraperService {

    /** The product CSS selector. */
    private static final String PRODUCT_SELECTOR = "div.productInfo a";

    /** The product href selector. */
    private static final String HREF_ATTRIBUTE_SELECTOR = "href";

    /** The logger. */
    private static Logger LOGGER = LoggerFactory.getLogger(ScraperServiceProvider.class);

    @Override
    public ScraperModel getScraperModelFromUri(final URI uri) throws IOException, ParsingFailureException {
        LOGGER.debug("Entered getJsonFromUri with [uri = {}]", uri);
        final Document document = Jsoup.connect(uri.toString()).get();
        final Set<String> selectedUris = document.select(PRODUCT_SELECTOR).stream()
                .map(element -> element.attr(HREF_ATTRIBUTE_SELECTOR)).collect(Collectors.toSet());

        // Create the model
        final ScraperModel scraperModel = new ScraperModel();
        final List<ScraperResultModel> scraperResultModels = new ArrayList<ScraperResultModel>(selectedUris.size());
        scraperModel.setResults(scraperResultModels);
        scraperModel.setTotal(parseAllUri(selectedUris, scraperResultModels));

        return scraperModel;
    }

    /**
     * Parse all the URIs and create the model, counting the total price as we
     * go.
     *
     * @param selectedUris the selected URIs from the page
     * @param scraperResultModels the model list to populate
     * @return the total price
     * @throws IOException if there was an issue connection to the uri
     * @throws ParsingFailureException if there was an issue parsing
     */
    private static BigDecimal parseAllUri(final Set<String> selectedUris,
            final List<ScraperResultModel> scraperResultModels) throws IOException, ParsingFailureException {
        BigDecimal total = new BigDecimal(0);
        for (final String selectedUri : selectedUris) {
            final Document productPage = Jsoup.connect(selectedUri).get();

            // Create the results model
            final ScraperResultModel scraperResultModel = new ScraperResultModel();
            ElementSelectorUtil.setTitleFromElements(selectedUri, productPage, scraperResultModel);
            ElementSelectorUtil.setSizeFromElements(productPage, scraperResultModel);
            ElementSelectorUtil.setPriceFromElements(selectedUri, productPage, scraperResultModel);
            ElementSelectorUtil.setDescriptionFromElements(selectedUri, productPage, scraperResultModel);

            total = total.add(scraperResultModel.getUnitPrice());
            scraperResultModels.add(scraperResultModel);
        }
        return total;
    }

}
