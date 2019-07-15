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
    private static JsonPair createPair (String strpair) throws JsonParseException
    {
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
        while (index < len)
        {
            //=============================================================
            current = str.codePointAt(index); // Es : (Separa el nombre del valor).
            if (current == 58)
            {
                break;
            }
            //=============================================================
            switch (txtq)
            {
                //=========================================================
                case NONE:
                {
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
                case SINGLE:
                {
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
                case DOUBLE:
                {
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
        //-----------------------------------------------------------------
        switch (ivalue.type)
        {
            //--------------------------------------------
            case INTEGER:
                pair.setValue(Long.valueOf(ivalue.value.toString()));
                break;
            //--------------------------------------------
            case FLOAT:
                pair.setValue(Double.valueOf(ivalue.value.toString()));
                break;
            //--------------------------------------------
            case STRING:
            {
                pair.setEncodedValue(ivalue.value.toString());
                break;
            }
            //--------------------------------------------
            case BOOLEAN:
                pair.setValue(Boolean.valueOf(ivalue.value.toString()));
                break;
            //--------------------------------------------
            case JSONOBJECT:
            {
                JsonObject nwobj = createObject(ivalue.value.toString());
                pair.setValue(nwobj);
                break;
            }
            //--------------------------------------------
            case JSONARRAY:
            {
                ArrayList<JsonObject> objsar = new ArrayList<>();
                String[] objectstxt = splitPairs(ivalue.value.toString());
                for (String jsontxt : objectstxt)
                {
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
    //======================================================================
    /**
     * Analiza un valor para determinar que es.
     * @param value
     * @return
     * @throws Exception 
     */
    private static InternalValue createValue (String value) throws JsonParseException
    {
        //=======================================================
        StringBuilder str = new StringBuilder(value);
        int index = 0;
        int len = str.length();
        int current;
        boolean accepted;
        boolean keepadding = true;
        boolean escape = false;
        InternalValue val = new InternalValue();
        //=======================================================
        while (index < len)
        {
            current = str.codePointAt(index);
            //===================================================
            //Se busca un valor null
            if (current == 110 || current == 78)//Se encontro una 'n' 0 'N', Podria ser null.
            {
                int toend = len - index;
                if (toend > 3)
                {
                    //val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 117 && current != 85)//Tiene que ser 'u' o 'U'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    //val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 108 && current != 76)//Tiene que ser 'l' o 'L'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    //val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 108 && current != 76)//Tiene que ser 'l' o 'L'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    //val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    keepadding = false;
                    break;
                }
            }
            //===================================================
            //Se busca un valor boolean true
            if (current == 116 || current == 84)//Se encontro una 't' 0 'T', Podria ser True.
            {
                int toend = len - index;
                if (toend > 3)
                {
                    val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 114 && current != 82)//Tiene que ser 'r' o 'R'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 117 && current != 85)//Tiene que ser 'u' o 'U'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
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
            //Se busca un valor boolean false
            if (current == 102 || current == 70)//Se encontro una 'f' 0 'F', Podria ser False.
            {
                int toend = len - index;
                if (toend > 4)
                {
                    val.value.appendCodePoint(current); 
                    index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 97 && current != 65)//Tiene que ser 'a' o 'A'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 108 && current != 76)//Tiene que ser 'l' o 'L'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
                    if (current != 115 && current != 83)//Tiene que ser 's' o 'S'
                        throw new JsonParseException("Failed to parse JSON value", value);
                    val.value.appendCodePoint(current); index++;
                    //-------------------------------------------
                    current = str.codePointAt(index);
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
            if ((current > 47 && current < 58) || current == 45)
            {
                val.value.appendCodePoint(current);
                val.type = JsonValueType.INTEGER;
                index++;
                break;
            }
            //===================================================
            //Una comilla simple. Se asume con texto.
            if (current == 39)
            {
                //val.value.appendCodePoint(current);
                val.type = JsonValueType.STRING;
                val.qual = TxtQualifier.SINGLE;
                index++;
                break;
            }
            //===================================================
            //Una comilla doble. Se asume como texto.
            if (current == 34)
            {
                //val.value.appendCodePoint(current);
                val.type = JsonValueType.STRING;
                val.qual = TxtQualifier.DOUBLE;
                index++;
                break;
            }
            //===================================================
            //Una llave abierta. Se asume como objeto.
            if (current == 123)
            {
                val.type = JsonValueType.JSONOBJECT;
                val.value.append(JsonParser.getTxtBetweenBraces(value));
                return val;
            }
            //===================================================
            if (current == 91)
            {
                val.type = JsonValueType.JSONARRAY;
                val.value.append(JsonParser.getTxtBetweenBrackets(value));
                return val;
            }
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
            current = str.codePointAt(index);
            //===================================================
            switch (val.type)
            {
                //===============================================
                case INTEGER:
                    if (current == 46)//Punto flotante.
                    {
                        val.value.appendCodePoint(current);
                        val.type = JsonValueType.FLOAT;
                        index++;
                        break;
                    }
                    else if (current > 47 && current < 58)//Es un numero.
                    {
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
        //Estamos en el final. Se chequea que la sintaxis esta bien.
        //No puede haber nada abierto (comillas, llaves, etc.)
        if (val.qual != TxtQualifier.NONE)
            throw new JsonParseException("Failed to parse JSON value", value);
        //-------------------------------------------------------
        //No se aceptan mas caracteres imprimibles salvo espacio
        //y tabs o saltos de linea.
        while (index < len)
        {
            current = str.codePointAt(index);
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
    //======================================================================
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
    JsonValueType type = JsonValueType.NULL;
    StringBuilder value = new StringBuilder();
    TxtQualifier qual = TxtQualifier.NONE;
}
//==========================================================================