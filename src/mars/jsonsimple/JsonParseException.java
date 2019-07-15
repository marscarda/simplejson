package mars.jsonsimple;
//=================================================================
/**
 * Exception is thrown when a parse error ocurrs.
 * @author Mariano
 */
public class JsonParseException extends Exception
{
    String pars;
    JsonParseException (String message, String parsing)
    {
        super(message);
        pars = parsing;
    }
    public String getTextToParse ()
    {
        return pars;
    }
}
//=================================================================