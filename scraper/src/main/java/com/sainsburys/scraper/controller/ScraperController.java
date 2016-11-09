package com.sainsburys.scraper.controller;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sainsburys.scraper.exception.ParsingFailureException;
import com.sainsburys.scraper.json.model.ScraperModel;
import com.sainsburys.scraper.service.api.ScraperService;

/**
 * Scraper controller. The entry point to start orchestrating the scraping and
 * output to the console.
 */
@Component
public class ScraperController {

    /** The logger. */
    private static Logger LOGGER = LoggerFactory.getLogger(ScraperController.class);

    /** The scraper service. */
    @Autowired
    private ScraperService scraperService;

    /** The JSON mapper. */
    @Autowired
    private ObjectMapper mapper;

    /** The bean validator. */
    @Autowired
    private Validator validator;

    /**
     * Scrape the given uri and print to the console.
     *
     * @param scrapableUri the scrapeable URI
     */
    public void scrapeWithUri(final URI scrapableUri) {
        LOGGER.debug("Entered scrapeWithUri with [scrapableUri = {}]", scrapableUri);

        try {
            // Get complete JSON model
            final ScraperModel scraperModel = scraperService.getScraperModelFromUri(scrapableUri);
            // Validate
            final Errors errors = new BeanPropertyBindingResult(scraperModel, "scraperModel");
            validator.validate(scraperModel, errors);
            if (errors.hasErrors()) {
                LOGGER.error("The model has validation errors {}, {}printed information may be incomplete or incorrect",
                        errors, System.lineSeparator());
            }
            // Print to console
            System.out.println(mapper.writeValueAsString(scraperModel));
        } catch (final JsonProcessingException e) {
            LOGGER.error("Error writing JSON", e);
        } catch (final IOException e) {
            LOGGER.error("Error connecting to URI {}", scrapableUri, e);
        } catch (final ParsingFailureException e) {
            LOGGER.error("Unable to parse an element for {}", scrapableUri, e);
        }

    }

}
