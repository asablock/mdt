package io.github.asablock.mdt;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class CommandUtil {
    private CommandUtil() {
    }

    public static <S, T extends ArgumentBuilder<S, T>> ChainedCommandBuilder<S, T> chainedCommandBuilder(T first) {
        return new ChainedCommandBuilder<>(first);
    }

    public static class ChainedCommandBuilder<S, T extends ArgumentBuilder<S, T>> {
        private final List<Arg<?>> args;
        private final T first;

        private ChainedCommandBuilder(T first) {
            this.args = new ArrayList<>();
            this.first = first;
        }

        public <A> ChainedCommandBuilder<S, T> append(String name, A defaultValue, ArgumentType<A> type) {
            args.add(new Arg<>(name, defaultValue, type));
            return this;
        }

        public T executes(BiFunction<CommandContext<S>, Object[], Integer> command) {
            executes0(first, args, command);
            return first;
        }

        private static class Arg<A> extends Triplet<String, A, ArgumentType<A>> {
            public Arg(String s, A a, ArgumentType<A> aArgumentType) {
                super(s, a, aArgumentType);
            }
        }

        private static <S> void executes0(ArgumentBuilder<S, ?> first, final List<Arg<?>> args, final BiFunction<CommandContext<S>, Object[], Integer> command) {
            if (args.isEmpty()) {
                throw new IllegalArgumentException("args is empty");
            }

            final int size = args.size();
            final Object[] defaultVals = new Object[size];
            var iterator = args.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                var a = iterator.next();
                defaultVals[i] = a.getB();
            }

            RequiredArgumentBuilder<S, ?> builder = null;
            int i = size;
            for (Arg<?> arg : args.reversed()) {
                final int index = i;
                RequiredArgumentBuilder<S, ?> b = RequiredArgumentBuilder.argument(arg.getA(), arg.getC());
                if (builder != null) b.then(builder);
                b.executes(context -> {
                    Object[] v = new Object[size];
                    for (int j = 0; j < index; j++) {
                        var a = args.get(j);
                        v[j] = context.getArgument(a.getA(), Object.class);
                    }
                    System.arraycopy(defaultVals, index, v, index, size - index);
                    return command.apply(context, v);
                });
                builder = b;
                i--;
            }
            if (builder != null) first.then(builder);
            first.executes(context -> command.apply(context, defaultVals));
        }
    }
}
