package com.sainsburys.scraper.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sainsburys.scraper.exception.ParsingFailureException;
import com.sainsburys.scraper.json.model.ScraperModel;
import com.sainsburys.scraper.service.api.ScraperService;

/**
 * Test class for {@link ScraperController}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ScraperControllerTest {

    /** URI String. */
    private static final String URI = "URI";

    /** The scraper service. */
    @Mock
    private ScraperService scraperService;

    /** The JSON mapper. */
    @Mock
    private ObjectMapper mapper;

    /** The bean validator. */
    @Mock
    private Validator validator;

    /** Class under test. */
    @InjectMocks
    private ScraperController controller;

    /**
     * Test a valid pass through the controller.
     *
     * @throws IOException thrown exception
     * @throws ParsingFailureException thrown exception
     * @throws URISyntaxException thrown exception
     */
    @Test
    public void testScrapeAllValid() throws IOException, ParsingFailureException, URISyntaxException {
        // Set up
        final ScraperModel scraperModel = new ScraperModel();
        final URI uri = new URI(URI);
        Mockito.when(scraperService.getScraperModelFromUri(uri)).thenReturn(scraperModel);

        // Act
        controller.scrapeWithUri(uri);

        // Verify
        Mockito.verify(scraperService).getScraperModelFromUri(uri);
        Mockito.verify(validator).validate(Matchers.eq(scraperModel), Matchers.any(BindingResult.class));
        Mockito.verify(mapper).writeValueAsString(scraperModel);
        Mockito.verifyNoMoreInteractions(scraperService, validator, mapper);
    }

    /**
     * Test that when there are errors on the model then it will still print.
     *
     * @throws IOException thrown exception
     * @throws ParsingFailureException thrown exception
     * @throws URISyntaxException thrown exception
     */
    @Test
    public void testScrapeInvalidModelStillPrints() throws IOException, ParsingFailureException, URISyntaxException {
        // Set up
        final ScraperModel scraperModel = new ScraperModel();
        final URI uri = new URI(URI);
        Mockito.when(scraperService.getScraperModelFromUri(uri)).thenReturn(scraperModel);
        Mockito.doAnswer(invocation -> {
            final BeanPropertyBindingResult result = (BeanPropertyBindingResult) invocation.getArguments()[1];
            result.addError(new ObjectError("scraperModel", "error"));
            return result;
        }).when(validator).validate(Matchers.eq(scraperModel), Matchers.any(Errors.class));

        // Act
        controller.scrapeWithUri(uri);

        // Verify
        Mockito.verify(scraperService).getScraperModelFromUri(uri);
        Mockito.verify(validator).validate(Matchers.eq(scraperModel), Matchers.any(BindingResult.class));
        Mockito.verify(mapper).writeValueAsString(scraperModel);
        Mockito.verifyNoMoreInteractions(scraperService, validator, mapper);
    }

    /**
     * Test then when the service cannot parse then nothing further happens.
     *
     * @throws IOException thrown exception
     * @throws ParsingFailureException thrown exception
     * @throws URISyntaxException thrown exception
     */
    @Test
    public void testScraperServiceThrowsParsingExceptionAndIsCaught()
            throws IOException, ParsingFailureException, URISyntaxException {
        // Set up
        final URI uri = new URI(URI);
        Mockito.doThrow(ParsingFailureException.class).when(scraperService).getScraperModelFromUri(uri);

        // Act
        controller.scrapeWithUri(uri);

        // Verify
        Mockito.verify(scraperService).getScraperModelFromUri(uri);
        Mockito.verifyNoMoreInteractions(scraperService, validator, mapper);
    }

    /**
     * Test that when jackson cannot parse the model then nothing further
     * happens.
     *
     * @throws IOException thrown exception
     * @throws ParsingFailureException thrown exception
     * @throws URISyntaxException thrown exception
     */
    @Test
    public void testMapperThrowsJsonExceptionAndIsCaught()
            throws IOException, ParsingFailureException, URISyntaxException {
        // Set up
        final ScraperModel scraperModel = new ScraperModel();
        final URI uri = new URI(URI);
        Mockito.when(scraperService.getScraperModelFromUri(uri)).thenReturn(scraperModel);
        Mockito.doThrow(JsonProcessingException.class).when(mapper).writeValueAsString(scraperModel);

        // Act
        controller.scrapeWithUri(uri);

        // Verify
        Mockito.verify(scraperService).getScraperModelFromUri(uri);
        Mockito.verify(validator).validate(Matchers.eq(scraperModel), Matchers.any(BindingResult.class));
        Mockito.verify(mapper).writeValueAsString(scraperModel);
        Mockito.verifyNoMoreInteractions(scraperService, validator, mapper);
    }

    /**
     * Test that when the service throws {@link IOException} then nothing
     * further happens.
     * 
     * @throws IOException thrown exception
     * @throws ParsingFailureException thrown exception
     * @throws URISyntaxException thrown exception
     */
    @Test
    public void testScraperServiceThrowsIOExceptionAndIsCaught()
            throws IOException, ParsingFailureException, URISyntaxException {
        // Set up
        final URI uri = new URI(URI);
        Mockito.doThrow(IOException.class).when(scraperService).getScraperModelFromUri(uri);

        // Act
        controller.scrapeWithUri(uri);

        // Verify
        Mockito.verify(scraperService).getScraperModelFromUri(uri);
        Mockito.verifyNoMoreInteractions(scraperService, validator, mapper);
    }
}
