package mars.jsonsimple;
//=============================================================
/**
 *
 * @author Mariano
 */
public class JsonSerializer 
{
    //=========================================================
    /**
     * Returns a JSON text corresponding to a Json object
     * @param jsonobj The Json Object to be serialized.
     * @return Json format string.
     */
    public static String serializeJsonObject (JsonObject jsonobj)
    {
        return serializeJsonObject(jsonobj, false);
    }
    //=========================================================
    public static String serializeJsonObject (JsonObject jsonobj, boolean compact)
    {
        if (compact) return jsonobj.getTextCompact();
        return jsonobj.getText();
    }
    //=========================================================
}
//=============================================================