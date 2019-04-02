package com.foxmike.android.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 2019-04-01.
 */

public class SessionAdvertisements extends HashMap<String,Long> {



    public SessionAdvertisements(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public SessionAdvertisements(int initialCapacity) {
        super(initialCapacity);
    }

    public SessionAdvertisements() {
    }



    public SessionAdvertisements(Map<? extends String, ? extends Long> m) {
        super(m);
    }

}
