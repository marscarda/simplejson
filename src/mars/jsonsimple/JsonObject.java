package mars.jsonsimple;
//=======================================================
import java.util.ArrayList;
//=======================================================
public class JsonObject {
    //===================================================
    ArrayList<JsonPair> pairs = new ArrayList<>();
    //===================================================
    public void addPair(JsonPair pair) {
        pairs.add(pair);
    }
    //===================================================
    String getTextCompact () {
        StringBuilder str = new StringBuilder();
        //-----------------------------------------------
        str.append("{");
        //-----------------------------------------------
        boolean first = true;
        for (JsonPair pair : pairs) {
            //-------------------------------------------
            if (!first) str.append(",");
            else first = false;
            //-------------------------------------------
            str.append(pair.getTextCompact());
            //-------------------------------------------
        }
        //-----------------------------------------------
        str.append("}");
        return str.toString();
        //-----------------------------------------------
    }
    //===================================================
    String getText () {
        StringBuilder str = new StringBuilder();
        //-----------------------------------------------
        str.append("{");
        //-----------------------------------------------
        boolean first = true;
        for (JsonPair pair : pairs) {
            //-------------------------------------------
            if (!first) str.append(",\n");
            else first = false;
            //-------------------------------------------
            str.append(pair.getText());
            //-------------------------------------------
        }
        //-----------------------------------------------
        str.append("}");
        return str.toString();
        //-----------------------------------------------
    }
    //===================================================
    /**
     * Checks the existence of a pair.
     * @param name
     * @return 
     */
    public boolean checkPair (String name) {
        String n = name.toLowerCase();
        for (JsonPair pair : pairs)
            if (n.equals(pair.nme.toLowerCase())) return true;
        return false;
    }
    //===================================================
    /**
     * Returns a pair.
     * @param name
     * @return
     */
    public JsonPair getPair (String name) {
        String n = name.toLowerCase();
        for (JsonPair pair : pairs)
            if (n.equals(pair.nme.toLowerCase())) return pair;
        return new JsonPair("na", "n/a");
    }
    //===================================================
    @Override
    public String toString() {
        return getText();
    }
    //===================================================
}
//=======================================================