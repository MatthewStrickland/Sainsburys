package com.sainsburys.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.sainsburys.scraper.controller.ScraperController;

/**
 * Main application.
 * <p>
 * Scrapes the Sainsbury’s grocery site - Ripe Fruits page and returns a JSON
 * array of all the products on the page.
 * <p>
 */
@SpringBootApplication(exclude = { EmbeddedServletContainerAutoConfiguration.class, WebMvcAutoConfiguration.class })
public class ScraperApplication {

    /** The prompt to the console. */
    private static final String CONSOLE_PROMPT = "Enter a valid URL, or hit return to run against the default link:";

    /** The link given for the test. To be used if no other link provided. */
    private static final String TEST_LINK = "http://hiring-tests.s3-website-eu-west-1.amazonaws.com/"
            + "2015_Developer_Scrape/5_products.html";

    /** The logger. */
    private static Logger LOGGER = LoggerFactory.getLogger(ScraperApplication.class);

    /** The scraper controller. */
    @Autowired
    private ScraperController scraperController;

    /**
     * Starts the application
     *
     * @param args command line args
     * @throws IOException if the input fails
     */
    public static void main(final String[] args) throws IOException {
        final ConfigurableApplicationContext context = SpringApplication.run(ScraperApplication.class);

        final ScraperApplication scraperApplication = context.getBean(ScraperApplication.class);
        LOGGER.debug("Started application to scrape with {} arguments", args.length);

        scraperApplication.scrape(args);
        SpringApplication.exit(context);
    }

    /**
     * Begin scraping the provided URI's, or the test link if none other
     * provided.
     *
     * @param candidateUrls the potential URI's to parse
     * @throws IOException if the input fails
     */
    public void scrape(final String[] candidateUrls) throws IOException {
        if (candidateUrls.length > 0) {
            // When there are program arguments run through them all
            final Set<String> uriSet = new HashSet<String>(Arrays.asList(candidateUrls));
            for (final String uriString : uriSet) {
                callController(uriString);
            }
        } else {
            // Potential to loop here
            runFromConsole();
        }
    }

    /**
     * Gets the uri from a given string.
     *
     * @param stringUri the string uri
     * @return the URI
     */
    private static URI getUriFromString(final String stringUri) {
        return UriComponentsBuilder.fromHttpUrl(stringUri).build().toUri();
    }

    /**
     * Call through to the controller. If the URI is invalid continue.
     *
     * @param uriString the uri string
     */
    private void callController(final String uriString) {
        try {
            scraperController.scrapeWithUri(getUriFromString(uriString));
        } catch (final IllegalArgumentException | IllegalStateException e) {
            LOGGER.error("URI {} could not be parsed", uriString, e);
        }
    }

    /**
     * Run the app using console arguments.
     *
     * @throws IOException if the input fails
     */
    private void runFromConsole() throws IOException {
        final String input = ConsoleAsker.ask(CONSOLE_PROMPT);
        if (StringUtils.isEmpty(input)) {
            callController(TEST_LINK);
        } else {
            callController(input);
        }
    }

    /**
     * Wrapper to ask the console for input.
     */
    public static class ConsoleAsker {

        /** The reader. */
        public static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));

        /**
         * Ask the console with a message.
         *
         * @param message the message
         * @return the input
         * @throws IOException if the input fails
         */
        public static String ask(final String message) throws IOException {
            System.out.println(message);
            return READER.readLine();
        }
    }

}
