package com.sainsburys.scraper.exception;

/**
 * Custom checked exception when unable to parse.
 */
public class ParsingFailureException extends Exception {

    /** Default serial UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Message constructor.
     *
     * @param message the exception message
     */
    public ParsingFailureException(final String message) {
        super(message);
    }

}
