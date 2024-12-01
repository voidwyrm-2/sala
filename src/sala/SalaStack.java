package sala;

import java.util.Stack;

public class SalaStack extends Stack<Object> {
    public boolean hasAmount(int amount) {
        return amount <= this.size();
    }

    @Override
    public String toString() {
        if (size() == 0) return "[]";
        var str = new StringBuilder();
        var a = toArray();
        for (int i = 0; i < a.length; i++)  {
            if (i == a.length - 1) {
                str.append(a[i]);
            } else {
                str.append(a[i].getClass() == String.class ? "\"" +  a[i].toString() + "\"" : a[i]).append(", ");
            }
        }
        return "[ " + str.toString() + " ]";
    }
}
