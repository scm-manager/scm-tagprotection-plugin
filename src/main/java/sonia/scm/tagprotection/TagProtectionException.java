package sonia.scm.tagprotection;

/**
 * @author Oliver Milke
 */
public class TagProtectionException extends RuntimeException {

    /**
     * Field description
     */
    private static final long serialVersionUID = -980531376742552L;

    //~--- constructors ---------------------------------------------------------

    /**
     * Constructs ...
     */
    public TagProtectionException(String message) {

        super(message);
    }
}
