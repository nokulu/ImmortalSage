package net.minecraft.util.parsing.packrat;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.mutable.MutableBoolean;

public interface Term<S> {
    boolean parse(ParseState<S> pParseState, Scope pScope, Control pControl);

    static <S> Term<S> named(Atom<?> pName) {
        return new Term.Reference<>(pName);
    }

    static <S, T> Term<S> marker(Atom<T> pName, T pValue) {
        return new Term.Marker<>(pName, pValue);
    }

    @SafeVarargs
    static <S> Term<S> sequence(Term<S>... pElements) {
        return new Term.Sequence<>(List.of(pElements));
    }

    @SafeVarargs
    static <S> Term<S> alternative(Term<S>... pElements) {
        return new Term.Alternative<>(List.of(pElements));
    }

    static <S> Term<S> optional(Term<S> pTerm) {
        return new Term.Maybe<>(pTerm);
    }

    static <S> Term<S> cut() {
        return new Term<S>() {
            @Override
            public boolean parse(ParseState<S> p_333527_, Scope p_336097_, Control p_335047_) {
                p_335047_.cut();
                return true;
            }

            @Override
            public String toString() {
                return "\u2191";
            }
        };
    }

    static <S> Term<S> empty() {
        return new Term<S>() {
            @Override
            public boolean parse(ParseState<S> p_328418_, Scope p_332040_, Control p_328784_) {
                return true;
            }

            @Override
            public String toString() {
                return "\u03b5";
            }
        };
    }

    public static record Alternative<S>(List<Term<S>> elements) implements Term<S> {
        @Override
        public boolean parse(ParseState<S> p_328094_, Scope p_331753_, Control p_334626_) {
            MutableBoolean mutableboolean = new MutableBoolean();
            Control control = mutableboolean::setTrue;
            int i = p_328094_.mark();

            for (Term<S> term : this.elements) {
                if (mutableboolean.isTrue()) {
                    break;
                }

                Scope scope = new Scope();
                if (term.parse(p_328094_, scope, control)) {
                    p_331753_.putAll(scope);
                    return true;
                }

                p_328094_.restore(i);
            }

            return false;
        }
    }

    public static record Marker<S, T>(Atom<T> name, T value) implements Term<S> {
        @Override
        public boolean parse(ParseState<S> p_332878_, Scope p_331621_, Control p_334053_) {
            p_331621_.put(this.name, this.value);
            return true;
        }
    }

    public static record Maybe<S>(Term<S> term) implements Term<S> {
        @Override
        public boolean parse(ParseState<S> p_332001_, Scope p_329861_, Control p_331352_) {
            int i = p_332001_.mark();
            if (!this.term.parse(p_332001_, p_329861_, p_331352_)) {
                p_332001_.restore(i);
            }

            return true;
        }
    }

    public static record Reference<S, T>(Atom<T> name) implements Term<S> {
        @Override
        public boolean parse(ParseState<S> p_332365_, Scope p_333205_, Control p_334292_) {
            Optional<T> optional = p_332365_.parse(this.name);
            if (optional.isEmpty()) {
                return false;
            } else {
                p_333205_.put(this.name, optional.get());
                return true;
            }
        }
    }

    public static record Sequence<S>(List<Term<S>> elements) implements Term<S> {
        @Override
        public boolean parse(ParseState<S> p_330195_, Scope p_336361_, Control p_328798_) {
            int i = p_330195_.mark();

            for (Term<S> term : this.elements) {
                if (!term.parse(p_330195_, p_336361_, p_328798_)) {
                    p_330195_.restore(i);
                    return false;
                }
            }

            return true;
        }
    }
}