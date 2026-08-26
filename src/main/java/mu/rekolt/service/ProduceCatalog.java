package mu.rekolt.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import mu.rekolt.model.CashCropProduce;
import mu.rekolt.model.CerealProduce;
import mu.rekolt.model.PerishableProduce;
import mu.rekolt.model.Produce;

public final class ProduceCatalog {

    private ProduceCatalog() { }

//  The price list as an array. Rule 1 of section 2, and the only place the
//   four prices appear
    private static final Produce[] CodeList = {
            new CerealProduce    ("MZE", "Maize",          30.0),
            new CerealProduce    ("BNS", "Beans",          90.0),
            new PerishableProduce("POT", "Potatoes",       45.0),
            new CashCropProduce  ("TEA", "Green tea leaf", 25.0)
    };

    public static List<Produce> all() {
        return Collections.unmodifiableList(Arrays.asList(CodeList));
    }

//  The four codes in catalog order, used as the headings of the weekly grid
    public static String[] codes() {
        String[] codes = new String[CodeList.length];
        for (int i = 0; i < CodeList.length; i++) {
            codes[i] = CodeList[i].getCode();
        }
        return codes;   // a fresh array each time, so a caller cannot reorder the real one
    }

    public static Optional<Produce> forCode(String rawCode) {
        if (rawCode == null) {
            return Optional.empty();
        }
        String code = rawCode.trim().toUpperCase();
        for (Produce produce : CodeList) {
            if (produce.getCode().equals(code)) {
                return Optional.of(produce);
            }
        }
        return Optional.empty();
    }

    public static boolean isKnownCode(String rawCode) {
        return forCode(rawCode).isPresent();
    }
}