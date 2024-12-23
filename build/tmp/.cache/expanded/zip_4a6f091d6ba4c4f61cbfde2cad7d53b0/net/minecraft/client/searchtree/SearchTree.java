package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SearchTree<T> {
    static <T> SearchTree<T> empty() {
        return p_344644_ -> List.of();
    }

    static <T> SearchTree<T> plainText(List<T> pContents, Function<T, Stream<String>> pFilter) {
        if (pContents.isEmpty()) {
            return empty();
        } else {
            SuffixArray<T> suffixarray = new SuffixArray<>();

            for (T t : pContents) {
                pFilter.apply(t).forEach(p_342612_ -> suffixarray.add(t, p_342612_.toLowerCase(Locale.ROOT)));
            }

            suffixarray.generate();
            return suffixarray::search;
        }
    }

    List<T> search(String pQuery);
}