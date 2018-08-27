package asunder.toche.sccmanagement.preference;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by ToCHe on 12/8/2018 AD.
 */
public class Contextor {

    private static Contextor contextor;
    private Context context;

    private Contextor() {
    }

    public static Contextor getInstance() {
        if (contextor == null) {
            contextor = new Contextor();
        }
        return contextor;
    }

    public void init(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public Resources getResources() {
        return getContext().getResources();
    }

}