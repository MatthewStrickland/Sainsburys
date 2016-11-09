package com.sainsburys.scraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sainsburys.scraper.ScraperApplication.ConsoleAsker;
import com.sainsburys.scraper.controller.ScraperController;

/**
 * Test class for {@link ScraperApplication}. Note EclEmma code coverage isn't
 * compatible with PowerMock, see
 * {@link https://github.com/jayway/powermock/issues/422}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ConsoleAsker.class)
public class ScraperApplicationTest {

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** A parseable URI. */
    private static final String VALID_URI = "http://www.sainsburys.co.uk";

    /** An unparseable URI. */
    private static final String INVALID_URI = "http://www.!.co.uk";

    /** The scraper controller. */
    @Mock
    private ScraperController controller;

    /** Class under test. */
    @InjectMocks
    private ScraperApplication application;

    /** Expected exception. */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test when the app entry point is called with an empty string then the
     * controller is still called.
     *
     * @throws IOException thrown exception
     */
    @Test
    public void testNoArgsConsoleMain() throws IOException {
        // Set up
        PowerMockito.mockStatic(ConsoleAsker.class);
        Mockito.when(ConsoleAsker.ask(Matchers.anyString())).thenReturn(EMPTY_STRING);

        // Act
        application.scrape(new String[] {});

        // Verify
        Mockito.verify(controller).scrapeWithUri(Matchers.any(URI.class));
        Mockito.verifyNoMoreInteractions(controller);
    }

    /**
     * Test when the app entry point is called with an valid URI then the
     * controller is still called.
     *
     * @throws IOException thrown exception
     */
    @Test
    public void testArgsConsoleMain() throws IOException {
        // Set up
        PowerMockito.mockStatic(ConsoleAsker.class);
        Mockito.when(ConsoleAsker.ask(Matchers.anyString())).thenReturn(VALID_URI);

        // Act
        application.scrape(new String[] {});

        // Verify
        Mockito.verify(controller).scrapeWithUri(Matchers.any(URI.class));
        Mockito.verifyNoMoreInteractions(controller);
    }

    /**
     * Test when the input from the console fails an {@link IOException} is
     * thrown.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testArgsConsoleMainThrowsException() throws Exception {
        // Set up
        PowerMockito.mockStatic(ConsoleAsker.class);
        PowerMockito.doThrow(new IOException()).when(ConsoleAsker.class, "ask", Matchers.anyString());
        thrown.expect(IOException.class);

        // Act
        application.scrape(new String[] {});

        // Verify
        Mockito.verifyNoMoreInteractions(controller);
    }

    /**
     * Test when valid args are passed through the main method then the
     * controller is still called.
     *
     * @throws URISyntaxException thrown exception
     * @throws IOException thrown exception
     */
    @Test
    public void testArgsMainValidUri() throws URISyntaxException, IOException {
        // Act
        application.scrape(new String[] { VALID_URI });

        // Verify
        Mockito.verify(controller).scrapeWithUri(new URI(VALID_URI));
        Mockito.verifyNoMoreInteractions(controller);
    }

    /**
     * Test when invalid args are passed through the main method then the
     * controller is never called.
     *
     * @throws IOException thrown exception
     */
    @Test
    public void testArgsMainInvalidUri() throws IOException {
        // Act
        application.scrape(new String[] { INVALID_URI });

        // Verify
        Mockito.verifyNoMoreInteractions(controller);
    }

    /**
     * Test when valid and invalid args are passed through the main method then
     * the controller is called only the amount of times we have a valid URI.
     *
     * @throws URISyntaxException thrown exception
     * @throws IOException thrown exception
     */
    @Test
    public void testArgsMainValidAndInvalidUri() throws URISyntaxException, IOException {
        // Act
        application.scrape(new String[] { VALID_URI, INVALID_URI });

        // Verify
        Mockito.verify(controller).scrapeWithUri(new URI(VALID_URI));
        Mockito.verifyNoMoreInteractions(controller);
    }

}
