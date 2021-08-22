/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mars.jsonsimple;
//==========================================================================
/**
 * This class parses a JSON text and builds a JSON object.
 * @author Mariano
 */
public class JsonParser {
    //======================================================================
    /**
     * Parses a json text and returns a json object
     * @param jtoparse
     * @param jsontxt The text to parse.
     * @return A json object
     * @throws mars.jsonsimple.JsonParseException
     * @throws Exception 
     */
    public static JsonObject parseAndCreateJsonObject (String jtoparse) throws JsonParseException {
        //==================================================================
        String nettext = netText(jtoparse);
        if (!testJsonObj(nettext))
            throw new JsonParseException("Invalid Json content", jtoparse);
        String objtext = innerText(nettext);
        return createObject (objtext);
        //==================================================================
    }
    //======================================================================
    /**
     * Crea un objeto Json a partir del texto de este.
     * @param jsontxt
     * @return Un objeto json
     * @throws JsonParseException
     */
    private static JsonObject createObject (String jsontxt) throws JsonParseException {
        //Received a text free of braces in its ends. Now we split it pairs.
        String[] strPairs = splitByFreeCommas(jsontxt);
        JsonObject obj = new JsonObject();
        for (String pair : strPairs) {
            obj.addPair(createPair(pair));
        }
        return obj;
    }
    //**********************************************************************
    private static JsonPair createPair (String strpair) throws JsonParseException {
        //==============================================================
        //We find the index of the colon and split in two halves.
        int colonind = findColonIndex(strpair);
        //--------------------------------------------------------------
        //Key half
        String keyhalf = strpair.substring(0, colonind);
        //--------------------------------------------------------------
        //Value half
        colonind++;
        if (colonind >= strpair.length())
            throw new JsonParseException("Failed to parse a JSON pair", strpair);
        String valuehalf = strpair.substring(colonind);
        //--------------------------------------------------------------
        String key = innerText(netText(keyhalf));
        String val = netText(valuehalf);
        //**************************************************************
        InternalValue intrnlval = createValue(val);
        JsonPair pair;
        switch (intrnlval.type) {
            //-----------------------------------------------
            case INTEGER:
                pair = new JsonPair(key, Long.valueOf(intrnlval.txtval));
                return pair;
            //-----------------------------------------------
            case FLOAT:
                pair = new JsonPair(key, Float.valueOf(intrnlval.txtval));
                return pair;
            //-----------------------------------------------
            case STRING:
                pair = new JsonPair(key, innerText(val));
                return pair;
            //-----------------------------------------------
            case BOOLEAN:
                pair = new JsonPair(key, Boolean.valueOf(intrnlval.txtval));
                return pair;
            //-----------------------------------------------
            case JSONOBJECT: {
                String text = innerText(intrnlval.txtval);
                JsonObject object = createObject(text);
                pair = new JsonPair(key, object);
                return pair;
            }
            //-----------------------------------------------
            case JSONARRAY: {
                String text = innerText(intrnlval.txtval);
                String[] textobjs = splitByFreeCommas(text);
                String netobj;
                int count = textobjs.length;
                JsonObject[] objects = new JsonObject[count];
                for (int n = 0; n < count; n++) {
                    netobj = netText(textobjs[n]);
                    if (!testJsonObj(netobj))
                        throw new JsonParseException("Failed to parse. Invalid Json Array", netobj);
                    objects[n] = createObject(innerText(netobj));
                }
                pair = new JsonPair(key, objects);
                return pair;
            }
            //-----------------------------------------------
            case NULL:
                pair = new JsonPair(key);
                return pair;
            //-----------------------------------------------
        }
        //=================================================================
        throw new JsonParseException("Failed to parse a JSON pair", strpair);
        //=================================================================
    }
    //**********************************************************************
    /**
     * Finds the colon index in the pair
     * @param strpair
     * @return
     * @throws JsonParseException 
     */
    private static int findColonIndex (String strpair) throws JsonParseException {
        //=================================================================
        int len = strpair.length();
        int cp;
        int index = 0;
        boolean accept;
        //-----------------------------------------------------------------
        //Indicates if a key name was present before we find the colon
        boolean keyfound = false; 
        //=================================================================
        TextQualifier txtq = TextQualifier.NONE;
        //=================================================================
        //We search the colon (:) character outside a txt qualifyer
        while (index < len) {
            cp = strpair.codePointAt(index);
            switch (txtq) {
                //=========================================================
                case NONE: {
                    //-----------------------------------------------------
                    if (cp == 34) { txtq = TextQualifier.DOUBLE; index++; continue; }
                    if (cp == 39) { txtq = TextQualifier.SINGLE; index++; continue; }
                    //-----------------------------------------------------
                    if (cp == 58) {
                        if (!keyfound) //We found a colon but no key before.
                            throw new JsonParseException("Failed to parse a JSON pair", strpair);
                        return index;
                    }
                    //-----------------------------------------------------
                    accept = false;
                    if (cp == 9) accept = true;
                    if (cp == 10) accept = true;
                    if (cp == 13) accept = true;
                    if (cp == 32) accept = true;
                    if (!accept)
                        throw new JsonParseException("Failed to parse a JSON pair", strpair);
                    //-----------------------------------------------------
                }
                //=========================================================
                //We have a quotation open. We close it
                case DOUBLE: {
                    if (cp == 34) { 
                        if (strpair.codePointBefore(index) == 92) continue;
                        txtq = TextQualifier.NONE; 
                        keyfound = true;
                    }
                    index++;
                    continue; 
                    
                }
                //=========================================================
                //We have a quotation open. We close it
                case SINGLE: {
                    if (cp == 39) { 
                        if (strpair.codePointBefore(index) == 92) continue;
                        txtq = TextQualifier.NONE; 
                        keyfound = true;
                    }
                    index++;
                    continue; 
                }
                //=========================================================
            }
        }        
        //=================================================================
        throw new JsonParseException("Failed to parse a JSON pair", strpair);
        //=================================================================
    }
    //**********************************************************************
    private static InternalValue createValue (String rawtext) throws JsonParseException {
        //=============================================
        InternalValue value = new InternalValue();
        String text = netText(rawtext);
        value.txtval = text;
        //=============================================
        if (testInteger(text)) {
            value.type = JsonValueType.INTEGER;
            return value;
        }
        //=============================================
        if (testFloat(text)) {
            value.type = JsonValueType.FLOAT;
            return value;
        }
        //=============================================
        if (testString(text)) {
            value.type = JsonValueType.STRING;
            return value;
        }
        //=============================================
        if (testNull(text)) {
            value.type = JsonValueType.NULL;
            return value;
        }
        //=============================================
        if (testBoolean(text)) {
            value.type = JsonValueType.BOOLEAN;
            return value;
        }
        //=============================================
        if (testJsonObj(text)) {
            value.type = JsonValueType.JSONOBJECT;
            return value;
        }
        //=============================================
        if (testJsonObjArray(text)) {
            value.type = JsonValueType.JSONARRAY;
            return value;
        }
        //=============================================
        //Nothing we can identify.
        throw new JsonParseException("Json Parse failed", rawtext);
        //=============================================
    }
    //**********************************************************************
    /**
     * Creates a string free of spaces and L/F in its extremes.
     * @param textin
     * @return
     * @throws JsonParseException 
     */
    private static String netText (String textin) throws JsonParseException {
        //------------------------------------------------------------------
        int rawlen = textin.length();
        int indfrom = 0;
        int indto = 0;
        int cp;
        //------------------------------------------------------------------
        //We find the actual start of the value.
        for (int n = 0; n < rawlen; n++) {
            indfrom = n;
            cp = textin.codePointAt(n);
            if (cp == 9) continue;
            if (cp == 10) continue;
            if (cp == 13) continue;
            if (cp == 32) continue;
            break;
        }
        //------------------------------------------------------------------
        //We find the actual end of the value
        for (int n = rawlen - 1; n >= 0; n--) {
            cp = textin.codePointAt(n);
            if (cp == 9) continue;
            if (cp == 10) continue;
            if (cp == 13) continue;
            if (cp == 32) continue;
            indto = n;
            break;
        }
        //------------------------------------------------------------------
        if (indfrom > indto) throw new JsonParseException("Invalid text found", textin);
        if (indfrom == indto) {
            cp = textin.codePointAt(indfrom);
            switch (cp) {
                case 9:
                case 10:
                case 13:
                case 32:
                    throw new JsonParseException("Invalid text found", textin);
            }
        }
        //------------------------------------------------------------------
        String actualtext = textin.substring(indfrom, indto + 1);
        return actualtext;
        //------------------------------------------------------------------
    }
    //**********************************************************************
    /**
     * Test if the string represents a null value
     * @param textin
     * @return 
     */
    private static boolean testNull (String textin) {
        return (textin.compareToIgnoreCase("NULL") == 0);
    }
    //======================================================================
    /**
     * Tests if the text represents a boolean value
     * @param textin
     * @return 
     */
    private static boolean testBoolean (String textin) {
        if (textin.compareToIgnoreCase("TRUE") == 0) return true;
        return (textin.compareToIgnoreCase("FALSE") == 0);
    }
    //**********************************************************************
    /**
     * We test if the text represents an integer number.
     * @param value
     * @return 
     */
    private static boolean testInteger (String text) {
        //------------------------------------------------------------------
        int cp;
        //------------------------------------------------------------------
        //First test. Accepted numbers, minus sign and floating point.
        for (int n = 0; n < text.length(); n++) {
            cp = text.codePointAt(n);
            if (cp >= 47 && cp <= 57) continue;
            if (cp == 45) continue;
            return false;
        }
        //------------------------------------------------------------------
        //Second test. Minus sign not accepted beyon index 0
        for (int n = 1; n < text.length(); n++) {
            cp = text.codePointAt(n);
            if (cp == 45) return false;
        }
        //------------------------------------------------------------------
        //Third test. If it is only a minus sign is not an integer
        if (text.length() == 1)
            if (text.codePointAt(0) == 45) return false;
        //------------------------------------------------------------------
        return true;
        //------------------------------------------------------------------
    }
    //**********************************************************************
    /**
     * Tests if the text represents a float number.
     * @param text
     * @return 
     */
    private static boolean testFloat (String text) {
        //------------------------------------------------------------------
        int cp;
        boolean negative = false;
        boolean numfound = false;
        int fptcount = 0;
        int fptat = 1;
        //------------------------------------------------------------------
        //First test. Accepted numbers or minus sign
        for (int n = 0; n < text.length(); n++) {
            cp = text.codePointAt(n);
            if (cp >= 47 && cp <= 57) { numfound = true; continue; }
            if (cp == 45) { negative = true; continue; }
            if (cp == 46) { fptcount++; fptat = n; continue; }
            return false;
        }
        //------------------------------------------------------------------
        if (!numfound) return false;
        if (fptcount > 1) return false;
        //------------------------------------------------------------------
        //Second test. Minus sign not accepted beyon index 0
        for (int n = 1; n < text.length(); n++) {
            cp = text.codePointAt(n);
            if (cp == 45) return false;
        }
        //------------------------------------------------------------------
        //Check the floatin point is not in a wron place
        if (!negative && fptat == 0) return false;
        if (negative && fptat == 1) return false;
        if (fptat == text.length() - 1) return false;
        //------------------------------------------------------------------
        return true;
        //------------------------------------------------------------------
    }
    //**********************************************************************
    private static boolean testString (String text) {
        //------------------------------------------------------------------
        int len = text.length();
        if (len < 2) return false;
        //------------------------------------------------------------------
        int cp;
        int qcp = 0;
        //------------------------------------------------------------------
        //The first char must be some quote. And we remeber if single or double.
        cp = text.codePointAt(0);
        if (cp == 34 || cp == 39) qcp = cp;
        if (qcp == 0) return false;
        //------------------------------------------------------------------
        //Last char must be equal to the first.
        cp = text.codePointAt(len -1);
        if (cp != qcp) return false;
        //------------------------------------------------------------------
        //If we find the text qualifier must be preceded with "\". 
        for (int n = 1; n < len -1; n++) {
            cp = text.codePointAt(n);
            if (cp == qcp) 
                if (text.codePointBefore(n) != 92) return false;
        }
        //------------------------------------------------------------------
        return true;
        //------------------------------------------------------------------
    }
    //**********************************************************************
    /**
     * Tests if the text represents a json object.
     * @param text
     * @return 
     */
    private static boolean testJsonObj (String text) {
        //------------------------------------------------------------------
        int len = text.length();
        if (len < 2) return false;
        //------------------------------------------------------------------
        int cp;
        //------------------------------------------------------------------
        //First char must be an open brace.
        cp = text.codePointAt(0);
        if (cp != 123) return false;
        cp = text.codePointAt(len -1);
        if (cp != 125) return false;
        //------------------------------------------------------------------
        return true;
        //------------------------------------------------------------------
    }
    //**********************************************************************
    /**
     * Test if the text represents a json objects array.
     * @param text
     * @return 
     */
    private static boolean testJsonObjArray (String text) {
        //------------------------------------------------------------------
        int len = text.length();
        if (len < 2) return false;
        //------------------------------------------------------------------
        int cp;
        //------------------------------------------------------------------
        //First char must be an open brace.
        cp = text.codePointAt(0);
        if (cp != 91) return false;
        cp = text.codePointAt(len -1);
        if (cp != 93) return false;
        //------------------------------------------------------------------
        return true;
        //------------------------------------------------------------------
    }
    //**********************************************************************
    /**
     * Removes the first and last character from a text
     * @param text
     * @return 
     */
    private static String innerText (String text) throws JsonParseException {
        int len = text.length();
        if (len < 2)
            throw new JsonParseException("Failed to parse Json", text);
        if (len < 3) return "";
        return text.substring(1, len - 1);
    }
    //**********************************************************************
    /**
     * Cut a text in comas that are out of any quote, braces or brackers.
     * @param jsonstr
     * @return
     * @throws JsonParseException 
     */
    private static String[] splitByFreeCommas (String jsonstr) throws JsonParseException {
        //******************************************************************
        int len = jsonstr.length();
        int cp;
        int index = 0;
        int childrenopen;
        int textopen;
        int singleopen = 0;
        int doubleopen = 0;
        int bracesopen = 0;
        int bracketopen = 0;
        int indfrom = 0;
        //==================================================================
        String[] textParts = new String[0];
        //******************************************************************
        while (index < len) {
            //==============================================================
            childrenopen = bracesopen + bracketopen;
            textopen = singleopen + doubleopen;
            cp = jsonstr.codePointAt(index);
            //==============================================================
            switch (cp) {
                //----------------------------------------------------------
                case 34:
                    if (childrenopen > 0) break;
                    if (doubleopen == 0) { doubleopen++; break; }
                    if (jsonstr.codePointBefore(index) == 92) break;
                    doubleopen--; break;
                //----------------------------------------------------------
                case 39:
                    if (childrenopen > 0) break;
                    if (singleopen == 0) { singleopen++; break; }
                    if (jsonstr.codePointBefore(index) == 92) break;
                    singleopen--; break;
                //----------------------------------------------------------
                case 123:
                    if (textopen > 0) break;
                    bracesopen++; break;
                //----------------------------------------------------------
                case 125:
                    if (textopen > 0) break;
                    bracesopen--; break;
                //----------------------------------------------------------
                case 91:
                    if (textopen > 0) break;
                    bracketopen++; break;
                //----------------------------------------------------------
                case 93:
                    if (textopen > 0) break;
                    bracketopen--; break;
                //----------------------------------------------------------
                case 44: {
                    if (textopen > 0) break;
                    if (childrenopen > 0) break;
                    String text = jsonstr.substring(indfrom, index);
                    textParts = addTextPart(textParts, text);
                    indfrom = index + 1;
                    break;
                }
                //----------------------------------------------------------
            }
            //==============================================================
            index++;
            //==============================================================
        }
        //******************************************************************
        String text = jsonstr.substring(indfrom, index);
        textParts = addTextPart(textParts, text);
        //******************************************************************
        return textParts;
    }
    //======================================================================
    private static String[] addTextPart (String[] parts, String text) {
        int count = parts.length;
        String[] newarray = new String[count + 1];
        System.arraycopy(parts, 0, newarray, 0, count);
        newarray[count] = text;
        return newarray;
    }
    //**********************************************************************
}
//==========================================================================
enum TextQualifier {
    NONE,
    SINGLE,
    DOUBLE
}
//==========================================================================
class InternalValue {
    JsonValueType type = JsonValueType.NONE;
    //StringBuilder value = new StringBuilder();
    String txtval = null;
}
//==========================================================================