package mars.javascript;
//***********************************************************************
public class JSArrayGenerator {
    //*******************************************************************
    private int valuescount = 0;
    private String[] values = new String[0];
    boolean quotes = false;
    //-------------------------------------------------------------------
    public void quotesOn () { quotes = true; }
    //*******************************************************************
    public void addValue (String value) { this.addToArray(value); };
    public void addValue (Integer value) { this.addToArray(value.toString()); }
    public void addValue (Long value) { this.addToArray(value.toString()); }
    public void addValue (Float value) { this.addToArray(value.toString()); }
    public void addValue (Double value) { this.addToArray(value.toString()); }
    //*******************************************************************
    @Override
    public String toString () {
        StringBuilder array = new StringBuilder("[");
        boolean first = true;
        String value;
        for (String val : values) {
            value = val.replace("'", "\\'");
            if (!first) array.append(",");
            if (quotes) array.append("'").append(value).append("'");
            else array.append(value);
            first = false;
        }
        array.append("]");
        return array.toString();
    }
    //*******************************************************************
    private void addToArray (String value) {
        String[] newvalues = new String[valuescount + 1];
        System.arraycopy(values, 0, newvalues, 0, valuescount);
        newvalues[valuescount] = value;
        values = newvalues;
        valuescount++;
    }
    //*******************************************************************
}
//***********************************************************************

