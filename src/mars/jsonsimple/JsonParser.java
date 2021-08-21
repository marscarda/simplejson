/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mars.jsonsimple;
//==========================================================================
import java.util.ArrayList;
//==========================================================================
/**
 * This class parses a JSON text and builds a JSON object.
 * @author Mariano
 */
public class JsonParser 
{
    //======================================================================
    /**
     * Parses a json text and returns a json object
     * @param jsontxt The text to parse.
     * @return A json object
     * @throws Exception 
     */
    public static JsonObject parseAndCreateJsonObject (String jsontxt) throws JsonParseException
    {
        //==================================================================
        String str = getTxtBetweenBraces(jsontxt);
        return createObject (str);
        //==================================================================
    }
    //======================================================================
    /**
     * Crea un objeto Json a partir del texto de este.
     * @param jsontxt
     * @return Un objeto json
     * @throws JsonParseException
     */
    private static JsonObject createObject (String jsontxt) throws JsonParseException
    {
        String[] strPairs = splitPairs(jsontxt);
        JsonObject obj = new JsonObject();
        for (String pair : strPairs)
        {
            obj.addPair(createPair(pair));
        }
        return obj;
    }
    //======================================================================
    /**
     * Crea un objeto par json a partir del texto de este.
     * @param strpair
     * @return Un Objeto JsonPair
     * @throws Exception 
     */
    private static JsonPair createPair (String strpair) throws JsonParseException {
        StringBuilder str = new StringBuilder(strpair);
        StringBuilder name = new StringBuilder();
        String value;
        int index = 0;
        int current;
        boolean accepted;
        int len = str.length();
        TxtQualifier txtq = TxtQualifier.NONE;
        //=================================================================
        //En esta etapa se busca el nombre del par.
        while (index < len) {
            //=============================================================
            current = str.codePointAt(index); // Es : (Separa el nombre del valor).
            if (current == 58) {
                break;
            }
            //=============================================================
            switch (txtq) {
                //=========================================================
                case NONE: {
                    //-----------------------------------------------------
                    if (current == 39)
                    { txtq = TxtQualifier.SINGLE; break; }
                    //-----------------------------------------------------
                    if (current == 34)
                    { txtq = TxtQualifier.DOUBLE; break; }
                    //-----------------------------------------------------
                    accepted = false;
                    if (current == 9) accepted = true;
                    if (current == 10) accepted = true;
                    if (current == 13) accepted = true;
                    if (current == 32) accepted = true;
                    if (!accepted)
                        throw new JsonParseException("Failed to parse a JSON pair (1)", strpair);
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
                case SINGLE: {
                    //-----------------------------------------------------
                    if (current == 39)
                    { txtq = TxtQualifier.NONE; break; }
                    //-----------------------------------------------------
                    //Chequear aceptacion de caracteres.
                    //-----------------------------------------------------
                    name.appendCodePoint(current);
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
                case DOUBLE: {
                    //-----------------------------------------------------
                    if (current == 34)
                    { txtq = TxtQualifier.NONE; break; }
                    //-----------------------------------------------------
                    //Chequear aceptacion de caracteres.
                    //-----------------------------------------------------
                    name.appendCodePoint(current);
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
            }
            //-------------------------------------------------------------
            index++;
            //=============================================================
        }
        //=================================================================
        if (index == len) throw new JsonParseException("Failed to parse a JSON pair (2)", strpair);
        if (name.length() == 0) throw new JsonParseException("Failed to parse a JSON pair (3)", strpair);
        //=================================================================
        index++;
        if (index < len)
            value = str.substring(index, len);
        else
            throw new JsonParseException("Failed to parse a JSON pair (4)", strpair);
        //=================================================================
        JsonPair pair = new JsonPair(name.toString());
        
        
        
        
        
        
        InternalValue ivalue = createValue(value);
        
        System.out.println("---------------");
        System.out.println(ivalue.value.toString());
        System.out.println("---------------");
        
        
        
        
        //-----------------------------------------------------------------
        switch (ivalue.type) {
            //--------------------------------------------
            case INTEGER:
                pair.setValue(Long.valueOf(ivalue.value.toString()));
                break;
            //--------------------------------------------
            case FLOAT:
                pair.setValue(Double.valueOf(ivalue.value.toString()));
                break;
            //--------------------------------------------
            case STRING: {
                pair.setEncodedValue(ivalue.value.toString());
                break;
            }
            //--------------------------------------------
            case BOOLEAN:
                pair.setValue(Boolean.valueOf(ivalue.value.toString()));
                break;
            //--------------------------------------------
            case JSONOBJECT: {
                JsonObject nwobj = createObject(ivalue.value.toString());
                pair.setValue(nwobj);
                break;
            }
            //--------------------------------------------
            case JSONARRAY: {
                ArrayList<JsonObject> objsar = new ArrayList<>();
                String[] objectstxt = splitPairs(ivalue.value.toString());
                for (String jsontxt : objectstxt) {
                    String objtxt = getTxtBetweenBraces(jsontxt);
                    JsonObject nwobj = createObject(objtxt);
                    objsar.add(nwobj);
                }
                pair.setValue((JsonObject[])objsar.toArray(new JsonObject[0]));
                break;
            }
            //--------------------------------------------
        }
        //=================================================================
        return pair;
        //=================================================================
    }
    //**********************************************************************
    /**
     * Analiza un valor para determinar que es.
     * @param value
     * @return
     * @throws Exception 
     */
    @Deprecated
    private static InternalValue createValue (String value) throws JsonParseException
    {
        //=======================================================
        //String str = value;
        int index = 0;
        int len = value.length();
        int current;
        boolean accepted;
        boolean keepadding = true;
        boolean escape = false;
        InternalValue val = new InternalValue();
        //=======================================================
        while (index < len)
        {
            current = value.codePointAt(index);
            //===================================================
            //Se busca un valor null
            if (current == 110 || current == 78)//Se encontro una 'n' 0 'N', Podria ser null.
            {
                int toend = len - index;
                if (toend > 3)
                {
                    index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 117 && current != 85)//Tiene que ser 'u' o 'U'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    //val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 108 && current != 76)//Tiene que ser 'l' o 'L'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 108 && current != 76)//Tiene que ser 'l' o 'L'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    index++;
                    //-------------------------------------------
                    keepadding = false;
                    break;
                    //-------------------------------------------
                }
            }
            //===================================================
            //I we fond an F or f we search a value of false
            if (current == 116 || current == 84)
            {
                int toend = len - index;
                if (toend > 3)
                {
                    val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 114 && current != 82)//Tiene que ser 'r' o 'R'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    
                    
                    
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 117 && current != 85)//Tiene que ser 'u' o 'U'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 101 && current != 69)//Tiene que ser 'e' o 'E'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    val.type = JsonValueType.BOOLEAN;
                    keepadding = false;
                    break;
                    //-------------------------------------------
                }
            }
            //===================================================
            //We found an F or f. We go for a 'false'.
            if (current == 102 || current == 70) {
                int toend = len - index;
                if (toend > 4) {
                    val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 97 && current != 65)//Tiene que ser 'a' o 'A'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 108 && current != 76)//Tiene que ser 'l' o 'L'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 115 && current != 83)//Tiene que ser 's' o 'S'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = value.codePointAt(index);
                    if (current != 101 && current != 69)//Tiene que ser 'e' o 'E'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    val.type = JsonValueType.BOOLEAN;
                    keepadding = false;
                    break;
                }
            }
            //===================================================
            //Un numero o signo menos. se asume como entero
            if ((current > 47 && current < 58) || current == 45) {
                val.value.appendCodePoint(current);
                val.type = JsonValueType.INTEGER;
                index++;
                break;
            }
            //===================================================
            //Una comilla simple. Se asume con texto.
            if (current == 39) {
                //val.value.appendCodePoint(current);
                val.type = JsonValueType.STRING;
                val.qual = TxtQualifier.SINGLE;
                index++;
                break;
            }
            //===================================================
            //Una comilla doble. Se asume como texto.
            if (current == 34) {
                //val.value.appendCodePoint(current);
                val.type = JsonValueType.STRING;
                val.qual = TxtQualifier.DOUBLE;
                index++;
                break;
            }
            //===================================================
            //Una llave abierta. Se asume como objeto.
            if (current == 123) {
                val.type = JsonValueType.JSONOBJECT;
                val.value.append(JsonParser.getTxtBetweenBraces(value));
                return val;
            }
            //===================================================
            if (current == 91) {
                val.type = JsonValueType.JSONARRAY;
                val.value.append(JsonParser.getTxtBetweenBrackets(value));
                return val;
            }
            //===================================================
            
            
            //===================================================
            //Ninguna de las anteriores. Solo se aceptan espacios, etc.
            accepted = false;
            if (current == 9) accepted = true;
            if (current == 10) accepted = true;
            if (current == 13) accepted = true;
            if (current == 32) accepted = true;
            if (!accepted)
                throw new JsonParseException("Failed to parse JSON value", value);
            index++;
            //===================================================
        }
        //=======================================================
        //Hasta aqui. Es null, numero o texto.
        while (index < len && keepadding)
        {
            current = value.codePointAt(index);
            //===================================================
            
            
            
            
            switch (val.type)
            {
                //===============================================
                
                case BOOLEAN:
                    keepadding = false;
                    
                
                //===============================================
                case INTEGER:
                    if (current == 46) {
                        val.value.appendCodePoint(current);
                        val.type = JsonValueType.FLOAT;
                        index++;
                        break;
                    }
                    else if (current > 47 && current < 58) {
                        val.value.appendCodePoint(current);
                        index++;
                        break;
                    }
                    //-------------------------------------------
                    keepadding = false;
                    break;
                //===============================================
                case FLOAT:
                    if (current > 47 && current < 58)//Es un numero.
                    {
                        val.value.appendCodePoint(current);
                        index++;
                        break;
                    }
                    //-------------------------------------------
                    keepadding = false;
                    break;
                //===============================================
                case STRING:
                {
                    //-------------------------------------------
                    if (current == 92 && !escape) escape = true;
                    else
                    {   
                        if (!escape)
                        {    
                            
                            if (val.qual == TxtQualifier.SINGLE)
                            {
                                if (current == 39)
                                {
                                    val.qual = TxtQualifier.NONE;
                                    index++;
                                    keepadding = false;
                                    break;
                                }
                            }
                            if (val.qual == TxtQualifier.DOUBLE)
                            {
                                if (current == 34)
                                {
                                    val.qual = TxtQualifier.NONE;
                                    index++;
                                    keepadding = false;
                                    break;
                                }
                            }
                        }
                        else escape = false;
                        val.value.appendCodePoint(current);
                    }
                    index++;
                    break;
                }
                //===============================================
            }
            //===================================================
        }
        //=======================================================
        //We are at the final. We check for sintax. Nothing open
        //is allowed. Braces, Quotes, Brackets.
        if (val.qual != TxtQualifier.NONE)
            throw new JsonParseException("Failed to parse JSON value", value);
        //-------------------------------------------------------
        //No se aceptan mas caracteres imprimibles salvo espacio
        //y tabs o saltos de linea.
        while (index < len)
        {
            current = value.codePointAt(index);
            accepted = false;
            if (current == 9) accepted = true;
            if (current == 10) accepted = true;
            if (current == 13) accepted = true;
            if (current == 32) accepted = true;
            if (!accepted)
                throw new JsonParseException("Failed to parse JSON value", value);
            index++;
        }
        //=======================================================
        return val;
    }
    //**********************************************************************
    public static InternalValue ceateValue2 (String rawtext) throws JsonParseException {
        String text = netText(rawtext);
        InternalValue value = new InternalValue();
        //=============================================
        if (testInteger(text)) {
            //System.out.println("Is an integer");
            //value.txtval = text;
            value.type = JsonValueType.INTEGER;
            //return value;
        }
        //=============================================
        if (testFloat(text)) {
            //System.out.println("Is a float");
            //value.txtval = text;
            value.type = JsonValueType.FLOAT;
            //return value;
        }
        //=============================================
        if (testString(text)) {
            //System.out.println("Is a string");
            //value.txtval = text;
            value.type = JsonValueType.STRING;
            return value;
        }
        //=============================================
        if (testNull(text)) {
            //System.out.println("Is null");
            value.type = JsonValueType.NULL;
        }
        //=============================================
        if (testBoolean(text)) {
            //System.out.println("Is a boolean");
            value.type = JsonValueType.BOOLEAN;
        }
        //=============================================
        if (testJsonObj(text)) {
            //System.out.println("Is an Object");
            value.type = JsonValueType.JSONOBJECT;
        }
        //=============================================
        if (testJsonObjArray(text)) {
            //System.out.println("Is an Array");
            value.type = JsonValueType.JSONARRAY;
        }
        //=============================================
        switch (value.type) {
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
            case NULL:
                value.txtval = text;
                return value;
            case STRING:
            case JSONOBJECT:
            case JSONARRAY: {
                value.txtval = text.substring(1, text.length() -2);
                return value;
            }
        }
        //============================================
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
        if (!negative && fptat == 0) return false;
        if (negative && fptat == 1) return false;
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
     * Extrae el contenido del texto entre dos llaves
     * Excluyendo a estas
     * @param jsontxt
     * @return
     * @throws Exception 
     */
    private static String getTxtBetweenBraces (String jsontxt) throws JsonParseException
    {
        //==================================================================
        StringBuilder str = new StringBuilder(jsontxt);
        StringBuilder ret = new StringBuilder();
        boolean adding = false;
        boolean accepted;
        int index = 0;
        int current;
        int opencount = 0;
        int len = jsontxt.length();
        TxtQualifier qual = TxtQualifier.NONE;
        boolean escape = false;
        //==================================================================
        while (index < len)
        {
            current = str.codePointAt(index);
            //==============================================================
            //se encuentra una llave abierta.
            if (current == 123 && qual == TxtQualifier.NONE)
            {
                if (adding) opencount++;
                else
                {
                    adding = true;
                    index++;
                    continue;
                }
            }
            //==============================================================
            //Se encuentra una llave cerrada
            if (current == 125 && qual == TxtQualifier.NONE) // es }
            {
                if (adding)
                {
                    if (opencount > 0) opencount--;
                    else
                    {
                        index++;
                        adding = false;
                        continue;
                    }
                }
            }
            //==============================================================
            if (index < len)
            {
                //==========================================================
                if (!escape)
                {
                    switch (qual)
                    {
                        case NONE:
                            if (current == 34) qual = TxtQualifier.DOUBLE;
                            if (current == 39) qual = TxtQualifier.SINGLE;
                            break;
                        case SINGLE:
                            if (current == 39) qual = TxtQualifier.NONE;
                            break;
                        case DOUBLE:
                            if (current == 34) qual = TxtQualifier.NONE;
                            break;
                    }
                }
                else
                    escape = false;
                //==========================================================
                //current = str.codePointAt(index);
                if (adding)
                {
                    ret.appendCodePoint(current);
                    if (current == 92)
                        escape = true;
                }
                else
                {
                    accepted = false;
                    if (current == 9) accepted = true;
                    if (current == 10) accepted = true;
                    if (current == 13) accepted = true;
                    if (current == 32) accepted = true;
                    if (!accepted)
                        throw new JsonParseException("Failed to parse a JSON object", jsontxt);
                }
                //=========================================================
                index++;
            }
            //==============================================================
        }
        //==================================================================
        if (adding) throw new JsonParseException("Failed to parse a JSON object", jsontxt);
        return ret.toString();
        //==================================================================
    }
    //======================================================================
    /**
     * Extrae el contenido del texto entre dos corchetes
     * Excluyendo a estos.
     * @param jsontxt
     * @return
     * @throws Exception 
     */
    private static String getTxtBetweenBrackets (String jsontxt) throws JsonParseException
    {
        //==================================================================
        StringBuilder str = new StringBuilder(jsontxt);
        StringBuilder ret = new StringBuilder();
        boolean adding = false;
        int index = 0;
        int current;
        int opencount = 0;
        int len = jsontxt.length();
        TxtQualifier qual = TxtQualifier.NONE;
        boolean escape = false;
        //==================================================================
        while (index < len)
        {
            current = str.codePointAt(index);
            //==============================================================
            //se encuentra un corchete abriendo.
            if (current == 91 && qual == TxtQualifier.NONE) // es [
            {
                if (adding) opencount++;
                else
                {
                    adding = true;
                    index++;
                    continue;
                }
            }
            //==============================================================
            //se encuentra un corchete cerrando.
            if (current == 93 && qual == TxtQualifier.NONE) // es ]
            {
                if (adding)
                {
                    if (opencount > 0) opencount--;
                    else
                    {
                        index++;
                        adding = false;
                        continue;
                    }
                }
            }
            //==============================================================
            if (index < len)
            {
                //==========================================================
                if (!escape)
                {
                    switch (qual)
                    {
                        case NONE:
                            if (current == 34) qual = TxtQualifier.DOUBLE;
                            if (current == 39) qual = TxtQualifier.SINGLE;
                            break;
                        case SINGLE:
                            if (current == 39) qual = TxtQualifier.NONE;
                            break;
                        case DOUBLE:
                            if (current == 34) qual = TxtQualifier.NONE;
                            break;
                    }
                }
                else
                    escape = false;
                //==========================================================
                //current = str.codePointAt(index);
                if (adding)
                {
                    ret.appendCodePoint(current);
                    if (current == 92)
                        escape = true;
                }
                else
                {
                    boolean accepted = false;
                    if (current == 9) accepted = true;
                    if (current == 10) accepted = true;
                    if (current == 13) accepted = true;
                    if (current == 32) accepted = true;
                    if (!accepted)
                        throw new JsonParseException("Failed to parse a JSON array", jsontxt);
                }
                //=========================================================
                index++;
            }
            //==============================================================
        }
        //==================================================================
        if (adding) throw new JsonParseException("Failed to parse a JSON array", jsontxt);
        return ret.toString();
        //==================================================================
    }
    //======================================================================
    /**
     * Separa en pares de valores. Dentro de un mismo objeto json.
     * @param toSplit
     * @return Un array de strings para cada par.
     */
    static String[] splitPairs (String toSplit)
    {
        //=================================================================
        ArrayList pairs = new ArrayList<>();
        StringBuilder str = new StringBuilder(toSplit);
        int len = str.length();
        int index = 0;
        int current;
        boolean escape = false;
        ArrayList<Integer> arrindxs = new ArrayList<>();
        TxtQualifier txtq = TxtQualifier.NONE;
        //=================================================================
        while (index < len)
        {
            //=============================================================
            current = str.codePointAt(index);
            //=============================================================
            switch (txtq)
            {
                //=========================================================
                case NONE:
                {
                    //-----------------------------------------------------
                    if (current == 44)
                    {
                        arrindxs.add(index);
                        break;
                    }
                    //-----------------------------------------------------
                    if (current == 39)
                    { txtq = TxtQualifier.SINGLE; break; }
                    //-----------------------------------------------------
                    if (current == 34)
                    { txtq = TxtQualifier.DOUBLE; break; }
                    //-----------------------------------------------------
                    if (current == 123)
                    { txtq = TxtQualifier.BRACES; break; }
                    //-----------------------------------------------------
                    if (current == 91)
                    { txtq = TxtQualifier.BRACKETS; break; }
                   
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
                case SINGLE:
                {
                    //-----------------------------------------------------
                    if (escape)
                    { escape = false; break; }
                    //-----------------------------------------------------
                    if (current == 92)
                    { escape = true; break; }
                    //-----------------------------------------------------
                    if (current == 39)
                    { txtq = TxtQualifier.NONE; break; }
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
                case DOUBLE:
                {
                    //-----------------------------------------------------
                    if (escape)
                    { escape = false; break; }
                    //-----------------------------------------------------
                    if (current == 92)
                    { escape = true; break; }
                    //-----------------------------------------------------
                    if (current == 34)
                    { txtq = TxtQualifier.NONE; break; }
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
                case BRACES:
                {
                    //-----------------------------------------------------
                    if (escape)
                    { escape = false; break; }
                    //-----------------------------------------------------
                    if (current == 92)
                    { escape = true; break; }
                    //-----------------------------------------------------
                    if (current == 125)
                    { txtq = TxtQualifier.NONE; break; }
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
                case BRACKETS:
                {
                    //-----------------------------------------------------
                    if (escape)
                    { escape = false; break; }
                    //-----------------------------------------------------
                    if (current == 92)
                    { escape = true; break; }
                    //-----------------------------------------------------
                    if (current == 93)
                    { txtq = TxtQualifier.NONE; break; }
                    //-----------------------------------------------------
                    break;
                }
                //=========================================================
            }
            //=============================================================
            index++;
            //=============================================================
        }
        //=================================================================
        int beg = 0;
        String pair;
        for (Integer end : arrindxs)
        {
            if (beg != 0) beg += 2;
            pair = str.substring(beg, end);
            pairs.add(pair);
            beg = end - 1;
        }
        //-----------------------------------------------------------------
        if (beg != 0) beg += 2;
        pair = str.substring(beg, len);
        pairs.add(pair);
        //=================================================================
        return (String[])pairs.toArray(new String[0]);
        //=================================================================
    }
    //======================================================================
}
//==========================================================================
enum TxtQualifier
{
    NONE,
    SINGLE,
    DOUBLE,
    BRACES,
    BRACKETS
}
//==========================================================================
class InternalValue
{
    JsonValueType type = JsonValueType.NONE;
    StringBuilder value = new StringBuilder();
    String txtval = null;
    TxtQualifier qual = TxtQualifier.NONE;
}
//==========================================================================