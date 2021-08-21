package mars.jsonsimple;
//===============================================================
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
//===============================================================
/**
 * Json Pair. Represents a json pair for example "name" : "value".
 * This object is only useful if contained in a JsonObject.
 * See also JsonObject
 * @author Mariano
 */
public class JsonPair {
    //***************************************************************
    public JsonPair () {}
    //-----------------------------------------------------------
    public JsonPair (String name) {
        nme = name;
        type = JsonValueType.NULL;
    }
    //-----------------------------------------------------------
    /**
     * Sets the name and a string value
     * @param name
     * @param value 
     */
    public JsonPair (String name, String value) {
        nme = name;
        type = JsonValueType.STRING;
        String v;
        try {
            v = URLEncoder.encode(value, "UTF-8"); 
            val = v.replaceAll("\\+", " ");
            setok = true;
        }
        catch (UnsupportedEncodingException e) {
            val = value;
            setok = false; 
            seterr = e.getMessage();
        }
    }
    //-----------------------------------------------------------
    public JsonPair (String name, int value) {
        nme = name;
        val = value;
        type = JsonValueType.INTEGER;
    }
    //-----------------------------------------------------------
    /**
     * Sets the name and a long value
     * @param name
     * @param value 
     */
    public JsonPair (String name, Long value) {
        nme = name;
        val = value;
        type = JsonValueType.INTEGER;
    }
    //-----------------------------------------------------------
    public JsonPair (String name, Float value) {
        nme = name;
        val = value;
        type = JsonValueType.FLOAT;
    }
    //-----------------------------------------------------------    
    /**
     * Sets the name and a Double value
     * @param name
     * @param value 
     */
    public JsonPair (String name, Double value) {
        nme = name;
        val = value;
        type = JsonValueType.FLOAT;
    }
    //-----------------------------------------------------------
    /**
     * Sets the name and a boolean value
     * @param name
     * @param value 
     */
    public JsonPair (String name, Boolean value) {
        nme = name;
        val = value;
        type = JsonValueType.BOOLEAN;
    }
    //-----------------------------------------------------------
    /**
     * Sets the name and a Json Object value
     * @param name
     * @param value 
     */
    public JsonPair (String name, JsonObject value) {
        nme = name;
        val = value;
        type = JsonValueType.JSONOBJECT;
    }
    //-----------------------------------------------------------
    /**
     * Sets the name and a JsonObjects array
     * @param name
     * @param value 
     */
    public JsonPair (String name, JsonObject[] value) {
        nme = name;
        val = value;
        type = JsonValueType.JSONARRAY;
    }
    //***************************************************************
    JsonValueType type = JsonValueType.NULL;
    String nme = "name";
    Object val = null;
    boolean setok = false;
    String seterr = null;
    boolean valdidfail = false;
    //***************************************************************
    public boolean fetchValDidFail () { return valdidfail; }
    //===========================================================
    //public boolean isSetOk () { return setok; }
    public String getSetError () {
        if (seterr == null) return "";
        return seterr;
    }
    //===========================================================
    public void setName (String name){nme = name;}
    //-----------------------------------------------------------
    public void setValue (String value) throws JsonValueTypeException {
        type = JsonValueType.STRING;
        String v;
        try {
            v = URLEncoder.encode(value, "UTF-8"); 
            val = v.replaceAll("\\+", "%20");
            setok = true;
        }
        catch (UnsupportedEncodingException e) {
            val = value;
            setok = false; 
            seterr = e.getMessage();
        }
    }
    //-----------------------------------------------------------
    void setEncodedValue (String value) {
        type = JsonValueType.STRING;
        val = value;
    }
    //-----------------------------------------------------------
    public void setValue (Long value) {
        type = JsonValueType.INTEGER;
        val = value;
    }
    //-----------------------------------------------------------
    public void setValue (Double value) {
        type = JsonValueType.FLOAT;
        val = value;
    }
    //-----------------------------------------------------------
    public void setValue (Boolean value) {
        type = JsonValueType.BOOLEAN;
        val = value;
    }
    //-----------------------------------------------------------
    public void setValue (JsonObject value) {
        type = JsonValueType.JSONOBJECT;
        val = value;
    }
    //-----------------------------------------------------------
    public void setValue (JsonObject[] value) {
        type = JsonValueType.JSONARRAY;
        val = value;
    }
    //===========================================================
    public String getName(){return nme;}
    public JsonValueType getValueType (){ return type; }
    //===========================================================
    public String getStringValue() throws JsonValueTypeException {
        if (type == JsonValueType.JSONOBJECT || type == JsonValueType.JSONARRAY) return "";
        try {
            String urlEncoded = String.valueOf(val);
            return URLDecoder.decode(urlEncoded, "UTF-8");
        }
        catch (Exception e) { throw new JsonValueTypeException("Filed to decode URLEncode text"); }
    }
    //-----------------------------------------------------------
    public String getRawStringValue() {
        return String.valueOf(val);
    }
    //-----------------------------------------------------------
    public Long getLongValue() {
        if (type == JsonValueType.INTEGER) return (Long)val;
        return (long)0;
    }
    //-----------------------------------------------------------
    public Double getDoubleValue() {
        if (type == JsonValueType.INTEGER) return Double.valueOf(String.valueOf(val));
        if (type == JsonValueType.FLOAT) return (Double)val;
        return (double)0;
    }
    //-----------------------------------------------------------
    public Boolean getBooleanValue() {
        
        String a = this.getRawStringValue();
        
        System.out.println("QQQ " + a);
        
        return Boolean.parseBoolean(a);
        
        /*
        System.out.println(JsonValueType.BOOLEAN + " " + type);
        
        System.out.println("Object bool " + (String)val);
        
        
        if (type == JsonValueType.BOOLEAN) return Boolean.parseBoolean((String)val);
        return false;
        */
    }
    //-----------------------------------------------------------
    public int getIntValue ()  {
        if (type == JsonValueType.INTEGER) {
            String s = String.valueOf(val);
            try { return Integer.valueOf(s); }
            catch (Exception e) { return 0; }
        }
        return 0;
    }
    //-----------------------------------------------------------
    public JsonObject getChildJsonObject() {
        if (type == JsonValueType.JSONOBJECT) return (JsonObject)val;
        return new JsonObject();
    }
    //===========================================================
    /**
     * Returns the jsonobjects array
     * @return 
     */
    public JsonObject[] getChildrenArray () {
        if (type == JsonValueType.JSONARRAY) return (JsonObject[])val;
        if (type == JsonValueType.JSONOBJECT) {
            JsonObject[] ar = new JsonObject[1];
            ar[0] = (JsonObject)val;
            return ar;
        }
        return new JsonObject[0];
    }
    //===========================================================
    public String getTextCompact () {
        //-------------------------------------------------
        StringBuilder str = new StringBuilder();
        str.append("\"");
        str.append(nme);
        str.append("\":");
        //-------------------------------------------------
        switch (type)
        {
            //---------------------------------------------
            case NULL:
                str.append("null");
                break;
            //---------------------------------------------
            case STRING:
                str.append("\"");
                str.append(val);
                str.append("\"");
                break;
            //---------------------------------------------------
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
                str.append(val);
                break;
            //---------------------------------------------------
            case JSONOBJECT: {
                JsonObject obj = (JsonObject)val;
                str.append(obj.getTextCompact());
                break;
            }
            //---------------------------------------------------
            case JSONARRAY: {
                JsonObject[] objs = (JsonObject[])val;
                str.append("[");
                boolean first = true;
                for (JsonObject obj : objs)
                {
                    if (!first) str.append(",");
                    else first = false;
                    str.append(obj.getTextCompact());
                }
                str.append("]");
            }
            //---------------------------------------------------
        }
        return str.toString();
    }
    //===========================================================
    String getText () {
        //-------------------------------------------------
        StringBuilder str = new StringBuilder(); {
            str.append(" \"");
            str.append(nme);
            str.append("\" : ");
        }
        //-------------------------------------------------
        switch (type) {
            //---------------------------------------------
            case NULL:
                str.append("null ");
                break;
            //---------------------------------------------
            case STRING:
                str.append("\"");
                str.append(val);
                str.append("\" ");
                break;
            //---------------------------------------------------
            case INTEGER:
            case FLOAT:
            case BOOLEAN:
                str.append(val);
                str.append(" ");
                break;
            //---------------------------------------------------
            case JSONOBJECT: {
                JsonObject obj = (JsonObject)val;
                str.append(obj.getText());
                break;
            }
            //---------------------------------------------------
            case JSONARRAY: {
                JsonObject[] objs = (JsonObject[])val;
                str.append(" [\n");
                boolean first = true;
                for (JsonObject obj : objs)
                {
                    if (!first) str.append(",\n");
                    else first = false;
                    str.append(obj.getText());
                }
                str.append("\n] ");
            }
            //---------------------------------------------------
        }
        return str.toString();
    }
    //===========================================================
    public String replaceUTF8(String raw)
    {
        StringBuilder process = new StringBuilder(raw);
        for (int n = 0; n < process.length(); n++)
        {
            if (process.codePointAt(n) > 126)
            {
                switch (process.codePointAt(n))
                {
                    //--------------------------------------------------------
                    case 50052: process.setCharAt(n, (char)196); break;//Ä
                    case 50053: process.setCharAt(n, (char)197); break;//Å
                    case 50055: process.setCharAt(n, (char)199); break;//Ç
                    case 50057: process.setCharAt(n, (char)201); break;//É
                    case 50065: process.setCharAt(n, (char)209); break;//Ñ
                    case 50070: process.setCharAt(n, (char)214); break;//Ö
                    case 50076: process.setCharAt(n, (char)220); break;//Ü
                    case 50080: process.setCharAt(n, (char)224); break;//à
                    case 50081: process.setCharAt(n, (char)225); break;//á
                    case 50082: process.setCharAt(n, (char)226); break;//â
                    case 50084: process.setCharAt(n, (char)228); break;//ä
                    case 50083: process.setCharAt(n, (char)227); break;//ã
                    case 50085: process.setCharAt(n, (char)229); break;//å
                    case 50087: process.setCharAt(n, (char)231); break;//ç
                    case 50088: process.setCharAt(n, (char)232); break;//è
                    case 50089: process.setCharAt(n, (char)233); break;//é
                    case 50090: process.setCharAt(n, (char)234); break;//ê
                    case 50091: process.setCharAt(n, (char)235); break;//ë
                    case 50092: process.setCharAt(n, (char)236); break;//ì
                    case 50093: process.setCharAt(n, (char)237); break;//í
                    case 50094: process.setCharAt(n, (char)238); break;//î
                    case 50095: process.setCharAt(n, (char)239); break;//ï
                    case 50097: process.setCharAt(n, (char)241); break;//ñ
                    case 50099: process.setCharAt(n, (char)243); break;//ó
                    case 50098: process.setCharAt(n, (char)242); break;//ò
                    case 50100: process.setCharAt(n, (char)244); break;//ô
                    case 50102: process.setCharAt(n, (char)246); break;//ö
                    case 50101: process.setCharAt(n, (char)245); break;//õ
                    case 50105: process.setCharAt(n, (char)249); break;//ù
                    case 50106: process.setCharAt(n, (char)250); break;//ú
                    case 50107: process.setCharAt(n, (char)251); break;//û
                    case 50108: process.setCharAt(n, (char)252); break;//ü
                    //--------------------------------------------------------
                    case 49840: process.setCharAt(n, (char)176); break;//°
                    case 49826: process.setCharAt(n, (char)162); break;//¢
                    case 49827: process.setCharAt(n, (char)163); break;//£
                    case 49831: process.setCharAt(n, (char)167); break;//§
                    case 49846: process.setCharAt(n, (char)182); break;//¶
                    case 50079: process.setCharAt(n, (char)223); break;//ß
                    case 49838: process.setCharAt(n, (char)174); break;//®
                    case 49833: process.setCharAt(n, (char)169); break;//©
                    case 49844: process.setCharAt(n, (char)180); break;//´
                    case 49832: process.setCharAt(n, (char)168); break;//¨
                    case 50054: process.setCharAt(n, (char)198); break;//Æ
                    case 50072: process.setCharAt(n, (char)216); break;//Ø
                    case 49841: process.setCharAt(n, (char)177); break;//±
                    case 49829: process.setCharAt(n, (char)165); break;//¥
                    case 49845: process.setCharAt(n, (char)181); break;//µ
                    case 49834: process.setCharAt(n, (char)170); break;//ª
                    case 49850: process.setCharAt(n, (char)186); break;//º
                    case 50086: process.setCharAt(n, (char)230); break;//æ
                    case 50104: process.setCharAt(n, (char)248); break;//ø
                    case 49855: process.setCharAt(n, (char)191); break;//¿
                    case 49825: process.setCharAt(n, (char)161); break;//¡
                    case 49836: process.setCharAt(n, (char)172); break;//¬
                    case 49835: process.setCharAt(n, (char)171); break;//«
                    case 49851: process.setCharAt(n, (char)187); break;//»
                    case 49824: process.setCharAt(n, (char)160); break;//NO-BREAK SPACE
                    case 50048: process.setCharAt(n, (char)192); break;//À
                    case 50051: process.setCharAt(n, (char)195); break;//Ã
                    case 50069: process.setCharAt(n, (char)213); break;//Õ
                    case 50103: process.setCharAt(n, (char)247); break;//÷
                    case 50111: process.setCharAt(n, (char)255); break;//ÿ
                    case 49847: process.setCharAt(n, (char)183); break;//·
                    case 50050: process.setCharAt(n, (char)194); break;//Â
                    case 50058: process.setCharAt(n, (char)202); break;//Ê
                    case 50049: process.setCharAt(n, (char)193); break;//Á
                    case 50059: process.setCharAt(n, (char)203); break;//Ë
                    case 50056: process.setCharAt(n, (char)200); break;//È
                    case 50061: process.setCharAt(n, (char)205); break;//Í
                    case 50062: process.setCharAt(n, (char)206); break;//Î
                    case 50063: process.setCharAt(n, (char)207); break;//Ï
                    case 50060: process.setCharAt(n, (char)204); break;//Ì
                    case 50067: process.setCharAt(n, (char)211); break;//Ó
                    case 50068: process.setCharAt(n, (char)212); break;//Ô
                    case 50066: process.setCharAt(n, (char)210); break;//Ò
                    case 50074: process.setCharAt(n, (char)218); break;//Ú
                    case 50075: process.setCharAt(n, (char)219); break;//Û
                    case 50073: process.setCharAt(n, (char)217); break;//Ù
                    case 49848: process.setCharAt(n, (char)184); break;//¸
                    //--------------------------------------------------------
                }
            }
        }
        return process.toString();
    }
    //===========================================================
}
//===============================================================
