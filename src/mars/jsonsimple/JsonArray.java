package mars.jsonsimple;
//**********************************************************************
public class JsonArray {
    //==================================================================
    int count = 0;
    JsonObject[] array = new JsonObject[0];
    JsonObject jsonobject = null;
    //==================================================================
    public void addPair (JsonPair pair) {
        if (jsonobject == null) jsonobject = new JsonObject();
        jsonobject.addPair(pair);
    }
    //==================================================================
    public void addToArray () {
        if (jsonobject == null) jsonobject = new JsonObject();
        JsonObject[] newarray = new JsonObject[count + 1];
        System.arraycopy(array, 0, newarray, 0, count);
        newarray[count] = jsonobject;
        count++;
        array = newarray;
        jsonobject = null;
    }
    //==================================================================
    public int getCount () { return count; }
    public JsonObject[] getArray () {
        if (array == null) return new JsonObject[0];
        return array;
    }
    //==================================================================
}
//**********************************************************************