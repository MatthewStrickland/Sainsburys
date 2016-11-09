package com.sainsburys.scraper.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sainsburys.scraper.exception.ParsingFailureException;
import com.sainsburys.scraper.json.model.ScraperModel;
import com.sainsburys.scraper.json.model.ScraperResultModel;
import com.sainsburys.scraper.service.api.ScraperService;
import com.sainsburys.scraper.service.util.ElementSelectorUtil;

/**
 * Scraper service test. Note EclEmma code coverage isn't compatible with
 * PowerMock, see {@link https://github.com/jayway/powermock/issues/422}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Jsoup.class, ElementSelectorUtil.class })
public class ScraperServiceTest {

    /** A default Price. */
    private static final String PRICE = "1.00";

    /** A default price (as {@link BigDecimal}). */
    private static final BigDecimal BIGDECIMAL_PRICE = BigDecimal.valueOf(1);

    /** A default size. */
    private static final String SIZE = "10kb";

    /** A default description. */
    private static final String DESCRIPTION = "Description";

    /** A default title. */
    private static final String TITLE = "Title";

    /** A default URL. */
    private static final String DEFAULT_URL = "aUrl";

    /** A new URL. */
    private static final String NEW_URL_1 = "newUrl1";

    /** A new URL. */
    private static final String NEW_URL_2 = "newUrl2";

    /** A new URL. */
    private static final String NEW_URL_3 = "newUrl3";

    /** Class under test. */
    private ScraperService service;

    /** Expected exception. */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test setup.
     */
    @Before
    public void setUp() {
        service = new ScraperServiceProvider();
        PowerMockito.mockStatic(Jsoup.class);
        PowerMockito.spy(ElementSelectorUtil.class);
    }

    /**
     * Test a successful pass through with default values.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testReturnModel() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultTitle(TITLE);
        defaultDescription(DESCRIPTION);
        defaultPrice(BIGDECIMAL_PRICE);
        defaultSize(SIZE);

        initialiseLink(NEW_URL_1, new String[] {}, true);

        // Act
        final ScraperModel model = service.getScraperModelFromUri(new URI(DEFAULT_URL));

        // Assert
        assertThat(model.getResults(), hasSize(1));
        assertThat(model.getTotal().toString(), equalTo(PRICE));
        final ScraperResultModel resultModel = model.getResults().get(0);
        assertThat(resultModel.getTitle(), equalTo(TITLE));
        assertThat(resultModel.getDescription(), equalTo(DESCRIPTION));
        assertThat(resultModel.getUnitPrice().toString(), equalTo(PRICE));
        assertThat(resultModel.getSize(), equalTo(SIZE));
    }

    /**
     * Test the ability to parse and add prices.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testReturnModelWithPricesAdded() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1, NEW_URL_2, NEW_URL_3 }, false);

        defaultTitle(TITLE);
        defaultDescription(DESCRIPTION);
        defaultSize(SIZE);

        initialiseLink(NEW_URL_1, new String[] { "abc£1.50bca" }, true);
        initialiseLink(NEW_URL_2, new String[] { "0.30" }, true);
        initialiseLink(NEW_URL_3, new String[] { "£££abc£2.20!" }, true);

        // Act
        final ScraperModel model = service.getScraperModelFromUri(new URI(DEFAULT_URL));

        // Assert
        assertThat(model.getResults(), hasSize(3));
        assertThat(model.getTotal().toString(), equalTo("4.00"));
    }

    /**
     * Test that title can be parsed from the document tree.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testReturnModelWithTitle() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultPrice(BIGDECIMAL_PRICE);
        defaultDescription(DESCRIPTION);
        defaultSize(SIZE);

        final String newTitle = "New Title";
        initialiseLink(NEW_URL_1, new String[] { newTitle }, true);

        // Act
        final ScraperModel model = service.getScraperModelFromUri(new URI(DEFAULT_URL));

        // Assert
        assertThat(model.getResults(), hasSize(1));
        final ScraperResultModel resultModel = model.getResults().get(0);
        assertThat(resultModel.getTitle(), equalTo(newTitle));
    }

    /**
     * Test that size can be parsed from the document tree.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testReturnModelWithSize() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultPrice(BIGDECIMAL_PRICE);
        defaultDescription(DESCRIPTION);
        defaultTitle(TITLE);

        initialiseLink(NEW_URL_1, new String[] {}, true);

        // Act
        final ScraperModel model = service.getScraperModelFromUri(new URI(DEFAULT_URL));

        // Assert
        assertThat(model.getResults(), hasSize(1));
        final ScraperResultModel resultModel = model.getResults().get(0);
        assertThat(resultModel.getSize(), endsWith("kb"));

    }

    /**
     * Test that description can be parsed from the document tree.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testReturnModelWithDescription() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultPrice(BIGDECIMAL_PRICE);
        defaultSize(SIZE);
        defaultTitle(TITLE);

        final String fullDescription = "Textual description";
        final Elements elements = initialiseLink(NEW_URL_1, new String[] { DESCRIPTION }, true);
        mockInnerDescription(fullDescription, elements);

        // Act
        final ScraperModel model = service.getScraperModelFromUri(new URI(DEFAULT_URL));

        // Assert
        assertThat(model.getResults(), hasSize(1));
        final ScraperResultModel resultModel = model.getResults().get(0);
        assertThat(resultModel.getDescription(), equalTo(fullDescription));

    }

    /**
     * Test that title parsing will throw an exception when there is no Title
     * element.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testExceptionWithTitle() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultDescription(DESCRIPTION);
        defaultSize(SIZE);
        defaultPrice(BIGDECIMAL_PRICE);

        initialiseLink(NEW_URL_1, new String[] {}, true);

        thrown.expect(ParsingFailureException.class);
        thrown.expectMessage(NEW_URL_1);

        // Act
        service.getScraperModelFromUri(new URI(DEFAULT_URL));
    }

    /**
     * Test that price parsing will throw an exception when there is no Price
     * element.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testExceptionWithPrice() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultDescription(DESCRIPTION);
        defaultSize(SIZE);
        defaultTitle(TITLE);

        initialiseLink(NEW_URL_1, new String[] {}, true);

        thrown.expect(ParsingFailureException.class);
        thrown.expectMessage(NEW_URL_1);

        // Act
        service.getScraperModelFromUri(new URI(DEFAULT_URL));
    }

    /**
     * Test the ability to parse and add prices.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testExceptionWithPriceWithTooManyMatches() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultTitle(TITLE);
        defaultDescription(DESCRIPTION);
        defaultSize(SIZE);

        thrown.expect(ParsingFailureException.class);
        thrown.expectMessage(NEW_URL_1);

        initialiseLink(NEW_URL_1, new String[] { "abc£1.50bca1.2" }, true);

        // Act
        service.getScraperModelFromUri(new URI(DEFAULT_URL));
    }

    /**
     * Test that description parsing will throw an exception when there is no
     * Description element.
     *
     * @throws Exception thrown exception
     */
    @Test
    public void testExceptionWithDescription() throws Exception {
        // Set up
        initialiseLink(DEFAULT_URL, new String[] { NEW_URL_1 }, false);

        defaultPrice(BIGDECIMAL_PRICE);
        defaultSize(SIZE);
        defaultTitle(TITLE);

        initialiseLink(NEW_URL_1, new String[] {}, true);

        thrown.expect(ParsingFailureException.class);
        thrown.expectMessage(NEW_URL_1);

        // Act
        service.getScraperModelFromUri(new URI(DEFAULT_URL));
    }

    /**
     * Adds the inner description to the child elements.
     *
     * @param fullDescription the description to add
     * @param elements the elements
     */
    private void mockInnerDescription(final String fullDescription, final Elements elements) {
        final Element mockedElement = elements.get(0);
        final Elements siblingElements = new Elements();
        final Element siblingElement = Mockito.mock(Element.class);
        final Element descriptionElement = Mockito.mock(Element.class);
        siblingElements.add(descriptionElement);
        Mockito.when(mockedElement.nextElementSibling()).thenReturn(siblingElement);
        Mockito.when(siblingElement.select(Matchers.anyString())).thenReturn(siblingElements);
        Mockito.when(descriptionElement.text()).thenReturn(fullDescription);
    }

    /**
     * Initialses an outer URL, and sets the elements underneath with
     * appropriate values.
     *
     * @param outerUrl the outer URL
     * @param elementReturns the values to set to the inner elements
     * @param forText if we care about an elements text over the attr
     * @return the mocked elements
     * @throws IOException thrown exception
     */
    private Elements initialiseLink(final String outerUrl, final String[] elementReturns, final boolean forText)
            throws IOException {
        final Connection mockConnection = Mockito.mock(Connection.class);
        final Document mockDocument = Mockito.mock(Document.class);
        final Elements elements = new Elements();
        Mockito.when(Jsoup.connect(outerUrl)).thenReturn(mockConnection);
        Mockito.when(mockConnection.get()).thenReturn(mockDocument);
        Mockito.when(mockDocument.select(Matchers.anyString())).thenReturn(elements);

        for (int i = 0; i < elementReturns.length; i++) {
            final Element mockElement = Mockito.mock(Element.class);
            elements.add(mockElement);
            if (forText) {
                Mockito.when(mockElement.text()).thenReturn(elementReturns[i]);
            } else {
                Mockito.when(mockElement.attr(Matchers.anyString())).thenReturn(elementReturns[i]);
            }
        }

        return elements;
    }

    /**
     * Sets a default size.
     *
     * @param size the size
     * @throws Exception thrown exception
     */
    private void defaultSize(final String size) throws Exception {
        PowerMockito.doAnswer(invocation -> {
            final ScraperResultModel result = (ScraperResultModel) invocation.getArguments()[1];
            result.setSize(size);
            return result;
        }).when(ElementSelectorUtil.class, "setSizeFromElements", Matchers.any(Document.class),
                Matchers.any(ScraperResultModel.class));
    }

    /**
     * Sets a default description.
     *
     * @param description the description
     * @throws Exception thrown exception
     */
    private void defaultDescription(final String description) throws Exception {
        PowerMockito.doAnswer(invocation -> {
            final ScraperResultModel result = (ScraperResultModel) invocation.getArguments()[2];
            result.setDescription(description);
            return result;
        }).when(ElementSelectorUtil.class, "setDescriptionFromElements", Matchers.any(String.class),
                Matchers.any(Document.class), Matchers.any(ScraperResultModel.class));
    }

    /**
     * Sets a default price.
     *
     * @param price the price
     * @throws Exception thrown exception
     */
    private void defaultPrice(final BigDecimal price) throws Exception {
        PowerMockito.doAnswer(invocation -> {
            final ScraperResultModel result = (ScraperResultModel) invocation.getArguments()[2];
            result.setUnitPrice(price.setScale(2, RoundingMode.HALF_UP));
            return result;
        }).when(ElementSelectorUtil.class, "setPriceFromElements", Matchers.any(String.class),
                Matchers.any(Document.class), Matchers.any(ScraperResultModel.class));
    }

    /**
     * Sets a default title.
     *
     * @param title the title
     * @throws Exception thrown exception
     */
    private void defaultTitle(final String title) throws Exception {
        PowerMockito.doAnswer(invocation -> {
            final ScraperResultModel result = (ScraperResultModel) invocation.getArguments()[2];
            result.setTitle(title);
            return result;
        }).when(ElementSelectorUtil.class, "setTitleFromElements", Matchers.any(String.class),
                Matchers.any(Document.class), Matchers.any(ScraperResultModel.class));
    }

}
