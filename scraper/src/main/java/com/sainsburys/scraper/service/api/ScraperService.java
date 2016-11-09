package com.sainsburys.scraper.service.api;

import java.io.IOException;
import java.net.URI;

import com.sainsburys.scraper.exception.ParsingFailureException;
import com.sainsburys.scraper.json.model.ScraperModel;

/**
 * Scraper service interface.
 */
public interface ScraperService {

    /**
     * Gets the completed scraper model from a given URI.
     *
     * @param uri the uri to use
     * @return a completed scraper model
     * @throws IOException if there was an issue connection to the uri
     * @throws ParsingFailureException if there was an issue parsing
     */
    ScraperModel getScraperModelFromUri(URI uri) throws IOException, ParsingFailureException;

}
